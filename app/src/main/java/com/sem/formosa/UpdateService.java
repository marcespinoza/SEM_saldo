package com.sem.formosa;

/**
 * Created by Marcelo on 12/10/2016.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

public class UpdateService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String URL = "urlpath";
    public static final String FILENAME = "filename";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.vogella.android.service.receiver";
    public static String CLOCK_WIDGET_UPDATE = "com.eightbitcloud.example.widget.8BITCLOCK_WIDGET_UPDATE";

    RequestQueue MyRequestQueue;
    SharedPreferences.Editor editor;
    Handler mHandler = new Handler();

    public UpdateService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i("alarmita","alarmita");
        long ct = System.currentTimeMillis(); //get current time
        AlarmManager mgr=(AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intenti = new Intent(CLOCK_WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intenti, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.setInexactRepeating(AlarmManager.RTC, ct, 7000, pendingIntent);


}

    private void publishResults(String outputPath, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILEPATH, outputPath);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }



}
