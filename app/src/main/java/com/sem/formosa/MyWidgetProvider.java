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
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {


    SharedPreferences pref;
    String usuario, fecha_saldo, saldo;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
     Log.i("alarmita","onupdate");
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


}
