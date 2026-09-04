# Design notes

Why the screens look the way they do, and the traps that shape is built on. The visual source is
`design/dashboard-redesign.dc.html` - a Claude Design document exported into the repo so the
layout can be read rather than eyeballed from screenshots. Read it, not a picture of it: the first
attempt at the dashboard was transcribed by eye and got the hero colour, the icons and the section
structure all wrong while the markup sat there saying otherwise.

## One palette, and it is yours

`primary` is `#006973`, the colour of the launcher icon, and it has been in `Color.kt` all along.
It was invisible on any modern device because **Material You was on by default**: on Android 12+
the app took its palette from the wallpaper, so it had no colour of its own. That is what "it
feels generic" meant, literally.

The default is now off (`SettingsRepositoryImpl.getUseDynamicColorsFromSettings`). The switch
stays in Settings for anyone who prefers the app to match their phone.

The redesign introduced no new colours. Its twenty-two hex values are the light scheme already in
`Color.kt`, tone for tone - `#9EEFFC` is `primaryContainer`, `#F5FAFB` is `surface`, `#CDE7EC` is
`secondaryContainer`. Use the role, never the hex, so dark theme follows.

The background is flat `surface`. It used to be a gradient mixed from three container tones, which
was right while the palette could be anything, and wrong once the cards became
`surfaceContainerLowest`: a ground that drifts leaves the same card reading as a card in one
corner and as nothing in another.

## One width per screen

`ContentColumn` (in `feature/ui`) caps a screen's content at `MaxContentWidth` (1040dp) and
centres it. Everything on a screen goes inside it - the app-bar title, the search field, the
filters, the cards - so they share a left and a right edge at every window size, and the leftover
room on a tablet becomes margin.

Capping elements individually is the mistake this replaced. It produces a screen holding a
full-bleed progress card, a 720dp search field and a three-column grid, each finding its own edge.

The dashboard is the exception: past 1000dp it becomes two columns, `MaxDashboardWidth` (1240dp)
wide, with a `DataRailWidth` (348dp) rail of numbers and dates on the right. That is the
redesign's own `minmax(0,1fr) 348px`, and it is what the empty third of a tablet is for.

## Layout traps, all found the hard way

- **A width cap on a spanned lazy-grid item does nothing.** The item is measured at an *exact*
  width and `widthIn(max = …)` coerces its own maximum back up into that fixed range. It compiles,
  it runs, the screenshot is identical. Wrap the content in a `Box(Modifier.fillMaxWidth())`,
  which absorbs the exact width and passes loose constraints down.
- **An `Image` inside `height(IntrinsicSize.Min)` reports its natural height** and grew the hero
  card to nine hundred dp. Put the image in a `Box` that carries the height instead.
- **`GridCells.Adaptive(360.dp)` needs 720dp for a second column.** Foldables sit at 674-841dp and
  fell to one. 300dp is the number that gives a foldable two.
- **A card with `enabled = false` is not "not clickable", it is greyed out and faded.** For a row
  that simply has nowhere to go, leave the card enabled and put `Modifier.clickable` on the
  content only when there is a destination.
- **Photographs get a ratio, not a height.** A fixed 250dp against a column 380dp wide on a phone
  and 340dp on a tablet is a tall card in one place and a square in the other.
- **Text over a photograph disappears** whenever the photograph is a bright sky. Names go under
  the image, on `surfaceContainerLowest`.

## Nothing Material in a shared view

The plan for iOS is native SwiftUI navigation, toolbars and search with the Compose content
reused inside. That only works if the content is not loudly Material, which is why the redesign
was chosen over the Material 3 Expressive variant: expressive lives *inside* the content - the
asymmetric corners, Roboto Flex, the tonal blocks - not in the chrome.

So keep `TopAppBar`, `NavigationBar`, `FloatingActionButton` and ripple out of anything meant to
be shared. Cards, text, photographs and pills travel; chrome does not. `MainShell` breaks this
rule deliberately - it is the chrome, and it is the piece iOS replaces.

Material 3 Expressive is not available anyway: `ButtonGroup`, `MaterialShapes`, `LoadingIndicator`
and `SplitButtonLayout` do not resolve in Compose Multiplatform 1.12.0-alpha03, which is the
newest published version. Expressive lives in the Android-only `androidx.compose.material3` line.
