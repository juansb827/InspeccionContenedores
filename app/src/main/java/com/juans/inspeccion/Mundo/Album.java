package com.juans.inspeccion.Mundo;

import android.content.Context;
import com.juans.inspeccion.Mundo.Storage.PhotoStore;
import com.juans.inspeccion.Mundo.Storage.MediaPhotoStore;
import com.juans.inspeccion.Interfaz.MainActivity;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Album implements Serializable {
    // Generated from the released d9c52d3 sources.
    private static final long serialVersionUID = 4164294007417506670L;
    private int fotos_tomadas;
    private ArrayList<String> fotos = new ArrayList<>();
    private String id;
    private HashMap<String, String> remoteNames = new HashMap<>();

    public String getId() {
        if (id == null) id = UUID.randomUUID().toString();
        return id;
    }
    public ArrayList<String> getFotos() { return fotos; }
    public void setFotos(ArrayList<String> fotos) { this.fotos = fotos; }
    public int getFotos_tomadas() { return fotos_tomadas; }
    public void setFotos_tomadas(int count) { fotos_tomadas = count; }

    public void agregarFoto(String reference) throws IOException {
        if (reference == null || !reference.startsWith("content://")) throw new IOException("Importe las fotos antiguas desde Configuración");
        if (!fotos.contains(reference)) { fotos.add(reference); fotos_tomadas++; }
    }
    public void setRemoteName(String reference, String name) { remoteNames.put(reference, name); }
    public String remoteName(String reference, PhotoStore store) throws IOException {
        String name = remoteNames.get(reference);
        return name == null ? store.displayName(reference) : name;
    }
    public void replaceReference(String oldReference, String newReference) {
        for (int i = 0; i < fotos.size(); i++) if (fotos.get(i).equals(oldReference)) fotos.set(i, newReference);
        String name = remoteNames.remove(oldReference);
        if (name != null) remoteNames.put(newReference, name);
    }
    public void deletePhoto(Context context, int index) throws IOException {
        String reference = fotos.get(index);
        new MediaPhotoStore(context).delete(reference);
        fotos.remove(index);
        remoteNames.remove(reference);
    }
    public void eliminarFoto(int index) throws IOException { deletePhoto(MainActivity.context, index); }
    /** Clearing a form/damage unlinks photos; it never deletes gallery copies. */
    public void borrarAlbum() throws IOException { fotos.clear(); remoteNames.clear(); }
    public void renombrarFotos(String name) throws IOException {
        for (int i = 0; i < fotos.size(); i++) remoteNames.put(fotos.get(i), name + "_" + i + ".jpg");
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (fotos == null) fotos = new ArrayList<>();
        if (remoteNames == null) remoteNames = new HashMap<>();
        getId();
    }
}
