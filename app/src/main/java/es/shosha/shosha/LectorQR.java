package es.shosha.shosha;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import es.shosha.shosha.persistencia.ParticipaFB;

public class LectorQR extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lector_qr);
        //Aparece el botón de atrás
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");

                try {
                    String decodificado = URLDecoder.decode(contents, "UTF-8");
                    JSONObject obj = new JSONObject(decodificado);
                    String lista = obj.getString("idLista");
                    String clave = obj.getString("clave");
                    int idu = MyApplication.getUser().getId();
                    // Añadir al usuario como participante en la lista
                    //Añade participante en bd remota
                    //Comprobar clave de la lista?
                    ParticipaFB.insertaParticipaFB(idu, Integer.valueOf(lista));
                    sigueAnadirParticipante(false, Integer.valueOf(lista));
                } catch (UnsupportedEncodingException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /*Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, 0);*/

                } else {
                    String s = "Debe conceder el permiso para poder utilizar esta funcionalidad";
                    Toast.makeText(getBaseContext(), s, Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void abrirEscaner(View view) {

        // verifico si el usuario dio los permisos para la camara
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } else {
//            Toast.makeText(getBaseContext(), "La aplicación necesita usar la cámara", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }

    }

    public void sigueAnadirParticipante(boolean error, int lista) {
        if (error) {
            Toast.makeText(this, "Error al añadir la lista", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lista añadida", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, ListaProductos.class);//Muestra la lista nueva
            Bundle bundle = new Bundle();
            bundle.putInt("idLista", lista);
            i.putExtras(bundle);
            startActivity(i);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}