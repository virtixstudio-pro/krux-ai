package com.studio.krux;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView chatLog;
    private EditText inputMessage;
    private Button sendButton;
    private ScrollView scrollView;
    private final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = "VOTRE_CLE_API"; // Remplace par ta clé ou BuildConfig

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatLog = findViewById(R.id.chatLog);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        scrollView = findViewById(R.id.scrollView);

        sendButton.setOnClickListener(v -> {
            String text = inputMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                chatLog.append("\n\nMoi : " + text);
                inputMessage.setText("");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                callAi(text);
            }
        });
    }

    private void callAi(String prompt) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "llama3-8b-8192");
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            messages.put(message);
            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        chatLog.append("\n\nErreur réseau : " + e.getMessage());
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String resStr = response.body().string();
                            JSONObject jsonRes = new JSONObject(resStr);
                            String reply = jsonRes.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            new Handler(Looper.getMainLooper()).post(() -> {
                                chatLog.append("\n\nKRUX : " + reply.trim());
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            });
                        } catch (JSONException e) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                chatLog.append("\n\nErreur parsing JSON");
                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                            });
                        }
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
