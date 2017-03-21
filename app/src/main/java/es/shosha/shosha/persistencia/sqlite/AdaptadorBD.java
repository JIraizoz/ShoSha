package es.shosha.shosha.persistencia.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import es.shosha.shosha.dominio.Item;
import es.shosha.shosha.dominio.Lista;
import es.shosha.shosha.dominio.Usuario;

/**
 * Created by Jesús Iráizoz on 06/03/2017.
 */

public class AdaptadorBD {
    private static final String NOMBRE_BD = "ShoSha";
    private static final int VERSION_BD = 1;

    private static final String TB_USUARIO = "usuario";
    private static final String TB_LISTA = "lista";
    private static final String TB_ITEM = "item";
    private static final String TB_PARTICIPA = "participa";
    private static final String ID = "id";
    private static final String NOMBRE = "nombre";
    private static final String USR_EMAIL = "email";
    private static final String LST_PROP = "propietario";
    private static final String LST_ESTADO = "estado";
    private static final String ITM_PRECIO = "precio";
    private static final String IDLISTA = "idLista";
    private static final String PPA_IDUSR = "idUsuario";
    private static final String PPA_ACTIVO = "activo";

    private static final String ID_LOG = "USO DE BD";

    private final Context contexto;

    private SQLiteDatabase bdatos;

    private AuxiliarBD auxBD;


    public AdaptadorBD(Context contexto) {
        this.contexto = contexto;
        auxBD = new AuxiliarBD(contexto);
    }

    private static class AuxiliarBD extends SQLiteOpenHelper {

        private Context cntx;

