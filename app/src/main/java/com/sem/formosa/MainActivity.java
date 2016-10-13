package com.sem.formosa;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
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
import com.hkm.ui.processbutton.iml.ActionProcessButton;
import com.klinker.android.link_builder.Link;
import com.klinker.android.link_builder.LinkBuilder;
import com.maksim88.passwordedittext.PasswordEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ProgressGenerator.OnCompleteListener {

    ActionProcessButton login_button;
    RequestQueue MyRequestQueue;
    int thisWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    String usuario, contraseña, saldo, fecha_saldo;
    EditText input_usuario;
    PasswordEditText input_contraseña;
    SharedPreferences.Editor editor;
    SharedPreferences pref;
    TextView texto1;
  private static final String TEXT =
            "Here is an example link <a href=\"https://formosa.dat.cespi.unlp.edu.ar/#/login\">\"formosa.dat.cespi.unlp.edu.ar/#/login\"</a>." +
                    "To show it alongside other LinkBuilder functionality, lets highlight this.";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Link sem = new Link("https://formosa.dat.cespi.unlp.edu.ar/#/login");
        sem.setTextColor(Color.parseColor("#00BCD4"));
        sem.setHighlightAlpha(.4f);
        sem.setOnClickListener(new Link.OnClickListener() {
            @Override
            public void onClick(String clickedText) {
                Intent intent= new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.formosa.dat.cespi.unlp.edu.ar"));
                startActivity(intent);
            }
        });
        texto1 = (TextView) findViewById(R.id.demo1);
        texto1.setText(Html.fromHtml(TEXT));
        LinkBuilder.on(texto1)
                .addLink(sem)
                .build();
        Typeface asenine = Typeface.createFromAsset(getAssets(), "fonts/asenine.ttf");
        texto1.setTypeface(asenine);
        pref = getApplicationContext().getSharedPreferences("SEM_SALDO", MODE_PRIVATE);
        usuario=pref.getString("usuario", null);
        contraseña=pref.getString("contraseña", null);
        saldo=pref.getString("saldo", null);
        fecha_saldo=pref.getString("fecha_saldo", null);
        input_usuario = (EditText) findViewById(R.id.usuario);
        input_contraseña = (PasswordEditText) findViewById(R.id.contraseña);
        final ProgressGenerator progressGenerator = new ProgressGenerator(this);
        login_button = (ActionProcessButton) findViewById(R.id.login_button);
        login_button.setMode(ActionProcessButton.Mode.ENDLESS);
        login_button.setOnClickNormalState(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor = pref.edit();
                editor.putString("usuario", "null");
                editor.putString("contraseña", "null");
                editor.commit();
                iniciarSesion();
                progressGenerator.start(login_button);
                login_button.setEnabled(false);
            }
        }).build();
        startService();
       // checkLogin(usuario);
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
            Intent intent = new Intent(this,SaldoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(bundle);
            finish();
            startActivity(intent);
        }
    }

    public void startService() {
        Intent msgIntent = new Intent(MainActivity.this, UpdateService.class);
        startService(msgIntent);
    }

    public void iniciarSesion(){
        MyRequestQueue = Volley.newRequestQueue(this);
        String url = "https://core-formosa.dat.cespi.unlp.edu.ar/mobile/global";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
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
                    messageError = sem.getString("messageError");
                    saldo = sem.getString("saldo");
                    editor = pref.edit();
                    editor.putString("saldo", saldo);
                    editor.putString("fecha_saldo", date);
                    editor.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
              if(errorCode=="20"){
                  Toast.makeText(getApplicationContext(), messageError, Toast.LENGTH_SHORT).show();
                  login_button.setEnabled(true);
              }else{
                  AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                  ComponentName thisWidget = new ComponentName(getApplicationContext(),MyWidgetProvider.class);
                  int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                  for (int widgetId : allWidgetIds) {
                  RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
                  R.layout.widget_layout);
                  remoteViews.setTextViewText(R.id.update, "$" + saldo);
                  remoteViews.setTextViewText(R.id.fecha_Saldo, date);appWidgetManager.updateAppWidget(widgetId, remoteViews);}
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
                Log.i("errorvolley", "" + error);
                Toast.makeText(getApplicationContext(), "Tiempo de espera agotado. Intente de nuevo", Toast.LENGTH_SHORT).show();
                login_button.setEnabled(true);
                //This code is executed if there is an error.
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("op", "login");
                MyData.put("celular", "pedrotomas");
                MyData.put("password", "pedrotomas16");
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onComplete() {

    }
}
