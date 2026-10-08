package com.zero;

import android.app.Activity;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    TextView estado;
    EditText apiKey;
    TextToSpeech voz;
    SharedPreferences preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        estado = findViewById(R.id.estado);
        apiKey = findViewById(R.id.apiKey);

        Button guardarKey = findViewById(R.id.guardarKey);
        Button hablar = findViewById(R.id.hablar);

        preferencias = getSharedPreferences("Zero", MODE_PRIVATE);

        String claveGuardada = preferencias.getString("gemini_key", "");
        apiKey.setText(claveGuardada);

        voz = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                voz.setLanguage(new Locale("es", "MX"));
                voz.setSpeechRate(0.9f);
            }
        });

        guardarKey.setOnClickListener(v -> {
            String clave = apiKey.getText().toString().trim();

            if (!clave.isEmpty()) {
                preferencias.edit()
                        .putString("gemini_key", clave)
                        .apply();

                estado.setText("API Key guardada");
               
