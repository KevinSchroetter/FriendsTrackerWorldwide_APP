package com.osmap.pbfrks.friendstrackerworldwide;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * Created by kevin on 21-Jun-17.
 */

class SpecialMarker extends Marker {
    private String id = null;
    public SpecialMarker(MapView mapView, String id) {
        super(mapView);
        this.id = id;
    }
    public String getId(){
        return id;
    }
    public void updateId(String newId){
        this.id = newId;
    }
}
