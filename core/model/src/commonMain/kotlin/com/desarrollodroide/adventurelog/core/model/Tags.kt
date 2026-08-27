package com.desarrollodroide.adventurelog.core.model

/**
 * A tag nobody typed.
 *
 * Importers stamp the identifier a record had in whatever system it came from onto the record they
 * create, so that running the import again updates the place rather than duplicating it. They land
 * in the same list as "hiking" and "with the kids", and the server has no notion of a tag that is
 * only for machines - which is why they show up on the web too.
 *
 * A tag carrying a UUID is one of those. No one types a UUID into a tag box.
 */
private val UUID = Regex(
    "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
    RegexOption.IGNORE_CASE
)

fun String.isMachineTag(): Boolean = UUID.containsMatchIn(this)

/**
 * The tags worth showing someone.
 *
 * Only display surfaces filter: the edit form keeps every tag the record actually has, so saving a
 * place never quietly deletes data the user could not see. Removing one stays a deliberate act.
 */
fun List<String>.userTags(): List<String> = filterNot { it.isMachineTag() }
