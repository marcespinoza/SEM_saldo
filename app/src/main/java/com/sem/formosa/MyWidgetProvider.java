package com.sem.formosa;

/**
 * Created by Marcelo on 10/10/2016.
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

    public static String CLOCK_WIDGET_UPDATE = "com.eightbitcloud.example.widget.8BITCLOCK_WIDGET_UPDATE";

    SharedPreferences pref;
    String saldo, fecha_saldo;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        pref = context.getSharedPreferences("SEM_SALDO", 0);
        saldo=pref.getString("saldo", null);
        fecha_saldo=pref.getString("fecha_saldo", null);

        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                MyWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);
            if(saldo==null){
                 remoteViews.setTextViewText(R.id.update, "Sin datos");
                remoteViews.setTextViewText(R.id.fecha_Saldo, "...");}
            else{
                remoteViews.setTextViewText(R.id.update, "$" + saldo);
                remoteViews.setTextViewText(R.id.fecha_Saldo, fecha_saldo);
            }
            Intent intent = new Intent(context, MyWidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);


        if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {

            Notification noti = new Notification.Builder(context)
                    .setContentTitle("Estacionamiento medido ")
                    .setContentText("Cargar credito")
                    .setSmallIcon(R.mipmap.ic_saldo)
                    .build();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, noti);
            Log.i("alarmita","widget");
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
            String date = df.format(Calendar.getInstance().getTime());
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);
        remoteViews.setTextViewText(R.id.update, date);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
        for (int appWidgetID : ids) {
            appWidgetManager.updateAppWidget(appWidgetID, remoteViews);
        }
          }
      }



}
