#!/usr/bin/env bash
# Guards the native-UI split: shared (commonMain/commonTest) code must stay free of Compose.
#   (a) no *.kt under src/commonMain or src/commonTest imports/uses Compose
#   (b) no build file declares a Compose-family dependency inside a commonMain block
#   (c) every project type a *ViewModel.kt (commonMain) imports is declared in a file with no Compose import
# Usage: scripts/check-common-compose.sh [a|b|c ...]   (no argument = all checks)
# Exit 0 when the selected checks are clean. Plain bash 3.2 + grep/find only.
set -u
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT" || exit 2

COMPOSE_RE='androidx\.compose|org\.jetbrains\.compose'
fail=0

common_kt() { # list *.kt in commonMain/commonTest, outside build/
  find . \( -path '*/build' -o -path './.git' -o -path '*/.gradle' -o -path './.superpowers' \) -prune -o \
    -type f -name '*.kt' \( -path '*/src/commonMain/*' -o -path '*/src/commonTest/*' \) -print | sort
}

check_a() {
  echo "== (a) Compose references in commonMain/commonTest sources =="
  local n=0 f
  while IFS= read -r f; do
    if grep -Eq "$COMPOSE_RE" "$f"; then echo "  $f"; n=$((n+1)); fi
  done < <(common_kt)
  echo "  -> $n offending file(s)"
  [ "$n" -eq 0 ]
}

check_b() {
  echo "== (b) Compose dependencies inside commonMain blocks =="
  local n=0 f hits
  while IFS= read -r f; do
    # Track brace depth inside a commonMain { } block; flag Compose-family dependency lines in it.
    hits=$(awk '
      function count(s, ch,   t) { t = s; return gsub(ch, "", t) }
      {
        line = $0
        if (!inblock && line ~ /commonMain[[:space:]]*(\.dependencies)?[[:space:]]*(\{|by)/ || (!inblock && line ~ /commonMain.*dependencies/)) {
          inblock = 1; depth = 0
        }
        if (inblock) {
          depth += count(line, "\\{") - count(line, "\\}")
          if (line ~ /(compose|navigation-compose|koin\.compose|coil\.compose|lifecycle\.runtimeCompose|libs\.[A-Za-z0-9_.-]*compose)/ &&
              line !~ /^[[:space:]]*(\/\/|\*)/ ) {
            if (line ~ /compose\.components\.resources/ && FILENAME ~ /core\/common/) next
            printf "%s:%d: %s\n", FILENAME, NR, line
          }
          if (depth <= 0 && line ~ /\}/) inblock = 0
        }
      }' "$f")
    if [ -n "$hits" ]; then echo "$hits" | sed 's/^/  /'; n=$((n+1)); fi
  done < <(find . \( -path '*/build' -o -path './.git' -o -path '*/.gradle' -o -path './.superpowers' \) -prune -o \
            -type f \( -name 'build.gradle.kts' -o -path './build-logic/*.kt' -o -path './build-logic/*.kts' \) -print | sort)
  echo "  -> $n offending file(s)"
  [ "$n" -eq 0 ]
}

# Find the commonMain file declaring simple name $2 in package $1. Prints matches, one per line.
declaring_files() {
  local pkg="$1" name="$2" dir d
  dir="$(echo "$pkg" | tr . /)"
  find . \( -path '*/build' -o -path './.git' -o -path './.superpowers' \) -prune -o \
    -type d -path "*/src/commonMain/kotlin/$dir" -print | while IFS= read -r d; do
    grep -lE "^[[:space:]]*((public|internal|private|data|sealed|enum|abstract|open|annotation|value|fun|inline|expect|actual|const|suspend)[[:space:]]+)*(class|interface|object|typealias|fun|val|var)[[:space:]]+(<[^>]*>[[:space:]]*)?$name([^A-Za-z0-9_.]|$)" "$d"/*.kt 2>/dev/null
    # extension fun/val: "fun Recv.name(" / "val Recv.name"
    grep -lE "^[[:space:]]*((public|internal|inline|private)[[:space:]]+)*(fun|val)[[:space:]]+(<[^>]*>[[:space:]]*)?[A-Za-z0-9_<>?,. ]+\\.$name([^A-Za-z0-9_]|$)" "$d"/*.kt 2>/dev/null
  done | sort -u
}

check_c() {
  echo "== (c) ViewModel-imported project types declared in Compose-importing files =="
  local n=0 warn=0 vm imp path seg pkg name files cnt
  while IFS= read -r vm; do
    while IFS= read -r imp; do
      path="${imp#import }"; path="${path%% as *}"; path="$(echo "$path" | tr -d ' \r')"
      case "$path" in *'*') continue;; esac
      pkg=""; name=""
      local IFS_OLD="$IFS"; IFS=.
      set -- $path
      IFS="$IFS_OLD"
      for seg in "$@"; do
        case "$seg" in
          [A-Z]*) name="$seg"; break;;
          *) pkg="${pkg:+$pkg.}$seg"; last="$seg";;
        esac
      done
      if [ -z "$name" ]; then name="$last"; pkg="${pkg%.*}"; fi   # top-level fun/val
      files="$(declaring_files "$pkg" "$name")"
      cnt=$(printf '%s' "$files" | grep -c .)
      if [ "$cnt" -ne 1 ]; then
        echo "  warning: $vm imports $path; $cnt declaring files, not checked"; warn=$((warn+1)); continue
      fi
      if grep -Eq "^import ($COMPOSE_RE)" "$files"; then
        echo "  $vm imports $path -> declared in $files (imports Compose)"; n=$((n+1))
      fi
    done < <(grep -E '^import com\.desarrollodroide\.adventurelog\.' "$vm")
  done < <(find . \( -path '*/build' -o -path './.git' -o -path './.superpowers' \) -prune -o \
            -type f -name '*ViewModel.kt' -path '*/src/commonMain/*' -print | sort)
  echo "  -> $n offending import(s), $warn unresolved warning(s)"
  [ "$n" -eq 0 ]
}

checks="${*:-a b c}"
for c in $checks; do
  case "$c" in
    a) check_a || fail=1;;
    b) check_b || fail=1;;
    c) check_c || fail=1;;
    *) echo "unknown check: $c (use a, b or c)" >&2; exit 2;;
  esac
done
[ "$fail" -eq 0 ] && echo "OK" || echo "FAILED"
exit "$fail"
