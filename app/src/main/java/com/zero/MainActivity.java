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
hablarTexto("API Key guardada.");
}
});

hablar.setOnClickListener(v -> escuchar());
}

private void escuchar() {
    estado.setText("🎤 Escuchando...");

    Intent intent = new Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
    );

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

protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == 100 &&
            resultCode == RESULT_OK &&
            data != null) {

        ArrayList<String> resultados =
                data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                );

        if (resultados != null && !resultados.isEmpty()) {

            String texto = resultados.get(0);

            estado.setText("Tú: " + texto);

            preguntarGemini(texto);
        }
    }
} {
    estado.setText("🎤 Escuchando...");

    Intent intent = new Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
    );

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

private void preguntarGemini(String pregunta) {

    String clave = preferencias.getString("gemini_key", "");

    if (clave.isEmpty()) {
        estado.setText("Falta la API Key");
        hablarTexto("Primero necesitas guardar tu API Key de Gemini.");
        return;
    }

    estado.setText("🧠 Zero está pensando...");

    new Thread(() -> {
        try {

            String direccion =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-3.5-flash-lite:generateContent?key="
                    + clave;

            URL url = new URL(direccion);

            HttpURLConnection conexion =
                    (HttpURLConnection) url.openConnection();

            conexion.setRequestMethod("POST");
            conexion.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );
            conexion.setDoOutput(true);

            JSONObject parte = new JSONObject();
            parte.put(
                    "text",
                    "Tu nombre es Zero. "
                    + "Eres un asistente personal. "
                    + "Responde siempre en español "
                    + "de forma natural y breve. "
                    + "Usuario: " + pregunta
            );

            JSONArray partes = new JSONArray();
            partes.put(parte);

            JSONObject contenido = new JSONObject();
            contenido.put("parts", partes);

            JSONArray contenidos = new JSONArray();
            contenidos.put(contenido);

            JSONObject datos = new JSONObject();
            datos.put("contents", contenidos);

            OutputStream salida = conexion.getOutputStream();
            salida.write(datos.toString().getBytes("UTF-8"));
            salida.close();

            BufferedReader lector =
                    new BufferedReader(
                            new InputStreamReader(
                                    conexion.getInputStream()
                            )
                    );

            StringBuilder respuesta =
                    new StringBuilder();

            String linea;

            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }

            lector.close();

            JSONObject resultado =
                    new JSONObject(respuesta.toString());

            String textoRespuesta =
                    resultado
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

            runOnUiThread(() -> {
                estado.setText("Zero: " + textoRespuesta);
                hablarTexto(textoRespuesta);
            });

        } catch (Exception e) {

            runOnUiThread(() -> {
                estado.setText("Error con Gemini");
                hablarTexto(
                        "Tuve un problema al comunicarme con Gemini."
                );
            });
        }
    }).start();
}
private void hablarTexto(String texto) {

    if (voz != null) {
        voz.speak(
                texto,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "zero"
        );
    }
}

@Override
protected void onDestroy() {

    if (voz != null) {
        voz.stop();
        voz.shutdown();
    }

    super.onDestroy();
}
}
               
