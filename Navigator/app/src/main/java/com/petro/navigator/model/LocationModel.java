package com.petro.navigator.model;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.mapping.MapMarker;
import com.petro.navigator.R;

import java.io.IOException;

/**
 *
 * Classe modelo da Localidade (POI)
 */
public class LocationModel {

    // Atribuutos privados
    private int _id;
    private String _desc;
    private float _lon;
    private float _lat;
    private MapMarker _marker;

    /**
     * cosntrutor
     */
    public LocationModel(){}
    public LocationModel(int id, String title, String desc, float lon, float lat){

        Image image = new Image();
        try {
            image.setImageResource(R.drawable.pin64);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this._id  = id;
        this._lon = _lon;
        this._lat = _lat;
        this._marker = new MapMarker(new GeoCoordinate(lat, lon), image);
        this._marker.setTitle(title);
        this._marker.setDescription(desc);
    }
    public LocationModel(String title, String desc, float lon, float lat){

        Image image = new Image();
        try {
            image.setImageResource(R.drawable.pin64);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this._lon = lon;
        this._lat = lat;
        this._marker = new MapMarker(new GeoCoordinate(lat, lon), image);
        this._marker.setTitle(title);
        this._marker.setDescription(desc);
    }

    public float getLon() { return _lon; }
    public void setLon(float val) { _lon = val; }

    public float getLat() { return _lat; }
    public void setLat(float val) { _lat = val; }

    public MapMarker getMarker() { return _marker; }
    public String getTitle() { return _marker.getTitle(); }
    public String getDesc() { return _marker.getDescription(); }
}
