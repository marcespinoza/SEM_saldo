package com.sem.control;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.appyvet.rangebar.RangeBar;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.kyleduo.switchbutton.SwitchButton;
import com.marcoscg.easylicensesdialog.EasyLicensesDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marcelo on 10/10/2016.
 */
public class SaldoActivity extends AppCompatActivity {

    TextView texto_notificacion, saldo, ultimo_saldo, texto_1,texto_2,texto_3, texto_4;
    private FloatingActionMenu fab;
    private FloatingActionButton compartir;
    private FloatingActionButton mensaje;
    private FloatingActionButton cerrar_sesion;
    RangeBar range_bar;
    EditText range_saldo;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    SwitchButton switchButton;
    RequestQueue MyRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saldo_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.icon_bar);
        getSupportActionBar().setTitle(null);
        pref = getApplicationContext().getSharedPreferences("SEM_SALDO", MODE_PRIVATE);
        fab = (FloatingActionMenu) findViewById(R.id.fabmenu);
        range_saldo = (EditText) findViewById(R.id.range_saldo);
        range_saldo.setText("$ "+ pref.getString("check_saldo", "0"));
        range_saldo.setKeyListener(null);
        range_bar = (RangeBar) findViewById(R.id.rangebar);
        range_bar.setSeekPinByValue(Float.parseFloat(pref.getString("check_saldo", "0")));
        range_bar.setConnectingLineColor(Color.parseColor("#ff81c784"));
        range_bar.setSelectorColor(Color.parseColor("#ff388e3c"));
        range_bar.setPinColor(Color.parseColor("#ff388e3c"));
        range_bar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                range_saldo.setText("$ "+rightPinValue);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("check_saldo", rightPinValue);
                editor.commit();
            }

        });
        Typeface asenine = Typeface.createFromAsset(getAssets(), "fonts/asenine.ttf");
        Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        switchButton = (SwitchButton) findViewById(R.id.notification_switch);
        switchButton.setChecked(pref.getBoolean("switch", false));
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String str = sdf.format(new Date());
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("switch", isChecked);
                editor.putString("switch_date", str);
                editor.commit();
            }
        });
        texto_notificacion = (TextView) findViewById(R.id.notification_text);
        texto_2 = (TextView) findViewById(R.id.texto_2);
        texto_3 = (TextView) findViewById(R.id.texto_3);
        texto_4 = (TextView) findViewById(R.id.texto_4);
        texto_notificacion.setTypeface(asenine);
        texto_2.setTypeface(asenine);
        texto_3.setTypeface(asenine);
        texto_4.setTypeface(asenine);
        //Al cerrar sesion limpio el shared preferences y actualizo el widget
        cerrar_sesion = (FloatingActionButton) findViewById(R.id.cerrar_sesion);
        cerrar_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.commit();
                Intent intent = new Intent(SaldoActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                updateWidget();
                finish();
                startActivity(intent);
            }
        });
        mensaje = (FloatingActionButton) findViewById(R.id.mensaje);
        mensaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"marceloespinoza00@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "SEM Saldo");
                email.putExtra(Intent.EXTRA_TEXT, "Dejá tu mensaje");
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Elige un cliente :"));
            }
        });
        compartir = (FloatingActionButton) findViewById(R.id.compartir);
        compartir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "SEM Control");
                i.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.sem.control");
                startActivity(Intent.createChooser(i, "choose one"));
            }
        });

        saldo = (TextView) findViewById(R.id.saldo);
        ultimo_saldo = (TextView) findViewById(R.id.ultimo_saldo);
        saldo.setTypeface(roboto);
        ultimo_saldo.setTypeface(roboto);
        //Obtengo los datos enviados desde el MainActivity
        Bundle bundle;
        bundle = this.getIntent().getExtras();
        String saldo_ = bundle.getString("saldo");
        String ultimo_saldo_ = bundle.getString("fecha_saldo");
        saldo.setText("$" + saldo_);
        ultimo_saldo.setText(ultimo_saldo_);
        Intent msgIntent = new Intent(this, UpdateService.class);
        this.startService(msgIntent);
        checkEstacionamiento(this);
    }

   //Actualiza el widget cuando cierra sesión
    public void updateWidget(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
        ComponentName thisWidget = new ComponentName(getApplicationContext(), MyWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
                    R.layout.widget_layout);
            remoteViews.setTextViewText(R.id.update, "Inicia sesión");
            remoteViews.setTextViewText(R.id.fecha_Saldo, "..");
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()){
             case R.id.about:
                 EasyLicensesDialog easyLicensesDialog = new EasyLicensesDialog(this);
                 easyLicensesDialog.setTitle("SEM Control");
                 easyLicensesDialog.setCancelable(true);
                 easyLicensesDialog.setIcon(R.mipmap.ic_launch);
                 easyLicensesDialog.show();
         }
        return super.onOptionsItemSelected(item);
    }

    public void checkEstacionamiento(final Context context){
        if((pref.getString("usuario", null))!=null) {
            pref = context.getSharedPreferences("SEM_SALDO", context.MODE_PRIVATE);
            MyRequestQueue = Volley.newRequestQueue(context);
            String url = pref.getString("url_municipio","");
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject sem;
                    String s_saldo = null;
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                    String date = df.format(Calendar.getInstance().getTime());
                    try {
                        sem = new JSONObject(response);
                        s_saldo = sem.getString("saldo");
                        saldo.setText("$" + s_saldo);
                        ultimo_saldo.setText(date);
                        editor = pref.edit();
                        editor.putString("saldo", s_saldo);
                        editor.putString("fecha_saldo", date);
                        editor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String check_saldo = pref.getString("check_saldo", "0");
                    if (Float.parseFloat(s_saldo) < Float.parseFloat(check_saldo)&&Float.parseFloat(s_saldo)!=0) {
                        showNotification(context);
                    }
                    //This code is executed if the server responds, whether or not the response contains data.
                    //The String 'response' contains the server's response.
                }
            }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error) {
                    //This code is executed if there is an error.
                }
            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("op", "consultarEstado");
                    MyData.put("celular", pref.getString("usuario", null));
                    MyData.put("token", pref.getString("token", null));
                    MyData.put("codigoMunicipio", pref.getString("id_municipio",""));
                    MyData.put("agente", "8");
                    MyData.put("version", "1.12");//Add the data you'd like to send to the server.
                    return MyData;
                };
            };
            MyRequestQueue.add(MyStringRequest);
        }
    }

    private void showNotification (Context context){

        Notification noti = new Notification.Builder(context)
                .setContentTitle("Estacionamiento medido ")
                .setContentText("Cargar credito")
                .setSmallIcon(R.mipmap.ic_saldo)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, noti);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkEstacionamiento(this);
    }

}
