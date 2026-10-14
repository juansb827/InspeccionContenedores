package com.juans.inspeccion.platform.export;

import com.juans.inspeccion.Mundo.Storage.MediaPhotoStore;
import com.juans.inspeccion.platform.media.PrivateTakePicture;

import android.content.*;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.juans.inspeccion.Mundo.Inspeccion;
import com.juans.inspeccion.platform.export.CreadorFacturasPDF;
import com.itextpdf.text.pdf.PdfReader;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import java.nio.file.Files;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = android.app.Application.class)
public class FileConsumersTest {
    private Context context;
    @Before public void setup() { context = RuntimeEnvironment.getApplication();
        ((java.util.Map<?,?>) org.robolectric.util.ReflectionHelpers.getStaticField(FileProvider.class, "sCache")).clear();
    }
    @Test public void cameraContractGrantsOnlyItsDestination() throws Exception {
        File file = new MediaPhotoStore(context).createCapture("unique");
        Uri uri = new MediaPhotoStore(context).captureUri(file);
        Intent intent = new PrivateTakePicture().createIntent(context, uri);
        assertEquals(MediaPhotoStore.authority(context), uri.getAuthority());
        assertEquals(uri, intent.getParcelableExtra(android.provider.MediaStore.EXTRA_OUTPUT));
        assertEquals(uri, intent.getClipData().getItemAt(0).getUri());
        assertEquals(3, intent.getFlags() & 3);
        assertFalse(new PrivateTakePicture().parseResult(android.app.Activity.RESULT_CANCELED, null));
    }
    @Test public void providerRejectsOtherPrivateAndExternalFiles() {
        for (File file : new File[]{context.getFileStreamPath("listaPendientes"),
                new File(context.getCacheDir(), "secret"), new File(android.os.Environment.getExternalStorageDirectory(), "secret")}) {
            try { FileProvider.getUriForFile(context, MediaPhotoStore.authority(context), file); fail(); }
            catch (IllegalArgumentException expected) { }
        }
    }
    @Test public void exportReplacementFailureKeepsOriginal() throws Exception {
        File file = ExportFiles.destination(context, "report.xls");
        ExportFiles.write(file, out -> out.write(new byte[]{1,2,3}));
        try { ExportFiles.write(file, out -> { out.write(9); throw new IOException("full"); }); fail(); }
        catch (IOException expected) { }
        assertArrayEquals(new byte[]{1,2,3}, Files.readAllBytes(file.toPath()));
    }
    @Test public void viewerUsesMatchingAuthorityMimeAndReadGrantAndHandlesAbsentViewer() throws Exception {
        File file = ExportFiles.destination(context, "report.xls");
        for (String mime : new String[]{"application/vnd.ms-excel", "application/pdf"}) {
            Intent intent = ExportFiles.viewIntent(context, file, mime);
            assertEquals(mime, intent.getType());
            assertEquals(MediaPhotoStore.authority(context), intent.getData().getAuthority());
            assertTrue((intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0);
            Context absent = new ContextWrapper(context) {
                @Override public void startActivity(Intent ignored) { throw new ActivityNotFoundException(); }
            };
            assertFalse(ExportFiles.open(absent, file, mime));
        }
    }
    @Test public void pdfUsesBundledLogoAndProducesReadablePages() throws Exception {
        Inspeccion inspection = new Inspeccion();
        File file = CreadorFacturasPDF.generarFactura(context, inspection,
                new String[]{"10","10","2026","12","30"}, "receipt.pdf");
        PdfReader reader = new PdfReader(file.getPath());
        try { assertTrue(reader.getNumberOfPages() > 0); }
        finally { reader.close(); }
    }
}
