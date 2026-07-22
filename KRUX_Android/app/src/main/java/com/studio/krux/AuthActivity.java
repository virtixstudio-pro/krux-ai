package com.studio.krux;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AuthActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        Button authBtn = findViewById(R.id.authBtn);

        authBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String pass = passwordInput.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) return;

            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    openMain();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(registerTask -> {
                        if (registerTask.isSuccessful()) {
                            openMain();
                        } else {
                            Toast.makeText(AuthActivity.this, "Erreur Auth", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }

    private void openMain() {
        startActivity(new Intent(AuthActivity.this, MainActivity.class));
        finish();
    }
}
