package io.radar.capacitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.Manifest;
import android.os.Build;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.getcapacitor.Bridge;
import com.getcapacitor.CapConfig;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.cordova.MockCordovaInterfaceImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameterized test to verify location permission statuses
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.Q)
public class PermissionsTest {

    private RadarPlugin mPlugin;
    private TestBridge mBridge;

    @Parameter
    public PermissionState mGpsLocationPermission;

    @Parameter(1)
    public PermissionState mWifiLocationPermission;

    @Parameter(2)
    public PermissionState mBackgroundLocationPermission;

    @Parameters(name = "{index}: " +
            "gpsLocationPermission? {0}. " +
            "wifiLocationPermission? {1}. " +
            "backgroundLocationPermission? {2}")
    public static Iterable<Object[]> data() {
        PermissionState[] states = PermissionState.values();
        Object[][] data = new Object[states.length * states.length * states.length][3];
        int i = 0;
        for (PermissionState gpsLocationPermission : states) {
            for (PermissionState wifiLocationPermission : states) {
                for (PermissionState backgroundLocationPermission : states) {
                    data[i] = new Object[]{gpsLocationPermission, wifiLocationPermission, backgroundLocationPermission};
                    i++;
                }
            }
        }
        return Arrays.asList(data);
    }

    @Before
    public void setUp() {
        mPlugin = new RadarPlugin();
        mBridge = mock(TestBridge.class);
        mPlugin.setBridge(mBridge);
    }

    private boolean isGranted(PermissionState state) {
        return PermissionState.GRANTED == state;
    }

    private boolean isDenied(PermissionState state) {
        return PermissionState.DENIED == state || PermissionState.PROMPT_WITH_RATIONALE == state;
    }

    @Test
    public void testGetLocationPermissionsStatus() {
        PluginCall pluginCall = mock(PluginCall.class);
        Map<String, PermissionState> states = new HashMap<>();
        states.put(Manifest.permission.ACCESS_FINE_LOCATION, mGpsLocationPermission);
        states.put(Manifest.permission.ACCESS_COARSE_LOCATION, mWifiLocationPermission);
        states.put(Manifest.permission.ACCESS_BACKGROUND_LOCATION, mBackgroundLocationPermission);
        when(mBridge.getPermissionStates(eq(mPlugin))).thenReturn(states);
        mPlugin.getLocationPermissionsStatus(pluginCall);
        String expectedStatus;
        if (isGranted(mGpsLocationPermission) || isGranted(mWifiLocationPermission)) {
            if (isGranted(mBackgroundLocationPermission)) {
                expectedStatus = "GRANTED_BACKGROUND";
            } else {
                expectedStatus = "GRANTED_FOREGROUND";
            }
        } else if (isDenied(mGpsLocationPermission) || isDenied(mWifiLocationPermission)) {
            expectedStatus = "DENIED";
        } else {
            expectedStatus = "NOT_DETERMINED";
        }
        ArgumentCaptor<JSObject> captor = ArgumentCaptor.forClass(JSObject.class);
        verify(pluginCall).resolve(captor.capture());
        JSObject json = captor.getValue();
        assertNotNull(json);
        String actualStatus = json.getString("status");
        assertEquals(expectedStatus, actualStatus);
    }

    /**
     * Simple subclass to expose the {@link #getPermissionStates(Plugin)} method
     */
    private static class TestBridge extends Bridge {

        /**
         * {@inheritDoc}
         */
        public TestBridge(AppCompatActivity context, WebView webView, List<Class<? extends Plugin>> initialPlugins,
                          MockCordovaInterfaceImpl cordovaInterface, CapConfig config) {
            super(context, webView, initialPlugins, cordovaInterface, null, null, config);
        }

        @Override
        public Map<String, PermissionState> getPermissionStates(Plugin plugin) {
            return super.getPermissionStates(plugin);
        }
    }
}
