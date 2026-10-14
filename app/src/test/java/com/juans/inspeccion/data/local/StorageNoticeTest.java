package com.juans.inspeccion.data.local;

import android.os.Bundle;
import androidx.appcompat.app.*;
import com.juans.inspeccion.ui.settings.StorageNotice;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;
import java.nio.file.Files;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=29,application=android.app.Application.class)
public class StorageNoticeTest {
    public static class Host extends AppCompatActivity {
        @Override public void onCreate(Bundle state) { setTheme(com.juans.inspeccion.R.style.CustomActionBarTheme); super.onCreate(state); }
    }
    // DRAFT-006: actual dialog action, not just a marker method tested in isolation.
    @Test public void closingDoesNotAcknowledgeButEntendidoIsDurable() throws Exception {
        Host host=Robolectric.buildActivity(Host.class).setup().get();
        Files.write(host.getFileStreamPath("listaPendientes").toPath(),new byte[]{(byte)0xac,(byte)0xed,0,5});
        StorageNotice.show(host,false);
        AlertDialog first=(AlertDialog)ShadowDialog.getLatestDialog(); assertTrue(first.isShowing());
        first.getButton(AlertDialog.BUTTON_NEGATIVE).performClick(); org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertTrue(StorageCutover.prepare(host).noticePending);
        StorageNotice.show(host,false); AlertDialog second=(AlertDialog)ShadowDialog.getLatestDialog();
        assertEquals("Entendido",second.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        second.getButton(AlertDialog.BUTTON_POSITIVE).performClick(); org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertFalse(StorageCutover.prepare(host).noticePending); assertFalse(StorageCutover.prepare(host).noticePending);
        StorageNotice.show(host,false); assertSame(second,ShadowDialog.getLatestDialog());
        StorageNotice.show(host,true); assertNotSame(second,ShadowDialog.getLatestDialog()); host.finish();
    }
}
