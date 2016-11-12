package com.sem.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Marce Espinoza on 26/10/2016.
 */
public class BootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent msgIntent = new Intent(context, UpdateService.class);
        context.startService(msgIntent);
    }
}
