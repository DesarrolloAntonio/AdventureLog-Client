package com.desarrollodroide.adventurelog.feature.detail.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKPointAnnotation
import platform.darwin.NSObject

/**
 * iOS implementation of MapView using Apple Maps.
 * Displays a map centered at the specified coordinates.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView(
    latitude: String,
    longitude: String,
    modifier: Modifier
) {
    // Parse latitude and longitude
    val lat = remember(latitude) {
        try {
            latitude.toDouble()
        } catch (e: NumberFormatException) {
            0.0 // Default if parsing fails
        }
    }
    
    val long = remember(longitude) {
        try {
            longitude.toDouble()
        } catch (e: NumberFormatException) {
            0.0 // Default if parsing fails
        }
    }
    
    // Map delegate for handling map events if needed
    class MapDelegate : NSObject(), MKMapViewDelegateProtocol {
        // Map delegate implementation - can be extended for additional functionality
    }
    
    val mapDelegate = remember { MapDelegate() }
    
    // Using the updated UIKitView API
    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                setDelegate(mapDelegate)
                
                // Create coordinate and region
                val coordinate = CLLocationCoordinate2DMake(lat, long)
                val region = MKCoordinateRegionMakeWithDistance(coordinate, 1000.0, 1000.0)
                setRegion(region, animated = false)
                
                // Add a pin at the adventure location
                val annotation = MKPointAnnotation().apply {
                    setCoordinate(coordinate)
                    setTitle("Adventure Location")
                }
                addAnnotation(annotation)
            }
        },
        update = { mapView ->
            // Update the map if coordinates change
            val coordinate = CLLocationCoordinate2DMake(lat, long)
            val updatedRegion = MKCoordinateRegionMakeWithDistance(coordinate, 1000.0, 1000.0)
            mapView.setRegion(updatedRegion, animated = true)
        }
    )
}
