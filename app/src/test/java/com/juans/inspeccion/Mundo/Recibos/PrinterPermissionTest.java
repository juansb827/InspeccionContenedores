package com.juans.inspeccion.Mundo.Recibos;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.ActivityResult;
import androidx.core.app.ActivityOptionsCompat;
import com.juans.inspeccion.Interfaz.PrinterPermissionGate;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowBluetoothAdapter;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 31, application = android.app.Application.class)
public class PrinterPermissionTest {
    private ActivityController<ComponentActivity> controller;
    private ComponentActivity activity;
    private Registry registry;
    private PrinterPermissionGate gate;
    private int operations;
    @Before public void setup() {
        ShadowBluetoothAdapter.setIsBluetoothSupported(true);
        Shadows.shadowOf(BluetoothAdapter.getDefaultAdapter()).setState(BluetoothAdapter.STATE_ON);
        controller = Robolectric.buildActivity(ComponentActivity.class).create();
        activity = controller.get(); registry = new Registry(); gate = new PrinterPermissionGate(activity, registry);
        controller.start().resume();
    }
    @After public void cleanup() { controller.pause().stop().destroy(); }
    @Test public void android12RequestsBothPermissionsAndDenialStopsOperation() {
        gate.request(() -> operations++);
        assertArrayEquals(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, (String[]) registry.input);
        registry.dispatchResult(registry.code, Collections.singletonMap(Manifest.permission.BLUETOOTH_CONNECT, false));
        assertEquals(0, operations);
    }
    @Test public void grantResumesExactlyOnce() {
        gate.request(() -> operations++);
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(PrinterAccess.permissions(31));
        registry.dispatchResult(registry.code, Collections.singletonMap(Manifest.permission.BLUETOOTH_CONNECT, true));
        registry.dispatchResult(registry.code, Collections.singletonMap(Manifest.permission.BLUETOOTH_CONNECT, true));
        assertEquals(1, operations);
    }
    @Test public void deniedEnablementAndRevokedPermissionCannotPrint() {
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).grantPermissions(PrinterAccess.permissions(31));
        Shadows.shadowOf(BluetoothAdapter.getDefaultAdapter()).setState(BluetoothAdapter.STATE_OFF);
        gate.request(() -> operations++);
        assertTrue(registry.input instanceof android.content.Intent);
        registry.dispatchResult(registry.code, new ActivityResult(android.app.Activity.RESULT_CANCELED, null));
        assertEquals(0, operations);
        gate.request(() -> operations++);
        Shadows.shadowOf(BluetoothAdapter.getDefaultAdapter()).setState(BluetoothAdapter.STATE_ON);
        Shadows.shadowOf(RuntimeEnvironment.getApplication()).denyPermissions(Manifest.permission.BLUETOOTH_SCAN);
        registry.dispatchResult(registry.code, new ActivityResult(android.app.Activity.RESULT_OK, null));
        assertEquals(0, operations);
    }
    @Test public void unsupportedBluetoothDoesNotRequestPermissionOrPrint() {
        ShadowBluetoothAdapter.setIsBluetoothSupported(false);
        gate.request(() -> operations++);
        assertEquals(0, registry.launches); assertEquals(0, operations);
    }
    @Test @Config(sdk = 30) public void android11NeedsNoRuntimeBluetoothPermissions() {
        assertArrayEquals(new String[0], PrinterAccess.permissions(30));
        gate.request(() -> operations++); assertEquals(0, registry.launches); assertEquals(1, operations);
    }
    @Test @Config(sdk = 22) public void minimumSdkNeedsNoRuntimeBluetoothPermissions() {
        gate.request(() -> operations++); assertEquals(0, registry.launches); assertEquals(1, operations);
    }
    private static class Registry extends ActivityResultRegistry {
        int code, launches; Object input;
        @Override public <I,O> void onLaunch(int requestCode, ActivityResultContract<I,O> contract, I value, ActivityOptionsCompat options) {
            code = requestCode; input = value; launches++;
        }
    }
}
