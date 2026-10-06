package com.desarrollodroide.adventurelog.core.testing

/**
 * What every stub in this module does when nothing overrode it.
 *
 * The alternative - answering with an empty list - lets a test pass while the code under test
 * asks for something the test never set up, which is the failure mode these stubs exist to
 * prevent. Reaching one of these is a mistake in the test, and it should say so.
 */
internal fun unused(): Nothing =
    throw AssertionError("This test reached a call it does not override")
