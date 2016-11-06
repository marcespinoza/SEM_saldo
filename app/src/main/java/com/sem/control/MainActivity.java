package com.sem.control;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.maksim88.passwordedittext.PasswordEditText;
import com.sdsmdg.tastytoast.TastyToast;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity  {

    BootstrapButton login_button;
    RequestQueue MyRequestQueue;
    SimpleArcDialog mDialog;
    ArcConfiguration configuration;
    String usuario, contraseña, saldo, fecha_saldo;
    EditText input_usuario;
    PasswordEditText input_contraseña;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    TextView texto1;
    private static final String[] MUNICIPIOS = {
            "Municipio","Escobar","Formosa", "Chascomus", "Ituzaingó","La Plata","Matanza","Moron","Necochea","Pilar","San Martin","San Miguel","San Pedro"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.icon_bar);
        getSupportActionBar().setTitle(null);
        configuration = new ArcConfiguration(getApplicationContext());
        configuration.setColors(new int[]{Color.parseColor("#ff43a047")});
        configuration.setText("Por favor espere..");
        mDialog = new SimpleArcDialog(this);
        mDialog.setConfiguration(configuration);
        SpannableString spannableString = new SpannableString(getString(R.string.inicio_sesion));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://formosa.dat.cespi.unlp.edu.ar/#/login")));
            }
        };
        texto1 = (TextView) findViewById(R.id.demo1);
        spannableString.setSpan(clickableSpan, spannableString.length() - 30,
                spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        texto1.setText(spannableString, TextView.BufferType.SPANNABLE);
        texto1.setMovementMethod(LinkMovementMethod.getInstance());
        Typeface asenine = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        texto1.setTypeface(asenine);
        pref = getApplicationContext().getSharedPreferences("SEM_SALDO", MODE_PRIVATE);
        usuario=pref.getString("usuario", null);
        contraseña=pref.getString("contraseña", null);
        saldo=pref.getString("saldo", "..");
        fecha_saldo=pref.getString("fecha_saldo", "..");
        input_usuario = (EditText) findViewById(R.id.usuario);
        input_contraseña = (PasswordEditText) findViewById(R.id.contraseña);
        login_button = (BootstrapButton) findViewById(R.id.login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input_usuario.getText().toString().length() == 0 || input_contraseña.getText().toString().length() == 0 || pref.getString("municipio", "Municipio").equals("Municipio")) {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.snackbarPosition), "Selecciona Municipio y rellena todos los campos", Snackbar.LENGTH_LONG);
                    View snackBarView = snackbar.getView();
                    final int version = Build.VERSION.SDK_INT;
                    if (version >= 23) {
                        snackBarView.setBackgroundColor(ContextCompat.getColor(getApplication(), R.color.material_red_600));
                    } else {
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.material_red_600));
                    }
                    snackbar.show();
                } else if (NetworkUtils.isConnected(getApplicationContext())) {
                    mDialog.show();
                    iniciarSesion();
                } else {
                    Snackbar snackbar = Snackbar.make(findViewById(R.id.snackbarPosition), "Comprueba si tienes conexión!", Snackbar.LENGTH_LONG);
                    View snackBarView = snackbar.getView();
                    final int version = Build.VERSION.SDK_INT;
                    if (version >= 23) {
                        snackBarView.setBackgroundColor(ContextCompat.getColor(getApplication(), R.color.material_red_600));
                    } else {
                        snackBarView.setBackgroundColor(getResources().getColor(R.color.material_red_600));
                    }
                    snackbar.show();
                }
            }
        });
        MaterialSpinner spinner = (MaterialSpinner) findViewById(R.id.spinner);
        spinner.setItems(MUNICIPIOS);
        spinner.setHint("Municipio");
        spinner.setArrowColor(getResources().getColor(R.color.material_green_600));
        spinner.setSelectedIndex(pref.getInt("index_municipio", 0));
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                editor = pref.edit();
                editor.putString("municipio", item);
                editor.putInt("index_municipio", position);
                editor.commit();
                guardarMunicipio(item);
            }
        });

        checkLogin(usuario);
    }

    //Verifico si esta autenticado y llamo a SaldoActivity.class
    private void checkLogin(String usuario){
        if(usuario!=null){
            //paso los datos al siguiente activity
            Bundle bundle = new Bundle();
            bundle.putString("saldo", saldo);
            bundle.putString("fecha_saldo", fecha_saldo);
            Intent intent = new Intent(this,SaldoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(bundle);
            finish();
            startActivity(intent);
        }
    }

    public void iniciarSesion(){
        MyRequestQueue = Volley.newRequestQueue(this);
        String url = pref.getString("url_municipio","");
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mDialog.dismiss();
                Log.i("post",""+response);
                JSONObject sem = null;
                String errorCode = null;
                String token = null;
                String messageError = null;
                String saldo = null;
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                String date = df.format(Calendar.getInstance().getTime());

                try {
                    sem = new JSONObject(response);
                    errorCode = sem.getString("errorCode");
                    messageError = sem.getString("messageError");
                    saldo = sem.getString("saldo");
                    token = sem.getString("token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //Muestro un mensaje de error si el usuario es incorrecto o el tiempo de espera se ha agotado
              if(errorCode.equals("20")||errorCode.equals("17")){
                  TastyToast.makeText(getApplicationContext(), messageError, TastyToast.LENGTH_LONG, TastyToast.ERROR);

              }else{
                  editor = pref.edit();
                  editor.putString("saldo", saldo);
                  editor.putString("fecha_saldo", date);
                  editor.putString("token",token);
                  editor.putString("usuario", input_usuario.getText().toString());
                  editor.putString("contraseña", input_contraseña.getText().toString());
                  editor.commit();
                  AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                  ComponentName thisWidget = new ComponentName(getApplicationContext(),MyWidgetProvider.class);
                  int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                  for (int widgetId : allWidgetIds) {
                  RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
                  R.layout.widget_layout);
                  remoteViews.setTextViewText(R.id.update, "$" + saldo);
                  remoteViews.setTextViewText(R.id.fecha_Saldo, date);
                  appWidgetManager.updateAppWidget(widgetId, remoteViews);}
                  Bundle bundle = new Bundle();
                  bundle.putString("saldo", saldo);
                  bundle.putString("fecha_saldo", date);
                  Intent intent = new Intent(MainActivity.this, SaldoActivity.class);
                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                     intent.putExtras(bundle);
                      finish();
                      startActivity(intent);
                }
                //This code is executed if the server responds, whether or not the response contains data.
                //The String 'response' contains the server's response.
            }
        }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
            @Override
            public void onErrorResponse(VolleyError error) {
                mDialog.dismiss();
                Log.i("errorvolley", "" + error);
                Toast.makeText(getApplicationContext(), "Tiempo de espera agotado. Intente de nuevo", Toast.LENGTH_SHORT).show();
                login_button.setEnabled(true);
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("op", "login");
                MyData.put("celular", input_usuario.getText().toString());
                MyData.put("password", input_contraseña.getText().toString());
                MyData.put("codigoMunicipio", pref.getString("id_municipio",""));
                MyData.put("agente", "8");
                MyData.put("version", "1.12");//Add the data you'd like to send to the server.
                return MyData;
            };
        };
        MyRequestQueue.add(MyStringRequest);
    }

    private void guardarMunicipio(String Municipio){
        editor = pref.edit();
        switch(Municipio){
            case "Chascomus":
                editor.putString("url_municipio", "http://163.10.41.244/SEMServices/services/wssph.aspx");
                editor.putString("id_municipio", "6");
                break;
            case "Escobar":
                editor.putString("url_municipio", "https://core-escobar.dat.cespi.unlp.edu.ar/mobile/global");
                editor.putString("id_municipio", "13");
                break;
            case "Formosa":
                editor.putString("url_municipio", "https://core-formosa.dat.cespi.unlp.edu.ar/mobile/global");
                editor.putString("id_municipio", "17");
                break;
            case "Ituzaingo":
                editor.putString("url_municipio", "https://core-ituzaingo.dat.cespi.unlp.edu.ar/mobile/global");
                editor.putString("id_municipio", "14");
                break;
            case "La Matanza":
                editor.putString("url_municipio", "https://core.semlamatanza.com.ar/mobile/global");
                editor.putString("id_municipio", "5");
                break;
            case "La Plata":
                editor.putString("url_municipio", "http://200.82.126.106/SEMservices/services/wssph.aspx");
                editor.putString("id_municipio", "1");
                break;
            case "Moron":
                editor.putString("url_municipio", "http://estacionamiento.moron.gob.ar/SEMServices/services/wssph.aspx");
                editor.putString("id_municipio", "7");
                break;
            case "Necochea":
                editor.putString("url_municipio", "https://core-necochea.dat.cespi.unlp.edu.ar/mobile/global");
                editor.putString("id_municipio", "19");
                break;
            case "Pilar":
                editor.putString("url_municipio", "http://estacionamiento.pilar.gov.ar/SEMservices/services/wssph.aspx");
                editor.putString("id_municipio", "3");
                break;
            case "San Martin":
                editor.putString("url_municipio", "https://core-sanmartin.dat.cespi.unlp.edu.ar/mobile/global");
                editor.putString("id_municipio", "10");
                break;
            case "San Miguel":
                editor.putString("url_municipio", "https://core-sanmiguel.dat.cespi.unlp.edu.ar/mobile/global");
                editor.putString("id_municipio", "16");
                break;
            case "San Pedro":
                editor.putString("url_municipio", "https://core-sanpedro.dat.cespi.unlp.edu.ar/mobile/global");
                editor.putString("id_municipio","15");
                break;
            default:
                break;
        }
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        return super.onOptionsItemSelected(item);
    }

}
