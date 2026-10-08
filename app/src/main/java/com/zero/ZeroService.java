package com.zero;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

public class ZeroService extends Service {

    private static final String CHANNEL_ID = "zero_service";

    private SpeechRecognizer reconocedor;
    private Intent intentReconocimiento;

    @Override
    public void onCreate() {
        super.onCreate();

        crearNotificacion();

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            iniciarEscucha();
        }
    }

    private void crearNotificacion() {

        NotificationChannel canal =
                new NotificationChannel(
                        CHANNEL_ID,
                        "Zero",
                        NotificationManager.IMPORTANCE_LOW
                );

        NotificationManager manager =
                getSystemService(NotificationManager.class);

        manager.createNotificationChannel(canal);

        Notification notificacion =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("Zero está activo")
                        .setContentText("Di \"Zero\" para activarlo")
                        .setSmallIcon(
                                android.R.drawable.ic_btn_speak_now
                        )
                        .build();

        startForeground(1001, notificacion);
    }

    private void iniciarEscucha() {

        if (reconocedor != null) {
            reconocedor.destroy();
        }

        reconocedor =
                SpeechRecognizer.createSpeechRecognizer(this);

        reconocedor.setRecognitionListener(
                new RecognitionListener() {

                    @Override
                    public void onResults(Bundle resultados) {

                        ArrayList<String> textos =
                                resultados.getStringArrayList(
                                        SpeechRecognizer.RESULTS_RECOGNITION
                                );

                        if (textos != null && !textos.isEmpty()) {

                            String texto =
                                    textos.get(0).toLowerCase(
                                            Locale.ROOT
                                    );

                            if (texto.contains("zero")) {

                                abrirZero();

                            } else {

                                iniciarEscucha();
                            }
                        } else {

                            iniciarEscucha();
                        }
                    }

                    @Override
                    public void onError(int error) {
                        iniciarEscucha();
                    }

                    @Override
                    public void onReadyForSpeech(
                            android.os.Bundle params) {
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(
                            byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {
                    }

                    @Override
                    public void onPartialResults(
                            Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(
                            int eventType,
                            Bundle params) {
                    }
                }
        );

        intentReconocimiento =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

        intentReconocimiento.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intentReconocimiento.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "es-MX"
        );

        intentReconocimiento.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        reconocedor.startListening(
                intentReconocimiento
        );
    }

    private void abrirZero() {

        Intent intent =
                new Intent(this, MainActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        if (reconocedor != null) {
            reconocedor.destroy();
            reconocedor = null;
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
