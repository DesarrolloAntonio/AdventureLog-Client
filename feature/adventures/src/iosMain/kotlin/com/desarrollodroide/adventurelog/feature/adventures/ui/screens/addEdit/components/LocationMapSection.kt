package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKPointAnnotation
import platform.darwin.NSObject
import platform.UIKit.UITapGestureRecognizer

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun LocationMapSection(
    latitude: String?,
    longitude: String?,
    onMapClick: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier
) {
    // Default location (Madrid)
    val defaultLat = 40.4168
    val defaultLon = -3.7038
    
    // Parse coordinates
    val (lat, lon) = remember(latitude, longitude) {
        try {
            val parsedLat = latitude?.toDouble() ?: defaultLat
            val parsedLon = longitude?.toDouble() ?: defaultLon
            parsedLat to parsedLon
        } catch (_: NumberFormatException) {
            defaultLat to defaultLon
        }
    }
    
    // Map delegate to handle tap gestures
    class MapDelegate : NSObject(), MKMapViewDelegateProtocol {
        var mapClickCallback: ((Double, Double) -> Unit)? = null
        
        @ObjCAction
        fun handleMapTap(gestureRecognizer: UITapGestureRecognizer) {
            val mapView = gestureRecognizer.view as? MKMapView ?: return
            val touchPoint = gestureRecognizer.locationInView(mapView)
            val coordinate = mapView.convertPoint(
                point = touchPoint,
                toCoordinateFromView = mapView
            )
            
            // Access coordinate properties using useContents
            coordinate.useContents {
                mapClickCallback?.invoke(this.latitude, this.longitude)
            }
        }
    }
    
    val mapDelegate = remember { MapDelegate() }
    mapDelegate.mapClickCallback = onMapClick
    
    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                setDelegate(mapDelegate)
                
                // Set initial region
                val coordinate = CLLocationCoordinate2DMake(lat, lon)
                val region = MKCoordinateRegionMakeWithDistance(
                    centerCoordinate = coordinate,
                    latitudinalMeters = if (latitude != null) 5000.0 else 10000000.0,
                    longitudinalMeters = if (latitude != null) 5000.0 else 10000000.0
                )
                setRegion(region, animated = false)
                
                // Add tap gesture recognizer
                val tapGesture = UITapGestureRecognizer(
                    target = mapDelegate,
                    action = platform.objc.sel_registerName("handleMapTap:")
                )
                addGestureRecognizer(tapGesture)
                
                // Add marker if coordinates are provided
                if (latitude != null && longitude != null) {
                    val annotation = MKPointAnnotation().apply {
                        setCoordinate(coordinate)
                        setTitle("Selected Location")
                    }
                    addAnnotation(annotation)
                }
            }
        },
        update = { mapView ->
            // Update map when coordinates change
            val coordinate = CLLocationCoordinate2DMake(lat, lon)
            val updatedRegion = MKCoordinateRegionMakeWithDistance(
                centerCoordinate = coordinate,
                latitudinalMeters = if (latitude != null) 5000.0 else 10000000.0,
                longitudinalMeters = if (latitude != null) 5000.0 else 10000000.0
            )
            mapView.setRegion(updatedRegion, animated = true)
            
            // Update marker
            mapView.removeAnnotations(mapView.annotations)
            if (latitude != null && longitude != null) {
                val annotation = MKPointAnnotation().apply {
                    setCoordinate(coordinate)
                    setTitle("Selected Location")
                }
                mapView.addAnnotation(annotation)
            }
        }
    )
}
