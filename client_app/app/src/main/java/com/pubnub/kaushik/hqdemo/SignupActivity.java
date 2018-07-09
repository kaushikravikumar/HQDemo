package com.pubnub.kaushik.hqdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import java.util.UUID;

public class SignupActivity extends AppCompatActivity {

    EditText username;

    EditText password;

    Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signUp = findViewById(R.id.signup);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("pref", 0); // 0 - for private mode
        String uuid;
        // If user doesn't have uuid create a random one! Then grant access to read/write for PubNub channels.
        if (!pref.contains("uuid")) {
            SharedPreferences.Editor editor = pref.edit();
            uuid = UUID.randomUUID().toString();
            editor.putString("uuid", uuid);
            editor.commit();
            grantAccess();
        }
        // User has already created account and thus, takes them to MainActivity
        else {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
        }

    }
    private void grantAccess()
    {

    }
}
