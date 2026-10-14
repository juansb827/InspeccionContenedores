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
    // DRAFT-006: explicitly requested diagnostics, without a retirement disclaimer.
    @Test public void explicitStatusShowsCountsAndClosingLeavesStorageIntact() throws Exception {
        Host host=Robolectric.buildActivity(Host.class).setup().get();
        Files.write(host.getFileStreamPath("listaPendientes").toPath(),new byte[]{(byte)0xac,(byte)0xed,0,5});
        StorageNotice.show(host);
        AlertDialog first=(AlertDialog)ShadowDialog.getLatestDialog(); assertTrue(first.isShowing());
        android.widget.TextView message=first.findViewById(android.R.id.message);
        assertEquals("Archivos aislados: 1\nNo hay archivos sin resolver.",message.getText().toString());
        assertEquals("Cerrar",first.getButton(AlertDialog.BUTTON_POSITIVE).getText().toString());
        first.getButton(AlertDialog.BUTTON_POSITIVE).performClick(); org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        assertFalse(first.isShowing());
        assertEquals(1,StorageCutover.prepare(host).isolated.size());
        StorageNotice.show(host); assertNotSame(first,ShadowDialog.getLatestDialog()); host.finish();
    }
}
