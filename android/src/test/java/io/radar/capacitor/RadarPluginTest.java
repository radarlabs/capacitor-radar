package io.radar.capacitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Build;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.getcapacitor.Bridge;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.UUID;

import io.radar.sdk.Radar;

@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.P)
public class RadarPluginTest {

    private RadarPlugin mPlugin;
    private Bridge mBridge;

    @Before
    public void setUp() {
        mPlugin = new RadarPlugin();
        mBridge = mock(Bridge.class);
        when(mBridge.getContext()).thenReturn(ApplicationProvider.getApplicationContext());
        mPlugin.setBridge(mBridge);
    }

    private void initializeSdk() {
        PluginCall initializer = mock(PluginCall.class);
        when(initializer.getString(matches("publishableKey")))
                .thenReturn("prj_test_pk_0000000000000000000000000000000000000000");
        mPlugin.initialize(initializer);
    }

    @Test
    public void testSetMetadata() {
        initializeSdk();
        assertNull(Radar.getMetadata());
        PluginCall pluginCall = mock(PluginCall.class);
        JSObject metadata = new JSObject();
        String value = UUID.randomUUID().toString();
        metadata.put("test", value);
        when(pluginCall.getObject(matches("metadata"))).thenReturn(metadata);
        mPlugin.setMetadata(pluginCall);
        JSONObject json = Radar.getMetadata();
        assertNotNull(json);
        assertEquals(value, json.optString("test"));
    }
}
