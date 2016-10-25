package com.sem.formosa;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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
import com.hkm.ui.processbutton.iml.ActionProcessButton;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.maksim88.passwordedittext.PasswordEditText;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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
                Log.i("entro","a19"+input_contraseña.getText().toString());
                if(input_usuario.getText().toString().length()==0 || input_contraseña.getText().toString().length()==0) {
                    TastyToast.makeText(getApplicationContext(), "Rellena todos los campos!", TastyToast.LENGTH_LONG, TastyToast.WARNING);
                }else{mDialog.show();
                iniciarSesion();}
            }
        });
        checkLogin(usuario);
    }

    private List<Link> getExampleLinks() {
        List<Link> links = new ArrayList<>();

        final Link sem = new Link("https://formosa.dat.cespi.unlp.edu.ar");
        sem.setTextColor(Color.parseColor("#00BCD4"));
        sem.setHighlightAlpha(.4f);
        sem.setOnClickListener(new Link.OnClickListener() {
            @Override
            public void onClick(String clickedText) {
                Log.i("clii","clii");
            }
        });
        links.add(sem);
        return links;
    }

    private void checkLogin(String usuario){
        if(usuario!=null){
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
        String url = "https://core-formosa.dat.cespi.unlp.edu.ar/mobile/global";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mDialog.dismiss();
                Log.i("post",""+response);
                JSONObject sem = null;
                String errorCode = null;
                String messageError = null;
                String saldo = null;
                DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                String date = df.format(Calendar.getInstance().getTime());

                try {
                    sem = new JSONObject(response);
                    errorCode = sem.getString("errorCode");
                    Log.i("entro","a17"+errorCode);
                    messageError = sem.getString("messageError");
                    Log.i("entro","a18"+errorCode);
                    saldo = sem.getString("saldo");
                    Log.i("entro","a19"+errorCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
              if(errorCode.equals("20")){
                  Toast.makeText(getApplicationContext(), messageError, Toast.LENGTH_SHORT).show();
              }else if(errorCode.equals("17")){

                  Toast.makeText(getApplicationContext(), messageError, Toast.LENGTH_SHORT).show();

              }else{
                  editor = pref.edit();
                  editor.putString("saldo", saldo);
                  editor.putString("fecha_saldo", date);
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
                MyData.put("codigoMunicipio", "17");
                MyData.put("agente", "8");
                MyData.put("version", "1.12");//Add the data you'd like to send to the server.
                return MyData;
            };
        };

        MyRequestQueue.add(MyStringRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.


        return super.onOptionsItemSelected(item);
    }

}
