package io.ionic.starter;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

import io.radar.capacitor.RadarPlugin;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //loads plugin and initializes Radar using the publishable key in strings.xml
        registerPlugin(RadarPlugin.class);
    }

}
