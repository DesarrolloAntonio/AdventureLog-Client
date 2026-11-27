package com.desarrollodroide.adventurelog.feature.locations

import com.desarrollodroide.adventurelog.feature.locations.viewmodel.webPhotoFileName
import kotlin.test.Test
import kotlin.test.assertEquals

/** The name a photo from a link is uploaded under: the server refuses one it can't read a type from. */
class WebPhotoFileNameTest {

    @Test
    fun theQueryStringIsNotPartOfTheName() {
        // Wikipedia's thumbnails carry utm_ parameters; the server refused "…jpg?utm_source=…".
        assertEquals(
            "500px-Rgelchoc.jpg",
            webPhotoFileName("https://upload.wikimedia.org/a/b/500px-Rgelchoc.jpg?utm_source=en.wikipedia.org&utm_campaign=api")
        )
    }

    @Test
    fun norIsTheFragment() {
        assertEquals("photo.png", webPhotoFileName("https://x.y/photo.png#top"))
    }

    @Test
    fun aPlainLinkKeepsItsName() {
        assertEquals("Example.jpg", webPhotoFileName("https://upload.wikimedia.org/wikipedia/commons/a/a9/Example.jpg"))
    }

    @Test
    fun aLinkWithNoExtensionIsAJpeg() {
        assertEquals("image.jpg", webPhotoFileName("https://x.y/photos/12345?size=large"))
    }
}
