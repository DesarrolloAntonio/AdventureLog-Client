package com.desarrollodroide.adventurelog.feature.mapgoogle

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.desarrollodroide.adventurelog.feature.ui.map.AppMapSpec
import com.desarrollodroide.adventurelog.feature.ui.map.GoogleMapBackend
import com.google.android.gms.maps.MapsInitializer

/** Google Maps for AppMap. Registered in Koin by the play flavor only. */
class GoogleMapsBackend : GoogleMapBackend {

    override fun isAvailable(context: Context): Boolean =
        ProcessAvailability.isAvailable { MapsInitializer.initialize(context.applicationContext) }

    @Composable
    override fun Map(spec: AppMapSpec, modifier: Modifier) = GoogleMapContent(spec, modifier)

    private companion object {
        /** One answer per process: Play Services do not come and go while the app runs. */
        val ProcessAvailability = GoogleMapsAvailability()
    }
}
