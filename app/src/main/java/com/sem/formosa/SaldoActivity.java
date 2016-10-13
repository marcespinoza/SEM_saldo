package com.sem.formosa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

/**
 * Created by Marcelo on 10/10/2016.
 */
public class SaldoActivity extends AppCompatActivity {

    TextView saldo, ultimo_saldo, texto_1,texto_2,texto_3, texto_4;
    private FloatingActionMenu fab;
    private FloatingActionButton compartir;
    private FloatingActionButton mensaje;
    private FloatingActionButton cerrar_sesion;
    RangeBar range_bar;
    EditText range_saldo;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saldo_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                pref = getApplicationContext().getSharedPreferences("SEM_SALDO", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("check_saldo", rightPinValue);
                editor.commit();
            }

        });
        Typeface asenine = Typeface.createFromAsset(getAssets(), "fonts/asenine.ttf");
        Typeface aliquam = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        Typeface trueno = Typeface.createFromAsset(getAssets(), "fonts/TruenoBd.otf");
        texto_1 = (TextView) findViewById(R.id.texto_1);
        texto_2 = (TextView) findViewById(R.id.texto_2);
        texto_3 = (TextView) findViewById(R.id.texto_3);
        texto_4 = (TextView) findViewById(R.id.texto_4);
        texto_1.setTypeface(trueno);
        texto_2.setTypeface(asenine);
        texto_3.setTypeface(asenine);
        texto_4.setTypeface(asenine);
        cerrar_sesion = (FloatingActionButton) findViewById(R.id.cerrar_sesion);
        cerrar_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SaldoActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.sem.saldo"));
                startActivity(intent);
            }
        });
        saldo = (TextView) findViewById(R.id.saldo);
        ultimo_saldo = (TextView) findViewById(R.id.ultimo_saldo);
        saldo.setTypeface(aliquam);
        ultimo_saldo.setTypeface(aliquam);
        Bundle bundle;
        bundle = this.getIntent().getExtras();
        String saldo_ = bundle.getString("saldo");
        String ultimo_saldo_ = bundle.getString("fecha_saldo");
        saldo.setText("$" + saldo_);
        ultimo_saldo.setText(ultimo_saldo_);
    }



}
