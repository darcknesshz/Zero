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
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.net.Uri;
import android.hardware.camera2.CameraManager;

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

        String claveGuardada =
                preferencias.getString("gemini_key", "");

        apiKey.setText(claveGuardada);

        voz = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                voz.setLanguage(new Locale("es", "MX"));
                voz.setSpeechRate(0.9f);
                voz.setPitch(0.75f);
            }
        });

        guardarKey.setOnClickListener(v -> {

            String clave =
                    apiKey.getText().toString().trim();

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

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == 100 &&
                resultCode == RESULT_OK &&
                data != null) {

            ArrayList<String> resultados =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                    );

            if (resultados != null &&
                    !resultados.isEmpty()) {

                String texto = resultados.get(0);

                estado.setText("Tú: " + texto);

                preguntarGemini(texto);
            }
        }
    }

    private void preguntarGemini(String pregunta) {

        String clave =
                preferencias.getString(
                        "gemini_key",
                        ""
                );

        if (clave.isEmpty()) {

            estado.setText("Falta la API Key");

            hablarTexto(
                    "Primero necesitas guardar tu API Key de Gemini."
            );

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
                        (HttpURLConnection)
                                url.openConnection();

                conexion.setRequestMethod("POST");

                conexion.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                conexion.setDoOutput(true);

                String instrucciones =
                        "Eres Zero, un asistente personal de Android. "
                        + "Responde en español. "
                        + "Debes decidir si el usuario quiere una acción "
                        + "del teléfono. "
                        + "SI quiere una acción, responde ÚNICAMENTE con "
                        + "JSON en este formato: "
                        + "{\"accion\":\"NOMBRE\",\"dato\":\"VALOR\"}. "
                        + "Acciones permitidas: "
                        + "ABRIR_YOUTUBE, "
                        + "ABRIR_WHATSAPP, "
                        + "ABRIR_CHROME, "
                        + "ABRIR_GOOGLE, "
                        + "ABRIR_AJUSTES, "
                        + "ABRIR_WIFI, "
                        + "ABRIR_BLUETOOTH, "
                        + "ABRIR_WEB, "
                        + "VOLUMEN_MAS, "
                        + "VOLUMEN_MENOS, "
                        + "SILENCIO, "
                        + "LINterna, "
                        + "LLAMAR. "
                        + "Para ABRIR_WEB, dato debe ser la dirección web. "
                        + "Para LLAMAR, dato debe ser el número. "
                        + "Para acciones sin dato usa una cadena vacía. "
                        + "Si NO es una acción del teléfono, responde "
                        + "ÚNICAMENTE con: "
                        + "{\"accion\":\"RESPONDER\",\"dato\":\"tu respuesta\"}. "
                        + "No pongas texto fuera del JSON. "
                        + "Usuario: "
                        + pregunta;

                JSONObject parte =
                        new JSONObject();

                parte.put(
                        "text",
                        instrucciones
                );

                JSONArray partes =
                        new JSONArray();

                partes.put(parte);

                JSONObject contenido =
                        new JSONObject();

                contenido.put(
                        "parts",
                        partes
                );

                JSONArray contenidos =
                        new JSONArray();

                contenidos.put(contenido);

                JSONObject datos =
                        new JSONObject();

                datos.put(
                        "contents",
                        contenidos
                );

                OutputStream salida =
                        conexion.getOutputStream();

                salida.write(
                        datos.toString()
                                .getBytes("UTF-8")
                );

                salida.close();

                int codigo =
                        conexion.getResponseCode();

                BufferedReader lector;

                if (codigo >= 200 &&
                        codigo < 300) {

                    lector =
                            new BufferedReader(
                                    new InputStreamReader(
                                            conexion.getInputStream()
                                    )
                            );

                } else {

                    lector =
                            new BufferedReader(
                                    new InputStreamReader(
                                    conexion.getErrorStream()
                            )
                    );
                }

                StringBuilder respuesta =
                        new StringBuilder();

                String linea;

                while ((linea =
                        lector.readLine()) != null) {

                    respuesta.append(linea);
                }

                lector.close();

                if (codigo < 200 ||
                        codigo >= 300) {

                    throw new Exception(
                            "Gemini HTTP " + codigo
                    );
                }

                JSONObject resultado =
                        new JSONObject(
                                respuesta.toString()
                        );

                String textoRespuesta =
                        resultado
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text")
                                .trim();

                procesarRespuesta(textoRespuesta);

            } catch (Exception e) {

                runOnUiThread(() -> {

                    estado.setText(
                            "Error con Gemini"
                    );

                    hablarTexto(
                            "Tuve un problema al comunicarme con Gemini."
                    );
                });
            }

        }).start();
    }

    private void procesarRespuesta(
            String respuesta
    ) {

        runOnUiThread(() -> {

            try {

                String limpia =
                        respuesta
                                .replace("```json", "")
                                .replace("```", "")
                                .trim();

                JSONObject orden =
                        new JSONObject(limpia);

                String accion =
                        orden.optString(
                                "accion",
                                "RESPONDER"
                        );

                String dato =
                        orden.optString(
                                "dato",
                                ""
                        );

                ejecutarAccion(
                        accion,
                        dato
                );

            } catch (Exception e) {

                estado.setText(
                        "Zero: " + respuesta
                );

                hablarTexto(respuesta);
            }
        });
    }

    private void ejecutarAccion(
            String accion,
            String dato
    ) {

        try {

            switch (accion) {

                case "ABRIR_YOUTUBE":
                    abrirAplicacion(
                            "com.google.android.youtube"
                    );
                    hablarTexto(
                            "Abriendo YouTube."
                    );
                    break;

                case "ABRIR_WHATSAPP":
                    abrirAplicacion(
                            "com.whatsapp"
                    );
                    hablarTexto(
                            "Abriendo WhatsApp."
                    );
                    break;

                case "ABRIR_CHROME":
                    abrirAplicacion(
                            "com.android.chrome"
                    );
                    hablarTexto(
                            "Abriendo Chrome."
                    );
                    break;

                case "ABRIR_GOOGLE":
                    abrirWeb(
                            "https://www.google.com"
                    );
                    hablarTexto(
                            "Abriendo Google."
                    );
                    break;

                case "ABRIR_AJUSTES":
                    startActivity(
                            new Intent(
                                    Settings.ACTION_SETTINGS
                            )
                    );
                    hablarTexto(
                            "Abriendo ajustes."
                    );
                    break;

                case "ABRIR_WIFI":
                    startActivity(
                            new Intent(
                                    Settings.ACTION_WIFI_SETTINGS
                            )
                    );
                    hablarTexto(
                            "Abriendo configuración de Wi-Fi."
                    );
                    break;

                case "ABRIR_BLUETOOTH":
                    startActivity(
                            new Intent(
                                    Settings.ACTION_BLUETOOTH_SETTINGS
                            )
                    );
                    hablarTexto(
                            "Abriendo Bluetooth."
                    );
                    break;

                case "ABRIR_WEB":
                    abrirWeb(dato);
                    hablarTexto(
                            "Abriendo la página."
                    );
                    break;

                case "VOLUMEN_MAS":
                    cambiarVolumen(
                            AudioManager.ADJUST_RAISE
                    );
                    hablarTexto(
                            "Subiendo volumen."
                    );
                    break;

                case "VOLUMEN_MENOS":
                    cambiarVolumen(
                            AudioManager.ADJUST_LOWER
                    );
                    hablarTexto(
                            "Bajando volumen."
                    );
                    break;

                case "SILENCIO":
                    cambiarVolumen(
                            AudioManager.ADJUST_MUTE
                    );
                    hablarTexto(
                            "Silenciando."
                    );
                    break;

                case "LINterna":
                    encenderLinterna();
                    break;

                case "LLAMAR":
                    llamar(dato);
                    break;

                default:
                    estado.setText(
                            "Zero: " + dato
                    );
                    hablarTexto(dato);
                    break;
            }

        } catch (Exception e) {

            estado.setText(
                    "No pude ejecutar la acción."
            );

            hablarTexto(
                    "No pude ejecutar esa acción."
            );
        }
    }

    private void abrirAplicacion(
            String paquete
    ) {

        Intent intent =
                getPackageManager()
                        .getLaunchIntentForPackage(
                                paquete
                        );

        if (intent != null) {

            startActivity(intent);

        } else {

            hablarTexto(
                    "No encontré esa aplicación."
            );
        }
    }

    private void abrirWeb(
            String direccion
    ) {

        if (direccion == null ||
                direccion.trim().isEmpty()) {

            return;
        }

        if (!direccion.startsWith("http://") &&
                !direccion.startsWith("https://")) {

            direccion =
                    "https://" + direccion;
        }

        Intent intent =
                new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(direccion)
                );

        startActivity(intent);
    }

    private void cambiarVolumen(
            int direccion
    ) {

        AudioManager audio =
                (AudioManager)
                        getSystemService(
                                Context.AUDIO_SERVICE
                        );

        audio.adjustVolume(
                direccion,
                AudioManager.FLAG_SHOW_UI
        );
    }

    private void encenderLinterna() {

        try {

            CameraManager camera =
                    (CameraManager)
                            getSystemService(
                                    Context.CAMERA_SERVICE
                            );

            String camara =
                    camera.getCameraIdList()[0];

            camera.setTorchMode(
                    camara,
                    true
            );

            hablarTexto(
                    "Linterna encendida."
            );

        } catch (Exception e) {

            hablarTexto(
                    "No pude controlar la linterna."
            );
        }
    }

    private void llamar(
            String numero
    ) {

        if (numero == null ||
                numero.trim().isEmpty()) {

            hablarTexto(
                    "Necesito un número para llamar."
            );

            return;
        }

        Intent intent =
                new Intent(
                        Intent.ACTION_DIAL,
                        Uri.parse(
                                "tel:" + numero
                        )
                );

        startActivity(intent);

        hablarTexto(
                "Abriendo el teléfono."
        );
    }

    private void hablarTexto(
            String texto
    ) {

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
