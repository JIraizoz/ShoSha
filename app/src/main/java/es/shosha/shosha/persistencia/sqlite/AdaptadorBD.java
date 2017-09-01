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
import java.util.Map;
import java.util.TreeMap;

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
    private static final String TB_CHK = "checksums";
    private static final String TB_QR = "codigoQR";
    private static final String ID = "id";
    private static final String NOMBRE = "nombre";
    private static final String USR_EMAIL = "email";
    private static final String USR_MODIF = "modificacion";
    private static final String LST_PROP = "propietario";
    private static final String LST_ESTADO = "estado";
    private static final String ITM_PRECIO = "precio";
    private static final String ITM_CANTIDAD = "cantidad";
    private static final String ITM_COMPRADO = "comprado";
    private static final String IDLISTA = "idLista";
    private static final String PPA_IDUSR = "idUsuario";
    private static final String PPA_ACTIVO = "activo";
    private static final String CHK_TABLA = "tabla";
    private static final String CHK_CRC = "crc";
    private static final String QR_IDQR = "idQR";
    private static final String SQL_TRNCTE = "DELETE FROM ";


    private static final String ID_LOG = "USO DE BD";
    public static final String ITM_CANT = "cantidad";
    public static final String ITM_COMPR = "comprado";

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

    @Deprecated
    public long insertarUsuario(int id, String nombre, String email) {
        bdatos.beginTransaction();
        long res;
        try {

            ContentValues valores = new ContentValues();
            valores.put(ID, id);
            valores.put(NOMBRE, nombre);
            valores.put(USR_EMAIL, email);
            bdatos.delete(TB_USUARIO, ID + " = " + id, null);
            res = bdatos.insert(TB_USUARIO, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public long insertarUsuario(String nombre, String email) {
        bdatos.beginTransaction();
        long res;
        try {

            ContentValues valores = new ContentValues();
            valores.put(NOMBRE, nombre);
            valores.put(USR_EMAIL, email);
            bdatos.delete(TB_USUARIO, NOMBRE + "=" + nombre + " AND " + USR_EMAIL + "=" + email, null);
            res = bdatos.insert(TB_USUARIO, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public long insertarUsuario(Usuario usuario) {
        bdatos.beginTransaction();
        long res;
        try {

            ContentValues valores = new ContentValues();
            valores.put(ID, usuario.getId());
            valores.put(NOMBRE, usuario.getNombre());
            valores.put(USR_EMAIL, usuario.getEmail());
            bdatos.delete(TB_USUARIO, ID + " = " + usuario.getId(), null);
            res = bdatos.insert(TB_USUARIO, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public long insertarLista(int id, String nombre, Usuario propietario, String estado) {
        bdatos.beginTransaction();
        long res;
        try {
            ContentValues valores = new ContentValues();
            valores.put(ID, id);
            valores.put(NOMBRE, nombre);
            valores.put(LST_PROP, propietario.getId());
            valores.put(LST_ESTADO, estado);
            bdatos.delete(TB_LISTA, ID + " = " + id, null);
            res = bdatos.insert(TB_LISTA, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public long insertarLista(Lista l) {
        bdatos.beginTransaction();
        long res;
        try {
            ContentValues valores = new ContentValues();
            valores.put(ID, l.getId());
            valores.put(NOMBRE, l.getNombre());
            valores.put(LST_PROP, l.getPropietario().getId());
            valores.put(LST_ESTADO, l.isEstado());
            bdatos.delete(TB_LISTA, ID + " = " + l.getId(), null);

            ContentValues cv = new ContentValues();
            cv.put(QR_IDQR, l.getCodigoQR());
            cv.put(IDLISTA, l.getId());

            bdatos.replace(TB_QR, null, cv);
            res = bdatos.insert(TB_LISTA, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public long insertarItem(int id, String nombre, double precio, int idLista) {
        bdatos.beginTransaction();
        long res = 0;
        try {
            ContentValues valores = new ContentValues();
            valores.put(ID, id);
            valores.put(NOMBRE, nombre);
            valores.put(ITM_PRECIO, precio);
            valores.put(IDLISTA, idLista);

            long l = bdatos.replace(TB_ITEM, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public long insertarItem(int id, String nombre, double precio, int idLista, int cantidad, boolean comprado) {
        bdatos.beginTransaction();
        long res = 0;
        try {
            ContentValues valores = new ContentValues();
            valores.put(ID, id);
            valores.put(NOMBRE, nombre);
            valores.put(ITM_PRECIO, precio);
            valores.put(IDLISTA, idLista);
            valores.put(ITM_CANT, cantidad);
            valores.put(ITM_COMPR, comprado);

            long l = bdatos.replace(TB_ITEM, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }
    public long insertarItem(Item i) {
        bdatos.beginTransaction();
        long res = 0;
        try {
            ContentValues valores = new ContentValues();
            valores.put(ID, i.getId());
            valores.put(NOMBRE, i.getNombre());
            valores.put(ITM_PRECIO, i.getPrecio());
            valores.put(IDLISTA, i.getIdLista());
            valores.put(ITM_CANT, i.getCantidad());
            valores.put(ITM_COMPR, i.isComprado());

            long l = bdatos.replace(TB_ITEM, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public long insertarParticipa(int idUsr, int idLista, boolean activo) {
        bdatos.beginTransaction();
        long res;
        try {
            ContentValues valores = new ContentValues();

            valores.put(IDLISTA, idLista);
            valores.put(PPA_IDUSR, idUsr);
            valores.put(PPA_ACTIVO, activo ? 1 : 0);
            /*
            bdatos.delete(TB_PARTICIPA, IDLISTA + " = " + idLista + " AND " + PPA_IDUSR + " = " + idUsr, null);
            res = bdatos.insert(TB_PARTICIPA, null, valores);
            */
            res = bdatos.replace(TB_PARTICIPA, null, valores);

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }

        return res;
    }

    public void insertarChecksum(Map<String, Double> mapaRemoto) {
        bdatos.beginTransaction();
        try {
            ContentValues valores = new ContentValues();

            for (String k : mapaRemoto.keySet()) {
                valores.put(CHK_TABLA, k);
                valores.put(CHK_CRC, mapaRemoto.get(k));
                bdatos.replace(TB_CHK, null, valores);
            }
            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }
    }

    public void insertaQR(String codigoQR, int idLista) {
        bdatos.beginTransaction();
        try {
            ContentValues valores = new ContentValues();
            valores.put(QR_IDQR, codigoQR);
            valores.put(IDLISTA, idLista);
            bdatos.replace(TB_QR, null, valores);
            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }
    }

    public List<Lista> obtenerListas(int idUsuario) {

        String sql = "SELECT distinct l.*,c.idQR " +
                "FROM codigoQR c JOIN lista l on c.idLista=l.id LEFT JOIN participa p ON l.id=p.idLista " +
                "WHERE (l.propietario=" + idUsuario + " AND l.estado=1) OR (p.idUsuario=" + idUsuario + " AND p.activo=1)";

        //Cursor de las listas del usuario idUsuario
        //Cursor c = bdatos.query(false, TB_LISTA, null, "propietario='" + idUsuario + "'", null, null, null, null, null);
        Cursor c = bdatos.rawQuery(sql, null);

        Usuario u = this.obtenerUsuario(idUsuario);

        Lista l = null;
        List<Lista> aux = new ArrayList<Lista>();

        if (c.moveToFirst()) {
            do {
                l = new Lista();
                l.setId(c.getInt(0));
                l.setNombre(c.getString(1));
                l.setEstado(c.getString(3).equals("1"));
                l.setCodigoQR(c.getString(4));
                int usrProp = c.getInt(2);
                if (usrProp == idUsuario && u != null) {
                    l.setPropietario(u);
                } else if (usrProp != idUsuario) {
                    l.setPropietario(this.obtenerUsuario(usrProp));
                }

                l.setListaItems(this.obtenerItems(l.getId()));

                l.setParticipantes(this.getParticipantes(l.getId()));

                aux.add(l);

            } while (c.moveToNext());
        }
        c.close();

        return aux;
    }

    public List<Lista> obtenerListas(Usuario u) {
        //Cursor de las listas del usuario idUsuario
        Cursor c = bdatos.query(false, TB_LISTA, null, "propietario=" + u.getId(), null, null, null, null, null);

        Lista l = null;
        List<Lista> aux = new ArrayList<Lista>();

        if (c.moveToFirst()) {
            do {
                l = new Lista();
                l.setId(c.getInt(0));
                l.setNombre(c.getString(1));
                l.setEstado(c.getString(3).equals("1"));
                int usrProp = c.getInt(2);
                if (usrProp == u.getId()) {
                    l.setPropietario(u);
                } else {
                    l.setPropietario(this.obtenerUsuario(usrProp));
                }

                l.setListaItems(this.obtenerItems(l.getId()));

                l.setParticipantes(this.getParticipantes(l.getId()));

                aux.add(l);

            } while (c.moveToNext());
        }
        c.close();
        return aux;
    }

    public Lista obtenerLista(int idLista, int idUsuario) {
        String sql = "SELECT DISTINCT l.*, c.idQR " +
                "FROM codigoQR c JOIN lista l on c.idLista=l.id LEFT JOIN participa p ON l.id=p.idLista " +
                "WHERE l.id=" + idLista + " AND ((l.propietario=" + idUsuario + " AND l.estado=1) OR (p.idUsuario=" + idUsuario + " AND p.activo=1))";
        Cursor c = bdatos.rawQuery(sql, null);
        Usuario u = this.obtenerUsuario(idUsuario);
        Lista l = new Lista();
        if (c.moveToFirst()) {
            l.setId(c.getInt(0));
            l.setNombre(c.getString(1));
            l.setEstado(c.getString(3).equals("1"));
            l.setCodigoQR(c.getString(4));
            int usrProp = c.getInt(2);
            if (usrProp == idUsuario && u != null) {
                l.setPropietario(u);
            } else if (usrProp != idUsuario) {
                l.setPropietario(this.obtenerUsuario(usrProp));
            }
            l.setListaItems(this.obtenerItems(l.getId()));
            l.setParticipantes(this.getParticipantes(l.getId()));
        }

        c.close();
        return l;
    }

    public int obtenerIdListaQR(String codigo) {
//        Cursor c = bdatos.query(false, TB_QR, null, QR_IDQR + "=" + codigo, null, null, null, null, null);
        Cursor c = bdatos.query(true, TB_QR, null, QR_IDQR + "=?", new String[]{codigo}, null, null, null, null);
        int i = 0;

        if (c.moveToFirst()) {
            do {
                i = c.getInt(c.getColumnIndex(IDLISTA));
            } while (c.moveToNext());
        }
        return i;
    }

    public Usuario obtenerUsuario(int id) {
        Cursor c = bdatos.query(false, TB_USUARIO, null, "id=" + id, null, null, null, null, null);
        Usuario u = null;

        if (c.moveToFirst()) {
            do {
                u = new Usuario(c.getInt(0), c.getString(1), c.getString(2));
            } while (c.moveToNext());
        }
        return u;
    }

    public List<Item> obtenerItems(int idLista) {

        Cursor c = bdatos.query(false, TB_ITEM, null, "idLista=" + idLista, null, null, null, null, null);
        Item i = null;
        List<Item> aux = new ArrayList<Item>();
        if (c.moveToFirst()) {
            do {
                i = new Item(c.getInt(0), c.getString(1), c.getDouble(2), idLista);
                i.setCantidad(c.getInt(4));
                i.setComprado(c.getInt(5) == 1);
                aux.add(i);
            } while (c.moveToNext());
        }
        c.close();
        return aux;
    }

    public Item obtenerItem(int idItem) {

        Cursor c = bdatos.query(false, TB_ITEM, null, "id=" + idItem, null, null, null, null, null);
        Item i = null;
        if (c.moveToFirst()) {
            do {
                i = new Item(c.getInt(0), c.getString(1), c.getDouble(2), c.getInt(3));
                i.setCantidad(c.getInt(4));
                i.setComprado(c.getInt(5) == 1);
            } while (c.moveToNext());
        }
        c.close();
        return i;
    }

    public List<Usuario> getParticipantes(int idLista) {
        //Cursor cursor = bdatos.rawQuery("SELECT * FROM " + TB_PARTICIPA + " WHERE " + IDLISTA + "='" + idLista + "'", null);
        Cursor cursor = bdatos.query(false, TB_PARTICIPA, null, IDLISTA + "=?", new String[]{String.valueOf(idLista)}, null, null, null, null);

        List<Usuario> aux = new ArrayList<Usuario>();

        if (cursor.moveToFirst()) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                aux.add(this.obtenerUsuario(cursor.getInt(cursor.getColumnIndex(PPA_IDUSR))));
            } while (cursor.moveToNext());
        }

        return aux;
    }

    public Map<String, Double> obtenerChecksum() {
        Map<String, Double> mapa = new TreeMap<>();

        Cursor c = bdatos.query(false, TB_CHK, null, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                mapa.put(c.getString(0), c.getDouble(1));
            } while (c.moveToNext());
        }
        c.close();

        return mapa;
    }

    public String obtenerQR(int idLista) {

        String codigo = "";

        Cursor c = bdatos.query(true, TB_QR, null, IDLISTA + "=?", new String[]{String.valueOf(idLista)}, null, null, null, null);

        if (c.moveToFirst()) {
            do {
                codigo = c.getString(0);
            } while (c.moveToNext());
        }
        c.close();
        return codigo;
    }

    public void updateUsuario(Usuario u) {
        bdatos.beginTransaction();
        try {

            ContentValues cv = new ContentValues();
            cv.put(NOMBRE, u.getNombre());
            cv.put(USR_EMAIL, u.getEmail());

            bdatos.update(TB_USUARIO, cv, "id=?", new String[]{String.valueOf(u.getId())});

            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }
    }

    public void updateLista(Lista lista, String nombre) {
        bdatos.beginTransaction();
        try {

            ContentValues cv = new ContentValues();
            cv.put(NOMBRE, lista.getNombre());

            bdatos.update(TB_LISTA, cv, "id=" + lista.getId(), null);
            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }
    }
    public long comprarItem(int idProducto) {
        bdatos.beginTransaction();
        long res=-1;
        try {
            ContentValues cv = new ContentValues();
            cv.put(ITM_COMPR,1);

            res=bdatos.update(TB_ITEM, cv, "id=" + idProducto, null);
            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }
        return res;
    }

    public void updateItem(Item i) {
        bdatos.beginTransaction();
        try {

            ContentValues valores = new ContentValues();
            valores.put(NOMBRE, i.getNombre());
            valores.put(ITM_PRECIO, i.getPrecio());
            valores.put(IDLISTA, i.getIdLista());
            valores.put(ITM_CANT, i.getCantidad());
            valores.put(ITM_COMPR, i.isComprado());

            bdatos.update(TB_ITEM, valores, ID + "=?", new String[]{String.valueOf(i.getId())});
            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }
    }

    public long eliminarLista(int id, Usuario usuario) {
        bdatos.beginTransaction();
        long res = -1;
        try {
            Cursor cursor = bdatos.rawQuery("SELECT * FROM " + TB_LISTA + " WHERE " + ID + "=" + id, null);
            if (cursor.moveToFirst()) {
                if (cursor.getInt(2) == (usuario.getId())) { //Si el usuario es propietario
                    ContentValues valores = new ContentValues();
                    valores.put(LST_ESTADO, "0");
                    res = bdatos.update(TB_LISTA, valores, ID + "=" + id, null);
                } else {
                    ContentValues valores = new ContentValues();
                    valores.put(PPA_ACTIVO, "0");
                    res = bdatos.update(TB_PARTICIPA, valores, IDLISTA + "=" + id + " AND " + PPA_IDUSR + " = " + usuario.getId(), null);
                }

//                long qr = eliminarQR(id);
//                if (qr > -1) {
                bdatos.setTransactionSuccessful();
//                } else {
//                    res = -1;
//                    Log.e("Método eliminarLista", "La transacción no se realizó correctamente", new Exception("Resultado QR: " + qr + " / Resultado método: " + res));
//                }
            }
        } finally {
            bdatos.endTransaction();
        }
        return res;
    }

    /**
     * Se elimina la lista dada de la base de datos local
     *
     * @param l Lista a borrar
     * @return resultado del borrado
     */
    public long eliminarLista(Lista l) {
        bdatos.beginTransaction();
        long res = -1;
        try {
            res = bdatos.delete(TB_LISTA, ID + "=" + l.getId(), null);

            //                long qr = eliminarQR(id);
//                if (qr > -1) {
            bdatos.setTransactionSuccessful();
//                } else {
//                    res = -1;
//                    Log.e("Método eliminarLista", "La transacción no se realizó correctamente", new Exception("Resultado QR: " + qr + " / Resultado método: " + res));
//                }

        } finally {
            bdatos.endTransaction();
        }
        return res;
    }

    public long eliminarLista(int id, int idUsuario) {
        bdatos.beginTransaction();
        long res = -1;
        try {
            Cursor cursor = bdatos.rawQuery("SELECT * FROM " + TB_LISTA + " WHERE " + ID + "=" + id, null);
            if (cursor.moveToFirst()) {
                if (cursor.getInt(2) == idUsuario) {
                    ContentValues valores = new ContentValues();
                    valores.put(LST_ESTADO, "0");
                    res = bdatos.update(TB_LISTA, valores, ID + "=" + id, null);
                } else {
                    ContentValues valores = new ContentValues();
                    valores.put(PPA_ACTIVO, "0");
                    res = bdatos.update(TB_PARTICIPA, valores, IDLISTA + "=" + id + " AND " + PPA_IDUSR + " = " + idUsuario, null);
                }


//                long qr = eliminarQR(id);
//                if (qr > -1)
                bdatos.setTransactionSuccessful();
//                else {
//                    res = -1;
//                    Log.e("Método eliminarLista", "La transacción no se realizó correctamente", new Exception("Resultado QR: " + qr + " / Resultado método: " + res));
//                }
            }
        } finally {
            bdatos.endTransaction();
        }
        return res;
    }

    public long eliminarItem(int lista, int item) {
        bdatos.beginTransaction();
        long res = -1;
        try {
            String sql = "SELECT * FROM " + TB_LISTA + " l join " + TB_ITEM + " i on l." + ID + "=i." + IDLISTA + " WHERE l." + ID + "=" + lista + " AND l." + LST_ESTADO + "=1 AND i." + ID + "=" + item;
            Cursor cursor = bdatos.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                res = bdatos.delete(TB_ITEM, ID + "=" + item, null);
                System.out.println("Respuesta eliminar item en local: " + res);
                bdatos.setTransactionSuccessful();
            }
        } finally {
            bdatos.endTransaction();
        }
        return res;
    }

    public long eliminarItem(String idLista, String idItem) {

        bdatos.beginTransaction();
        long res = -1;
        try {
            if (Integer.valueOf(idLista) > 0 && Integer.valueOf(idItem) > 0) {
                res = bdatos.delete(TB_ITEM, "id=? AND idLista=?", new String[]{idItem, idLista});
                bdatos.setTransactionSuccessful();
            }
        } finally {
            bdatos.endTransaction();
        }
        return res;
    }

    public long eliminarUsuario(Usuario u) {
        bdatos.beginTransaction();
        long res = -1;
        try {
            res = bdatos.delete(TB_USUARIO, NOMBRE + "=? AND " + USR_EMAIL + "=?", new String[]{u.getNombre(), u.getEmail()});
            bdatos.setTransactionSuccessful();
        } finally {
            bdatos.endTransaction();
        }
        return res;
    }

    /**
     * Metodo privado para eliminar un código QR de una lista concreta.
     * MÉTODO SIN TRANSACCIONES
     *
     * @param idLista Id de la lísta a eliminar el código
     * @return resultado de la operación de eliminar
     */
    private long eliminarQR(int idLista) {
        long res = -1;
//        bdatos.beginTransaction();
//        try {
        res = bdatos.delete(TB_QR, IDLISTA + "=?", new String[]{String.valueOf(idLista)});
        bdatos.setTransactionSuccessful();
//        } finally {
//            bdatos.endTransaction();
//        }
        return res;
    }

    /**
     * Metodo privado para eliminar un código QR de una lista concreta.
     * MÉTODO SIN TRANSACCIONES
     *
     * @param codigoQR Código QR a eliminar
     * @return resultado de la operación
     */
    private long eliminarQR(String codigoQR) {
        long res = -1;
//        bdatos.beginTransaction();
//        try {
        res = bdatos.delete(TB_QR, QR_IDQR + "=?", new String[]{String.valueOf(codigoQR)});
        bdatos.setTransactionSuccessful();
//        } finally {
//            bdatos.endTransaction();
//        }
        return res;
    }

    public boolean vaciarBaseDatos() {
        bdatos.beginTransaction();
        boolean res = true;

        try {
            bdatos.delete(TB_PARTICIPA, null, null);
            bdatos.delete(TB_ITEM, null, null);
            bdatos.delete(TB_LISTA, null, null);
            bdatos.delete(TB_USUARIO, null, null);
            int i = bdatos.delete(TB_CHK, null, null);

            bdatos.setTransactionSuccessful();
        } catch (Exception e) {
            System.out.println("/!\\ --> Error vaciado tabla");
            System.out.println("/!\\     Traza: " + e.getStackTrace().toString());
            res = true;
        } finally {
            bdatos.endTransaction();
        }
        return res;
    }
}