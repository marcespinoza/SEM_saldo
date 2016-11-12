package com.sem.control;

/**
 * Created by Marcelo on 12/10/2016.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class UpdateService extends Service {

   BroadcastReceiver receiver=null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.receiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //Registro un receiver que actualizará el widget cuando el dispositivo prenda y apague
        //pantalla
        receiver = new UpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(receiver, filter);
        long ct = System.currentTimeMillis();
        //Inicio un alarmManager que actualizará el widget cada una hora.
        //Anque el dispositivo no se haya desbloqueado
        AlarmManager mgr=(AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intenti = new Intent(this,MyWidgetProvider.class);
        intenti.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intenti, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.setInexactRepeating(AlarmManager.RTC, ct, 60000*60, pendingIntent);
        return START_STICKY;
    }

}
