package com.example.a1694;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d("AUTOR", FirebaseAuth.getInstance().getCurrentUser().toString());
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, MapsActivity.class));
        } else {

            setContentView(R.layout.activity_main);

            Button signInButton = findViewById(R.id.signInButton);
            Button logInButton = findViewById(R.id.logInButton);
        }
    }

    public void onClickSignIn(View v) {
        startActivity(new Intent(MainActivity.this, SignInActivity.class));

    }

    public void onClickLogIn(View v) {
        startActivity(new Intent(MainActivity.this, LogInActivity.class));

    }
}