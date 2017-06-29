package com.osmap.pbfrks.friendstrackerworldwide;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * A class extending Marker
 * Used of adding an ID field which is used for deleting a marker in the application
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */

public class SpecialMarker extends Marker {
    private String id = null;
    private String description = null;
    public SpecialMarker(MapView mapView) {
        super(mapView);
    }
    public String getId(){
        return id;
    }
    public void updateId(String newId){
        this.id = newId;
    }
}
