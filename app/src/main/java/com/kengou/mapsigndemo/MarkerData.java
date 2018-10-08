package com.kengou.mapsigndemo;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MarkerData {

    public static ArrayList<MarkerOptions> MARKER_OPTIONS_LIST = new ArrayList<>();

    static {

        MARKER_OPTIONS_LIST.add(
                new MarkerOptions()
                        .position(new LatLng(39.9035472811,116.3979148865))
                        .title("天安门广场")
        );
        MARKER_OPTIONS_LIST.add(
                new MarkerOptions()
                        .position(new LatLng(39.9256669251,116.3968849182))
                        .title("景山公园")
        );
        MARKER_OPTIONS_LIST.add(
                new MarkerOptions()
                        .position(new LatLng(39.9107896177,116.4161109924))
                        .title("北京协和医院")
        );
        MARKER_OPTIONS_LIST.add(
                new MarkerOptions()
                        .position(new LatLng(39.9121063240,116.3548278809))
                        .title("北京儿童医院")
        );
        MARKER_OPTIONS_LIST.add(
                new MarkerOptions()
                        .position(new LatLng(39.9604119309,116.3661575317))
                        .title("北京师范大学")
        );
    }
}
