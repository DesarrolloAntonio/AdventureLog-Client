package com.desarrollodroide.adventurelog.feature.map.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.*
import platform.darwin.NSObject

/**
 * iOS implementation of AdventureMapView using Apple Maps.
 * Displays adventures as annotations on the map.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdventureMapView(
    adventures: List<Adventure>,
    visitedRegions: List<VisitedRegion>,
    showRegions: Boolean,
    onAdventureClick: (adventureId: String) -> Unit,
    modifier: Modifier
) {
    // Map delegate for handling map events
    class MapDelegate(
        private val onAdventureClick: (adventureId: String) -> Unit
    ) : NSObject(), MKMapViewDelegateProtocol {
        
        override fun mapView(mapView: MKMapView, didSelectAnnotationView: MKAnnotationView) {
            val annotation = didSelectAnnotationView.annotation
            if (annotation is AdventureAnnotation) {
                onAdventureClick(annotation.adventureId)
            }
        }
    }
    
    val mapDelegate = remember { MapDelegate(onAdventureClick) }
    
    UIKitView(
        modifier = modifier,
        factory = {
            MKMapView().apply {
                setDelegate(mapDelegate)
                
                // Add annotations for all adventures
                adventures.forEach { adventure ->
                    val lat = adventure.latitude?.toDoubleOrNull()
                    val lng = adventure.longitude?.toDoubleOrNull()
                    
                    if (lat != null && lng != null) {
                        val annotation = AdventureAnnotation(
                            adventureId = adventure.id,
                            title = adventure.name,
                            subtitle = adventure.location ?: "",
                            coordinate = CLLocationCoordinate2DMake(lat, lng)
                        )
                        addAnnotation(annotation)
                    }
                }
                
                // Add region overlays if enabled
                if (showRegions) {
                    visitedRegions.forEach { region ->
                        region.latitude?.let { lat ->
                            region.longitude?.let { lng ->
                                // Create a circle overlay for the region
                                val coordinate = CLLocationCoordinate2DMake(lat, lng)
                                val circle = MKCircle.circleWithCenterCoordinate(
                                    coordinate,
                                    radius = 50000.0 // 50km radius
                                )
                                addOverlay(circle)
                            }
                        }
                    }
                }
                
                // Fit all annotations
                if (adventures.isNotEmpty()) {
                    showAnnotations(annotations(), animated = true)
                } else {
                    // Default to world view if no adventures
                    val coordinate = CLLocationCoordinate2DMake(40.416775, -3.703790) // Madrid
                    val region = MKCoordinateRegionMakeWithDistance(coordinate, 5000000.0, 5000000.0)
                    setRegion(region, animated = false)
                }
            }
        },
        update = { mapView ->
            // Update annotations if adventures change
            mapView.removeAnnotations(mapView.annotations())
            mapView.removeOverlays(mapView.overlays())
            
            adventures.forEach { adventure ->
                val lat = adventure.latitude?.toDoubleOrNull()
                val lng = adventure.longitude?.toDoubleOrNull()
                
                if (lat != null && lng != null) {
                    val annotation = AdventureAnnotation(
                        adventureId = adventure.id,
                        title = adventure.name,
                        subtitle = adventure.location ?: "",
                        coordinate = CLLocationCoordinate2DMake(lat, lng)
                    )
                    mapView.addAnnotation(annotation)
                }
            }
            
            // Add region overlays if enabled
            if (showRegions) {
                visitedRegions.forEach { region ->
                    region.latitude?.let { lat ->
                        region.longitude?.let { lng ->
                            val coordinate = CLLocationCoordinate2DMake(lat, lng)
                            val circle = MKCircle.circleWithCenterCoordinate(
                                coordinate,
                                radius = 50000.0
                            )
                            mapView.addOverlay(circle)
                        }
                    }
                }
            }
            
            if (adventures.isNotEmpty()) {
                mapView.showAnnotations(mapView.annotations(), animated = true)
            }
        }
    )
}

/**
 * Custom annotation class for adventures
 */
@OptIn(ExperimentalForeignApi::class)
private class AdventureAnnotation(
    val adventureId: String,
    private val title: String,
    private val subtitle: String,
    private val coordinate: CLLocationCoordinate2D
) : NSObject(), MKAnnotationProtocol {
    
    override fun coordinate(): CLLocationCoordinate2D = coordinate
    
    override fun title(): String? = title
    
    override fun subtitle(): String? = subtitle
}
