package com.petro.navigator.model;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.mapping.MapMarker;
import com.petro.navigator.R;

import java.io.IOException;
import java.util.Date;

/**
 *
 * Classe modelo da Localidade (POI)
 */
public class LocationModel {

    // Atribuutos privados
    private int _id;
    private String _uf;
    private String _type;
    private String _title;
    private String _context;
    private String _desc;
    private int _val;
    private float _lon;
    private float _lat;
    private MapMarker _marker;

    /**
     * cosntrutor
     */
    public LocationModel(){}
    public LocationModel(int id, String uf, String type, String context, String title, String desc, int val, float lon, float lat){

        Image image = new Image();
        try {
            switch(type){
                case "Equipamento - Pé de galinha":
                    image.setImageResource(R.drawable.pe_galinha64);
                    break;
                case "Equipamento - PIG":
                    image.setImageResource(R.drawable.pig64);
                    break;
                case "Poço":
                    image.setImageResource(R.drawable.poco64);
                    break;
                case "Poste":
                    image.setImageResource(R.drawable.poste64);
                    break;
                default:
                    image.setImageResource(R.drawable.pin64);
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        this._id  = id;
        this._uf  = uf;
        this._type  = type;
        this._context  = context;
        this._title = title;
        this._desc = desc;
        this._val = val;
        this._lon = _lon;
        this._lat = _lat;
        this._marker = new MapMarker(new GeoCoordinate(lat, lon), image);
        this._marker.setTitle(title);
        this._marker.setDescription(desc);
    }
    public LocationModel(String uf, String type, String context, String title, String desc, int val, float lon, float lat){

        Image image = new Image();
        try {
            switch(type){
                case "Equipamento - Pé de galinha":
                    image.setImageResource(R.drawable.pe_galinha64);
                    break;
                case "Equipamento - PIG":
                    image.setImageResource(R.drawable.pig64);
                    break;
                case "Poço":
                    image.setImageResource(R.drawable.poco64);
                    break;
                case "Poste":
                    image.setImageResource(R.drawable.poste64);
                    break;
                default:
                    image.setImageResource(R.drawable.pin64);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this._uf  = uf;
        this._type  = type;
        this._context  = context;
        this._val = val;
        this._lon = lon;
        this._lat = lat;
        this._title = title;
        this._desc = desc;
        this._marker = new MapMarker(new GeoCoordinate(lat, lon), image);
        this._marker.setTitle(title);
        this._marker.setDescription(desc);
    }

    public float getLon() { return _lon; }
    public void setLon(float val) { _lon = val; }

    public float getLat() { return _lat; }
    public void setLat(float val) { _lat = val; }

    public String getUf() { return _uf; }
    public void setUf(String val) { _uf = val; }

    public String getType() { return _type; }
    public void setType(String val) { _type = val; }

    public String getContext() { return _context; }
    public void setContext(String val) { _context = val; }

    public int getDate() { return _val; }
    public void setDate(int val) { _val = val; }

    public MapMarker getMarker() { return _marker; }
    public String getTitle() { return _marker.getTitle(); }
    public String getDesc() { return _marker.getDescription(); }
}
