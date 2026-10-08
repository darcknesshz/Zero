package com.zero;

import android.app.Activity;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView estado;
    TextToSpeech voz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        estado = findViewById(R.id.estado);
        Button hablar = findViewById(R.id.hablar);

        voz = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                voz.setLanguage(new Locale("es", "MX"));
            }
        });

        hablar.setOnClickListener(v -> escuchar());
    }

    private void escuchar() {
        estado.setText("🎤 Escuchando...");

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "es-MX"
        );

        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super
