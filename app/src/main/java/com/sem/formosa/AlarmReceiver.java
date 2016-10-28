package com.sem.formosa;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by jaltuzarra on 27/10/2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

    static int NOTIFICATION_ID = 1;
    SharedPreferences pref = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        pref = context.getSharedPreferences("SEM_SALDO", context.MODE_PRIVATE);
        Notification noti = new Notification.Builder(context)
                .setOngoing(true)
                .setContentTitle("Estacionado desde "+pref.getString("tiempo_inicio", "0:0"))
                .setContentText("Transcurren " + pref.getInt("contador", 1) + " minutos")
                .setSmallIcon(R.mipmap.icon_parking)
                .build();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, noti);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("contador",pref.getInt("contador",1)+1);
        editor.commit();
    }
}
