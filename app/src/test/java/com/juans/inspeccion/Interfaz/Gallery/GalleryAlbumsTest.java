package com.juans.inspeccion.Interfaz.Gallery;

import android.content.Context;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.Mundo.Storage.StoredInspections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import java.io.*;
import java.util.List;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, application = android.app.Application.class)
public class GalleryAlbumsTest {
    @Test public void allFourFlowsResolveOnlyTheirOwnGeneralAndDamageAlbums() {
        for (String document : new String[]{"EP", "ET"}) for (String direction : new String[]{"ENTRADA", "SALIDA"}) {
            Inspeccion inspection = new Inspeccion(); inspection.setTipoDocumento(document); inspection.setTipoInspeccion(direction);
            Danio damage = new Danio(); inspection.getDaniosManager().getListaDanios().add(damage);
            Album general = inspection.getFotosGenerales();
            assertSame(general, GalleryAlbums.resolve(inspection, null, inspection.getPhotoId(), general.getId()));
            assertSame(damage.getAlbum(), GalleryAlbums.resolve(inspection, null, inspection.getPhotoId(), damage.getAlbum().getId()));
            assertNull(GalleryAlbums.resolve(inspection, null, "another-inspection", general.getId()));
            assertNull(GalleryAlbums.resolve(inspection, null, inspection.getPhotoId(), "another-album"));
            assertEquals("Información", GalleryAlbums.label(inspection, general.getId()));
            assertEquals("Daño 1", GalleryAlbums.label(inspection, damage.getAlbum().getId()));
        }
    }

    @Test public void unsavedDamageResolvesWithoutAssociatingItToAnotherAlbum() {
        Inspeccion inspection = new Inspeccion(); Album editing = new Album();
        assertSame(editing, GalleryAlbums.resolve(inspection, editing, inspection.getPhotoId(), editing.getId()));
        assertNull(inspection.findAlbum(editing.getId()));
        assertEquals("Daño en edición", GalleryAlbums.label(inspection, editing.getId()));
    }

    @Test public void browsingResolutionIsIndependentOfMutationRestrictions() {
        Inspeccion inspection = new Inspeccion(); inspection.setTipoInspeccion("SALIDA");
        Album damage = new Album(), general = inspection.getFotosGenerales();
        String id = inspection.getPhotoId();
        assertSame(damage, GalleryAlbums.resolve(inspection, damage, id, damage.getId()));
        assertFalse(GalleryAlbums.canDelete(inspection, damage, id, damage.getId(), false, null, false));
        assertTrue(GalleryAlbums.canDelete(inspection, damage, id, general.getId(), false, null, false));
        assertFalse(GalleryAlbums.canDelete(inspection, damage, id, general.getId(), true, null, false));
        assertFalse(GalleryAlbums.canDelete(inspection, damage, id, general.getId(), false, "MODIFICAR", false));
        assertFalse(GalleryAlbums.canDelete(inspection, damage, id, general.getId(), false, null, true));
    }

    @Test public void unavailableItemsKeepTheirPositionsAndSelectionUsesReference() throws Exception {
        Album album = new Album();
        album.agregarFoto("content://media/1"); album.getFotos().add("/old/Inspecciones/missing.jpg");
        album.agregarFoto("content://media/3");
        List<GalleryMediaItem> items = GalleryMediaItem.photos(album);
        assertEquals(3, items.size()); assertEquals("/old/Inspecciones/missing.jpg", items.get(1).reference);
        assertEquals(GalleryMediaItem.Kind.PHOTO, items.get(1).kind);
        album.getFotos().remove(0);
        assertEquals(1, GalleryMediaItem.position(GalleryMediaItem.photos(album), "content://media/3", 2));
        album.getFotos().remove(1);
        assertEquals(0, GalleryMediaItem.position(GalleryMediaItem.photos(album), "content://media/3", 1));
        album.getFotos().clear();
        assertEquals(-1, GalleryMediaItem.position(GalleryMediaItem.photos(album), null, 0));
    }

    @Test public void galleryEditsRemainReadableWithExistingSerializationAndRemoteNames() throws Exception {
        Inspeccion inspection = new Inspeccion(); Album album = inspection.getFotosGenerales();
        album.agregarFoto("content://media/1"); album.agregarFoto("content://media/2");
        album.setRemoteName("content://media/1", "first.jpg"); album.setRemoteName("content://media/2", "second.jpg");
        assertTrue(album.removePhotoReference("content://media/1"));
        assertFalse(album.removePhotoReference("content://media/1"));
        assertNull(album.recordedRemoteName("content://media/1"));
        assertEquals("second.jpg", album.recordedRemoteName("content://media/2"));
        assertEquals(2, album.getFotos_tomadas()); // historical capture count is not the grid count
        assertEquals(4164294007417506670L, ObjectStreamClass.lookup(Album.class).getSerialVersionUID());
        Context context = RuntimeEnvironment.getApplication(); Pendientes.upsert(inspection, context);
        Inspeccion restored = StoredInspections.read(context, StoredInspections.DRAFTS).get(0);
        assertEquals(album.getId(), restored.getFotosGenerales().getId());
        assertEquals(album.getFotos(), restored.getFotosGenerales().getFotos());
        // Export the same-format draft for the actual pre-gallery rollback reader check.
        String export = System.getProperty("gallery.rollback.fixture");
        if (export != null) try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(export))) {
            out.writeObject(restored);
        }
    }
}
