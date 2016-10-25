package com.sem.formosa;

import android.app.Notification;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.widget.Chronometer;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marcelo on 12/10/2016.
 */
public class UpdateReceiver extends BroadcastReceiver {

    RequestQueue MyRequestQueue;
    SharedPreferences.Editor editor;
   SharedPreferences pref = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        pref = context.getSharedPreferences("SEM_SALDO", context.MODE_PRIVATE);
        if ((intent.getAction().equals("android.intent.action.SCREEN_OFF"))||(intent.getAction().equals("android.intent.action.SCREEN_ON"))) {

            Log.i("reciver", "APP_WIDGET_UPDATE "+intent.getAction());
            updateWidget(context);
        }

    }

    public void updateWidget(final Context context){
        Log.i("shared","shared"+(pref.getString("usuario", null)));
        if((pref.getString("usuario", null))!=null) {
            pref = context.getSharedPreferences("SEM_SALDO", context.MODE_PRIVATE);
            MyRequestQueue = Volley.newRequestQueue(context);
            String url = "https://core-formosa.dat.cespi.unlp.edu.ar/mobile/global";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("post", "" + response);
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
                    if (errorCode == "20") {
                        Toast.makeText(context, messageError, Toast.LENGTH_SHORT).show();
                    } else if (errorCode == "20") {
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
                    //This code is executed if there is an error.
                }
            }) {
                protected Map<String, String> getParams() {

                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("op", "login");
                    MyData.put("celular", pref.getString("usuario", null));
                    MyData.put("password", pref.getString("contraseña", null));
                    MyData.put("codigoMunicipio", "17");
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
                 .setUsesChronometer(true).setProgress(100, 0 , true)
                .setContentTitle("Estacionamiento medido ")
                .setContentText("Cargar credito")
                .setSmallIcon(R.mipmap.ic_saldo)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, noti);
    }

}
