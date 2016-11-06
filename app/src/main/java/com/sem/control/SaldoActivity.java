package com.sem.control;

import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.kyleduo.switchbutton.SwitchButton;
import com.marcoscg.easylicensesdialog.EasyLicensesDialog;

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
    SwitchButton switchButton;

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
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("switch", isChecked);
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
        Bundle bundle;
        bundle = this.getIntent().getExtras();
        String saldo_ = bundle.getString("saldo");
        String ultimo_saldo_ = bundle.getString("fecha_saldo");
        saldo.setText("$" + saldo_);
        ultimo_saldo.setText(ultimo_saldo_);
    }


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
                 easyLicensesDialog.setTitle("SEM Control"); //by default EasyLicensesDialog comes without any title.
                 easyLicensesDialog.setCancelable(true); //true or false
                 easyLicensesDialog.setIcon(R.mipmap.ic_launch); //add an icon to the title
                 easyLicensesDialog.show();
         }

        return super.onOptionsItemSelected(item);
    }


}
