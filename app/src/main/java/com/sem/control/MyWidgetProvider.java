package com.sem.control;

/**
 * Created by Marcelo on 10/10/2016.
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MyWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_AUTO_UPDATE = "AUTO_UPDATE";
    SharedPreferences pref;
    String usuario, fecha_saldo, saldo;
    RequestQueue MyRequestQueue;
    SharedPreferences.Editor editor;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        pref = context.getSharedPreferences("SEM_SALDO", 0);
        usuario=pref.getString("usuario", null);
        saldo=pref.getString("saldo", null);
        fecha_saldo=pref.getString("fecha_saldo", null);
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,MyWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget_layout);
            if(usuario==null){
                 remoteViews.setTextViewText(R.id.update, "Inicia sesión");
                remoteViews.setTextViewText(R.id.fecha_Saldo, "...");}
            else{
                remoteViews.setTextViewText(R.id.update, "$" + saldo);
                remoteViews.setTextViewText(R.id.fecha_Saldo, fecha_saldo);
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        if (NetworkUtils.isConnected(context)) {
             updateWidget(context);
                }
       Log.i("action","widget"+intent.getAction());
    }

    public void updateWidget(final Context context){
        pref = context.getSharedPreferences("SEM_SALDO", 0);
        if((pref.getString("usuario", null))!=null) {
            pref = context.getSharedPreferences("SEM_SALDO", context.MODE_PRIVATE);
            MyRequestQueue = Volley.newRequestQueue(context);
            String url = pref.getString("url_municipio","");
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject sem;
                    String errorCode = null;
                    String messageError = null;
                    String saldo = null;
                    String token;
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                    String date = df.format(Calendar.getInstance().getTime());
                    try {
                        sem = new JSONObject(response);
                        token=sem.getString("token");
                        errorCode = sem.getString("errorCode");
                        messageError = sem.getString("messageError");
                        saldo = sem.getString("saldo");
                        editor = pref.edit();
                        editor.putString("token",token);
                        editor.putString("saldo", saldo);
                        editor.putString("fecha_saldo", date);
                        editor.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (errorCode.equals("17")||errorCode.equals("20")) {

                        Toast.makeText(context, messageError, Toast.LENGTH_SHORT).show();

                    } else {
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
                        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
                        for (int widgetId : allWidgetIds) {
                            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                                    R.layout.widget_layout);
                            remoteViews.setTextViewText(R.id.update, "$" + saldo);
                            remoteViews.setTextViewText(R.id.fecha_Saldo, date);
                            appWidgetManager.updateAppWidget(widgetId, remoteViews);
                        }
                        String check_saldo = pref.getString("check_saldo", "0");
                        if (Float.parseFloat(saldo) < Float.parseFloat(check_saldo)) {
                            showNotification(context);
                        }
                    }
                    //This code is executed if the server responds, whether or not the response contains data.
                    //The String 'response' contains the server's response.
                }
            }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("action", "error"+error);
                }
            }) {
                protected Map<String, String> getParams() {

                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("op", "login");
                    MyData.put("celular", pref.getString("usuario", null));
                    MyData.put("password", pref.getString("contraseña", null));
                    MyData.put("codigoMunicipio", pref.getString("id_municipio",""));
                    MyData.put("agente", "8");
                    MyData.put("version", "1.12");//Add the data you'd like to send to the server.
                    return MyData;
                };
            };
            MyRequestQueue.add(MyStringRequest);
        }else{
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
            int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            for (int widgetId : allWidgetIds) {
                RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.widget_layout);
                remoteViews.setTextViewText(R.id.update, "Inicia sesión");
                remoteViews.setTextViewText(R.id.fecha_Saldo, "..");
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }
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
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        super.onDeleted(context, appWidgetIds);
        Log.i("MyTag", "onDeleted");
    }

    @Override
    public void onDisabled(Context context)
    {
        super.onDisabled(context);
        Log.i("MyTag", "onDisabled");
    }

    @Override
    public void onEnabled(Context context)
    {
        super.onEnabled(context);
        Log.i("MyTag", "enabled");
        Intent msgIntent = new Intent(context, UpdateService.class);
        context.startService(msgIntent);
    }

}
