package com.studio.krux;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView chatLog;
    private EditText inputMessage;
    private Button sendButton;
    private ScrollView scrollView;

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
                // Simulation de réponse en attendant l'intégration de l'API
                chatLog.append("\n\nKRUX : J'ai bien reçu ton message.");
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
