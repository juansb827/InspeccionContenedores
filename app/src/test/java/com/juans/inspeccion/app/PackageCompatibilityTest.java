package com.juans.inspeccion.app;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.view.*;
import androidx.fragment.app.FragmentFactory;
import com.juans.inspeccion.R;
import com.juans.inspeccion.Mundo.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.io.*;
import static org.junit.Assert.*;

/** Android identity/XML gates; no authenticated screen setup, SQL, FTP or hardware. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=29,application=Application.class)
public class PackageCompatibilityTest {
    @Test public void originalAndImplementationComponentsRemainRegisteredWithMatchingProvider() throws Exception {
        Context app=RuntimeEnvironment.getApplication(); PackageManager pm=app.getPackageManager();
        String[][] screens={{"LoginActivity","access"},{"MainActivity","access"},{"SettingsActivity","settings"},{"InspeccionActivity","inspection"},{"FichaContenedorActivity","containers"},{"PendientesActivity","pending"},{"ConsultasActivity","search"},{"VisualizarActivity","search"},{"ImpresoraActivity","printing"}};
        for(String[] screen:screens) {
            String old="com.juans.inspeccion.Interfaz."+screen[0], implementation="com.juans.inspeccion.ui."+screen[1]+"."+screen[0];
            pm.getActivityInfo(new ComponentName(app,old),0); pm.getActivityInfo(new ComponentName(app,implementation),0);
            assertTrue(Class.forName(implementation).isAssignableFrom(Class.forName(old)));
        }
        pm.getReceiverInfo(new ComponentName(app,"com.juans.inspeccion.Other.NetWorkChangeReceiver"),0);
        pm.getServiceInfo(new ComponentName(app,"com.juans.inspeccion.Other.SimpleService"),0);
        assertEquals("com.juans.inspeccion.fileprovider",pm.resolveContentProvider("com.juans.inspeccion.fileprovider",0).authority);
    }
    @Test public void originalFragmentNamesResolveToDescriptiveImplementations() {
        FragmentFactory factory=new FragmentFactory(); ClassLoader loader=getClass().getClassLoader();
        assertTrue(factory.instantiate(loader,"com.juans.inspeccion.Interfaz.Inspeccion_tabs.p1") instanceof com.juans.inspeccion.ui.inspection.InspectionInformationFragment);
        assertTrue(factory.instantiate(loader,"com.juans.inspeccion.Interfaz.Inspeccion_tabs.p2") instanceof com.juans.inspeccion.ui.inspection.InspectionDamageFragment);
        assertTrue(factory.instantiate(loader,"com.juans.inspeccion.Interfaz.Gallery.PhotoGalleryFragment") instanceof com.juans.inspeccion.ui.media.gallery.PhotoGalleryFragment);
        assertTrue(factory.instantiate(loader,"com.juans.inspeccion.Interfaz.Camera.CameraCaptureFragment") instanceof com.juans.inspeccion.ui.media.camera.CameraCaptureFragment);
        assertTrue(factory.instantiate(loader,"com.juans.inspeccion.Interfaz.Dialogs.EditTextDialog") instanceof com.juans.inspeccion.ui.common.dialogs.EditTextDialog);
    }
    @Test public void operatorFormXmlInflatesWithMovedAndRetainedWidgets() {
        Activity host=Robolectric.buildActivity(Activity.class).setup().get(); host.setTheme(R.style.CustomActionBarTheme);
        for(int layout:new int[]{R.layout.fragment_entrada_pag1,R.layout.fragment_salida_pag1,R.layout.entrada_empresa_transporte,R.layout.salida_empresa_transporte,R.layout.nuevo_danio}) {
            View form=LayoutInflater.from(host).inflate(layout,null); assertNotNull(form);
        }
        assertNotNull(new com.juans.inspeccion.ui.common.widgets.SlidingTabLayout(host)); host.finish();
    }
    @Test public void activeModelSerializationIdentitiesRemainStable() {
        assertEquals(7140579573600875404L,ObjectStreamClass.lookup(Inspeccion.class).getSerialVersionUID());
        assertEquals(4164294007417506670L,ObjectStreamClass.lookup(Album.class).getSerialVersionUID());
        assertEquals(779705593577192224L,ObjectStreamClass.lookup(Danio.class).getSerialVersionUID());
        assertEquals(6522051388807633138L,ObjectStreamClass.lookup(DaniosManager.class).getSerialVersionUID());
    }
}
