package io.ionic.starter;

import com.getcapacitor.BridgeActivity;
import io.radar.sdk.Radar;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Radar.initialize(this, "prj_test_pk_0000000000000000000000000000000000000000");
    }

}