        public AuxiliarBD(Context context) {
            super(context, NOMBRE_BD, null, VERSION_BD);
            this.cntx = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                InputStream is = cntx.getAssets().open("ShoSha.sql");
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String leido = "", sql = "";
                while ((leido = br.readLine()) != null) {
                    if (!leido.endsWith(";")) {
                        sql += leido;
                    } else {
                        sql += leido;
                        System.out.println("     > " + sql);
                        db.execSQL(sql);
                        sql = "";
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(ID_LOG, "Actualiza la versión: " + oldVersion + " a la versión: " + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + TB_USUARIO + ";"
                    + "DROP TABLE IF EXISTS " + TB_PARTICIPA + ";"
                    + "DROP TABLE IF EXISTS " + TB_LISTA + ";"
                    + "DROP TABLE IF EXISTS " + TB_ITEM + ";");
            this.onCreate(db);
        }
    }

    public AdaptadorBD open() throws SQLException {
        bdatos = auxBD.getWritableDatabase();
        return this;
    }

    public void close() {
        auxBD.close();
    }

    public long getUltimaModificacion(String idUsr) {
        Cursor c = bdatos.rawQuery("SELECT modificacion FROM usuario WHERE id='" + idUsr + "'", null);
        long l = -1L;
        if (c.moveToFirst())
            l = c.getLong(0);
        c.close();
        return l;
    }

    public void insertarUltimaModificacion(String v) {

    }

    public long insertarUsuario(String id, String nombre, String email) {
        bdatos.beginTransaction();
        long res;
        try {

            ContentValues valores = new ContentValues();
            valores.put(ID, id);
            valores.put(NOMBRE, nombre);
            valores.put(USR_EMAIL, email);
            bdatos.delete(TB_USUARIO, ID + " = '" + id + "'", null);
            res = bdatos.insert(TB_USUARIO, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public long insertarLista(String id, String nombre, Usuario propietario, String estado) {
        bdatos.beginTransaction();
        long res;
        try {
            ContentValues valores = new ContentValues();
            valores.put(ID, id);
            valores.put(NOMBRE, nombre);
            valores.put(LST_PROP, propietario.getId());
            valores.put(LST_ESTADO, estado);
            bdatos.delete(TB_LISTA, ID + " = '" + id + "'", null);
            res = bdatos.insert(TB_LISTA, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public long insertarItem(String id, String nombre, double precio, String idLista) {
        //    bdatos.beginTransaction();
        long res = 0;
    /*    try {*/
        ContentValues valores = new ContentValues();
        valores.put(ID, id);
        valores.put(NOMBRE, nombre);
        valores.put(ITM_PRECIO, precio);
        valores.put(IDLISTA, idLista);

        //System.out.println("+++++++++++++++++++++++++++ "+valores.toString());

        bdatos.delete(TB_ITEM, ID + " = '" + id + "'", null);
        res = bdatos.insertOrThrow(TB_ITEM, null, valores);
        //bdatos.rawQuery("INSERT INTO item VALUES ('"+id+"', '"+nombre+"', '"+precio+"', '"+idLista+"')",null);

  /*          bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }*/

        return res;
    }

    public long insertarParticipa(String idUsr, String idLista, boolean activo) {
        bdatos.beginTransaction();
        long res;
        try {
            ContentValues valores = new ContentValues();

            valores.put(IDLISTA, idLista);
            valores.put(PPA_IDUSR, idUsr);
            valores.put(PPA_ACTIVO, activo ? 1 : 0);
            bdatos.delete(TB_PARTICIPA, IDLISTA + " = '" + idLista + "' AND " + PPA_IDUSR + " = '" + idUsr + "'", null);
            res = bdatos.insert(TB_PARTICIPA, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

/*    public Cursor leerTodos() {
        //return bdatos.query(true,TB_USUARIO,null,null,null,null,null,null,"100");
        return bdatos.rawQuery("SELECT * FROM usuario", null);
    }*/


    public List<Lista> obtenerListas(String idUsuario) {

        //Cursor de las listas del usuario idUsuario
        Cursor c = bdatos.query(false, TB_LISTA, null, "propietario='" + idUsuario + "'", null, null, null, null, null);

        Usuario u = this.obtenerUsuario(idUsuario);

        Lista l = null;
        List<Lista> aux = new ArrayList<Lista>();

        if (c.moveToFirst()) {
            do {
                l = new Lista();
                l.setId(c.getString(0));
                l.setNombre(c.getString(1));
                l.setEstado(c.getString(3).equals("1"));
                String usrProp = c.getString(2);
                if (usrProp.equals(idUsuario) && u != null) {
                    l.setPropietario(u);
                } else if (!usrProp.equals(idUsuario)) {
                    l.setPropietario(this.obtenerUsuario(usrProp));
                }

                l.setListaItems(this.obtenerItems(l.getId()));

                System.out.println("ñññññññññññññññññññññññññññññññññññññññññññ "+l.toString());

                aux.add(l);
                System.out.println("ñññññññññññññññññññññññññññññññññññññññññññ "+aux.size());

            } while (c.moveToNext());
        }
        c.close();
        return aux;
    }

    public List<Lista> obtenerListas(Usuario u) {
        Cursor c = bdatos.query(false, TB_LISTA, null, "propietario='" + u.getId() + "'", null, null, null, null, null);
        Lista l = null;
        List<Lista> aux = new ArrayList<Lista>();
        while (c.moveToNext()) {
            l = new Lista(c.getString(0), c.getString(2), u, c.getString(4).equals("1"), this.obtenerItems(c.getString(0)), null);
            l.setListaItems(this.obtenerItems(l.getId()));
            aux.add(l);
        }
        return aux;
    }

    public Usuario obtenerUsuario(String id) {
        Cursor c = bdatos.query(false, TB_USUARIO, null, "id='" + id + "'", null, null, null, null, null);
        Usuario u = null;

        while (c.moveToNext()) {
            u = new Usuario(c.getString(0), c.getString(1), c.getString(3));
        }
        return u;
    }

    public List<Item> obtenerItems(String idLista) {
        Cursor c = bdatos.query(false, TB_ITEM, null, "idLista='" + idLista + "'", null, null, null, null, null);
        Item i = null;
        List<Item> aux = new ArrayList<Item>();
        System.out.println("*********************************************************** Antes del if de obtener items");
        if (c.moveToFirst()) {
            do {
                i = new Item(c.getString(0), c.getString(1), c.getDouble(2));
                aux.add(i);
                System.out.println("*********************************************************** " + i.toString());
            } while (c.moveToNext());
        }
        c.close();
        return aux;
    }

    public Usuario getUsuario(String id) {
        Cursor cursor = bdatos.rawQuery("SELECT * FROM " + TB_USUARIO + " WHERE id='" + id + "'", null);
        Usuario us = null;
        if (cursor.moveToFirst()) {
            us = new Usuario(cursor.getString(0), cursor.getString(1), cursor.getString(2));
        }
        cursor.close();
        return us;
    }

    public ArrayList<Lista> getListas(String usuario) {
        Cursor cursor = bdatos.rawQuery("SELECT * FROM " + TB_LISTA + " WHERE propietario='" + usuario + "' AND estado = '1'", null);
        ArrayList<Lista> listas = new ArrayList<>();
        Lista l = null;

        if (cursor.moveToFirst()) {
            do {
                l = new Lista(cursor.getString(0), cursor.getString(1), this.getUsuario(cursor.getString(2)), true, new ArrayList<Item>(), new ArrayList<Usuario>());
                listas.add(l);
            } while (cursor.moveToNext());
        }
        cursor.close();
        cursor = bdatos.rawQuery("SELECT * FROM " + TB_LISTA + " WHERE id IN(SELECT idLista FROM " + TB_PARTICIPA + " WHERE idUsuario = '" + usuario + "' AND activo = '1')", null);
        if (cursor.moveToFirst()) {
            do {
                l = new Lista(cursor.getString(0), cursor.getString(1), this.getUsuario(cursor.getString(2)), true, new ArrayList<Item>(), new ArrayList<Usuario>());
                listas.add(l);
            } while (cursor.moveToNext());
        }
        return listas;
    }

    public List<Usuario> getParticipantes(String idLista) {
        //Cursor cursor = bdatos.rawQuery("SELECT * FROM " + TB_PARTICIPA + " WHERE " + IDLISTA + "='" + idLista + "'", null);
        Cursor cursor = bdatos.query(false, TB_PARTICIPA, null, IDLISTA + "=?", new String[]{idLista}, null, null, null, null);

        List<Usuario> aux = new ArrayList<Usuario>();

        System.out.println("++++++++++++++++++++++++++++++++ " + cursor.getCount() + " ##################################");

        if (cursor.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                aux.add(this.getUsuario(cursor.getString(cursor.getColumnIndex(PPA_IDUSR))));
            } while (cursor.moveToNext());
        }

        return aux;
    }

    public long eliminarLista(String id, Usuario usuario) {
        bdatos.beginTransaction();
        long res = -1;
        try {
            Cursor cursor = bdatos.rawQuery("SELECT * FROM " + TB_LISTA + " WHERE " + ID + "='" + id + "'", null);
            if (cursor.moveToFirst()) {
                if (cursor.getString(2).equals(usuario.getId())) {
                    ContentValues valores = new ContentValues();
                    valores.put(LST_ESTADO, "0");
                    res = bdatos.update(TB_LISTA, valores, ID + "='" + id + "'", null);
                } else {
                    ContentValues valores = new ContentValues();
                    valores.put(PPA_ACTIVO, "0");
                    res = bdatos.update(TB_PARTICIPA, valores, IDLISTA + "='" + id + "' AND " + PPA_IDUSR + " = '" + usuario.getId() + "'", null);
                }
                bdatos.setTransactionSuccessful();
            }
        } finally {
            bdatos.endTransaction();
        }
        return res;
    }

    public long eliminarLista(String id, String idUsuario) {
        bdatos.beginTransaction();
        long res = -1;
        try {
            Cursor cursor = bdatos.rawQuery("SELECT * FROM " + TB_LISTA + " WHERE " + ID + "='" + id + "'", null);
            if (cursor.moveToFirst()) {
                if (cursor.getString(2).equals(idUsuario)) {
                    ContentValues valores = new ContentValues();
                    valores.put(LST_ESTADO, "0");
                    res = bdatos.update(TB_LISTA, valores, ID + "='" + id + "'", null);
                } else {
                    ContentValues valores = new ContentValues();
                    valores.put(PPA_ACTIVO, "0");
                    res = bdatos.update(TB_PARTICIPA, valores, IDLISTA + "='" + id + "' AND " + PPA_IDUSR + " = '" + idUsuario + "'", null);
                }
                bdatos.setTransactionSuccessful();
            }
        } finally {
            bdatos.endTransaction();
        }
        return res;
    }
}
