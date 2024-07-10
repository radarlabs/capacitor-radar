package io.ionic.starter;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

import io.radar.sdk.Radar;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Radar.initialize(this, "prj_test_pk_333df0ef19f87a254f12cb1818de8443181054a7");

        super.onCreate(savedInstanceState);
    }

}
