package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView(
    originLat: Double?,
    originLng: Double?,
    destinationLat: Double?,
    destinationLng: Double?,
    onMapClick: (Double, Double) -> Unit,
    modifier: Modifier
) {
    val hasOrigin = originLat != null && originLng != null
    val hasDestination = destinationLat != null && destinationLng != null

    if (!hasOrigin && !hasDestination) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Search for a place above and it will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // Centre on whichever end is known; with both, the midpoint keeps them both in frame at a
    // span wide enough for the distance between them.
    val centreLat = remember(originLat, destinationLat) {
        listOfNotNull(originLat, destinationLat).average()
    }
    val centreLng = remember(originLng, destinationLng) {
        listOfNotNull(originLng, destinationLng).average()
    }
    val span = remember(originLat, originLng, destinationLat, destinationLng) {
        if (hasOrigin && hasDestination) {
            val latMetres = kotlin.math.abs(originLat!! - destinationLat!!) * 111_000
            val lngMetres = kotlin.math.abs(originLng!! - destinationLng!!) * 111_000
            maxOf(latMetres, lngMetres, 2_000.0) * 1.6
        } else {
            20_000.0
        }
    }

    UIKitView(
        modifier = modifier.fillMaxSize(),
        factory = {
            MKMapView().apply {
                val centre = CLLocationCoordinate2DMake(centreLat, centreLng)
                setRegion(MKCoordinateRegionMakeWithDistance(centre, span, span), animated = false)

                if (hasOrigin) {
                    addAnnotation(
                        MKPointAnnotation().apply {
                            setCoordinate(CLLocationCoordinate2DMake(originLat!!, originLng!!))
                            setTitle("From")
                        }
                    )
                }
                if (hasDestination) {
                    addAnnotation(
                        MKPointAnnotation().apply {
                            setCoordinate(
                                CLLocationCoordinate2DMake(destinationLat!!, destinationLng!!)
                            )
                            setTitle("To")
                        }
                    )
                }
            }
        }
    )
}
