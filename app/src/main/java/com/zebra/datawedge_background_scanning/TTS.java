package com.zebra.datawedge_background_scanning;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

public class TTS {
    private static TextToSpeech textToSpeech;

    private static String queueText = "";

    public static void init(final Context context) {
        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    if (queueText.length() >= 3)
                        speakBarcode(queueText);
                }
            });
        }
        else
        {
            if (queueText.length() >= 3)
                speakBarcode(queueText);
        }
    }

    public static void speakBarcode(String barcode){
        TTS.speak(barcode.substring(0, 1));
        TTS.speak(barcode.substring(1, 2));
        TTS.speak(barcode.substring(2, 3));
    }

    public static void speak(final String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    public static void initAndSpeak(final Context context, final String text)
    {
        queueText = text;
        init(context);
    }

}
