package com.studio.krux;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView chatDisplay;
    private EditText msgInput;
    private TextToSpeech tts;
    private static final String GROQ_KEY = BuildConfig.GROQ_API_KEY;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatDisplay = findViewById(R.id.chatDisplay);
        msgInput = findViewById(R.id.msgInput);
        ImageButton sendBtn = findViewById(R.id.sendBtn);
        Button logoutBtn = findViewById(R.id.logoutBtn);

        tts = new TextToSpeech(this, this);

        logoutBtn.setOnClickListener(v -> {
            if (tts != null) {
                tts.stop();
            }
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
        });

        sendBtn.setOnClickListener(v -> {
            String prompt = msgInput.getText().toString().trim();
            if (!prompt.isEmpty()) {
                appendChat("VOUS > " + prompt + "\n");
                msgInput.setText("");
                queryGroq(prompt);
            }
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.FRENCH);
            tts.setPitch(0.9f);
            tts.setSpeechRate(1.0f);
        }
    }

    private void speakOut(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "KRUX_VOICE_ID");
        }
    }

    private void appendChat(String text) {
        runOnUiThread(() -> chatDisplay.append(text + "\n"));
    }

    private void queryGroq(String prompt) {
        new Thread(() -> {
            try {
                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("model", "llama-3.3-70b-versatile");

                JSONArray messages = new JSONArray();
                JSONObject msg = new JSONObject();
                msg.put("role", "user");
                msg.put("content", prompt);
                messages.put(msg);

                jsonPayload.put("messages", messages);

                RequestBody body = RequestBody.create(
                        jsonPayload.toString(), 
                        MediaType.get("application/json; charset=utf-8")
                );

                Request request = new Request.Builder()
                        .url("https://api.groq.com/openai/v1/chat/completions")
                        .addHeader("Authorization", "Bearer " + GROQ_KEY)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject resJson = new JSONObject(response.body().string());
                        String reply = resJson.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");
                        
                        appendChat("KRUX > " + reply + "\n");
                        speakOut(reply);
                    } else {
                        appendChat("ERREUR > " + response.code() + "\n");
                    }
                }
            } catch (Exception e) {
                appendChat("ERREUR > " + e.getMessage() + "\n");
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
