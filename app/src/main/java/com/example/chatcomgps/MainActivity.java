package com.example.chatcomgps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText loginEditText;
    private EditText senhaEditText;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginEditText = findViewById(R.id.loginEditText);
        senhaEditText = findViewById(R.id.senhaEditText);
        mAuth = FirebaseAuth.getInstance();
    }

    public void fazerLogin(View view){
        String login = loginEditText.getText().toString();
        String senha = senhaEditText.getText().toString();
        mAuth.signInWithEmailAndPassword(login, senha)
                .addOnSuccessListener((success) -> {
                    startActivity( new Intent (this, com.example.chatcomgps.ChatActivity.class));
                })
                .addOnFailureListener((exception) -> {
                    exception.printStackTrace();
                });
    }

    public void irParaCadastro(View view){
        startActivity( new Intent( this, com.example.chatcomgps.NovoUsuarioActivity.class));
    }
}
