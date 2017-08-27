package com.juans.inspeccion.Mundo.Data;

import android.content.res.Resources;

import com.juans.inspeccion.ConnectionException;
import com.juans.inspeccion.DataBaseException;
import com.juans.inspeccion.Mundo.DAO;
import com.juans.inspeccion.Mundo.Usuario;
import com.juans.inspeccion.R;

import java.util.HashMap;

/**
 * Created by Juan on 02/11/2015.
 */
public class UsuarioDataSource {

    public static Usuario findUserByid(int id,Resources res) throws ConnectionException {
        String consulta="SELECT * FROM TBINSPECTORES WHERE CCODINSPECTOR="+id;
        HashMap<String,String> mapa = DAO.getInstance().generarHashConConsulta(consulta);
        if(mapa.size()==0){
            return null;
        }
        Usuario usuario=new Usuario();


        usuario.setId(Integer.parseInt(mapa.get(res.getString(R.string.CCODINSPECTOR))));
        usuario.setNombre(mapa.get(res.getString(R.string.CNOMINSPECTOR)));
        usuario.setPatio(mapa.get(res.getString(R.string.CEN_COSTO)));
        //todo
        String habilitado=mapa.get(res.getString(R.string.NHABILITADO));
        boolean hab=false;
        if("1".equals(habilitado))
        {
            hab=true;
        }
        usuario.setHabilitado(hab);
        return usuario;

    }

}
