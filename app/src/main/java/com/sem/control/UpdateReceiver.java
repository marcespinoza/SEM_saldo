package com.sem.control;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import java.util.Date;
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

        Log.i("action", "action"+intent.getAction());
        if ((intent.getAction().equals("android.intent.action.SCREEN_OFF"))||(intent.getAction().equals("android.intent.action.SCREEN_ON"))||(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))) {

            if(NetworkUtils.isConnected(context)){
           checkEstacionamiento(context);}
        }
    }



    public void checkEstacionamiento(final Context context){
        if((pref.getString("usuario", null))!=null) {
            pref = context.getSharedPreferences("SEM_SALDO", context.MODE_PRIVATE);
            MyRequestQueue = Volley.newRequestQueue(context);
            String url = pref.getString("url_municipio","");
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject sem = null;
                    JSONObject sem_extra = null;
                    String errorCode = null;
                    String messageError = null;
                    String saldo = null;
                    String extra = null;
                    try {
                        sem = new JSONObject(response);
                        errorCode = sem.getString("errorCode");
                        messageError = sem.getString("messageError");
                        saldo = sem.getString("saldo");

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (errorCode.equals("8")) {
                        //No tiene estacionamiento en curso
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("contador", 1);
                        editor.commit();
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancel(1);
                        Intent intent = new Intent(context, AlarmReceiver.class);
                        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                        alarmManager.cancel(sender);
                    } else if (errorCode.equals("2")) {
                        //Tiene estacionamiento en curso
                        try {
                            sem_extra = new JSONObject(sem.getString("extra"));
                            extra = sem_extra.getString("hora");
                            Log.i("post","extra"+extra.substring(6, 11));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        String str = sdf.format(new Date());
                        editor = pref.edit();
                        editor.putString("tiempo_inicio", extra.substring(6, 11));
                        editor.putInt("contador", getTimeDifferance(extra.substring(6, 11), str));
                        editor.commit();
                        long ct = System.currentTimeMillis();
                        AlarmManager mgr=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                        Intent intenti = new Intent(context,AlarmReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intenti, PendingIntent.FLAG_CANCEL_CURRENT);
                        mgr.setInexactRepeating(AlarmManager.RTC, ct, 60000, pendingIntent);
                    }
                        String check_saldo = pref.getString("check_saldo", "0");
                        if (Float.parseFloat(saldo) < Float.parseFloat(check_saldo)&&Float.parseFloat(saldo)!=0) {
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

    public static int getTimeDifferance(String startTime,String endTime){
        try{
            Date time1 = new SimpleDateFormat("HH:mm").parse(endTime);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);

            Date time2 = new SimpleDateFormat("HH:mm").parse(startTime);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);

            Date x = calendar1.getTime();
            Date xy = calendar2.getTime();
            long diff = x.getTime() - xy.getTime();
            int diffMinutes = (int) (diff / (60 * 1000));

            int diffHours = diffMinutes / 60;
            System.out.println("diff hours" + diffHours);
            if(diffMinutes>59){
                diffMinutes = diffMinutes%60;
            }
            String totalDiff = diffHours+":"+diffMinutes;
            return diffMinutes;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

}
