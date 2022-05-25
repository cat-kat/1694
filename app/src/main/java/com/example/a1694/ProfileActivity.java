package com.example.a1694;


import static com.example.a1694.SignInActivity.mUser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ProfileActivity extends AppCompatActivity {

    TextView name, nickname, hometown, dialect, age, desc;
    ImageView photo;

    /* эдит + красивое отображение!
    кнопочки должны работать
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        name = findViewById(R.id.name);
        nickname = findViewById(R.id.username);
        hometown = findViewById(R.id.hometown);
        dialect = findViewById(R.id.dialect);
        age = findViewById(R.id.age);
        desc = findViewById(R.id.desc);
        photo = findViewById(R.id.photo);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
       DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                     DataSnapshot childSnap  = snapshot;

                    String mName = childSnap.child("name").getValue().toString();
                    String mNickname = childSnap.child("nickname").getValue().toString();
                    String mHometown = childSnap.child("hometown").getValue().toString();
                    String mDialect = childSnap.child("dialect").getValue().toString();
                    String mAge = childSnap.child("age").getValue().toString();
                    String mDesc = childSnap.child("desc").getValue().toString();
                    String mPhoroUrl = childSnap.child("photoUrl").getValue().toString();

                    name.setText(mName);
                    nickname.setText(mNickname);
                    hometown.setText(mHometown);
                    dialect.setText(mDialect);
                    age.setText(mAge);
                    desc.setText(mDesc);

                    URL req = null;
                    try {
                        req = new URL(
                                mPhoroUrl);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    Bitmap mIcon_val = null;
                    try {
                        mIcon_val = BitmapFactory.decodeStream(req.openConnection()
                                .getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    photo.setImageBitmap(mIcon_val);
                }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

     /*   if (mUser.nickname != "-1")
            nickname.setText(ref.child("name").get().toString()); else nickname.setText("still empty");
        if (mUser.hometown != "-1")
            hometown.setText(mUser.hometown); else hometown.setText("still empty");
            if (mUser.dialect != "-1")
                dialect.setText(mUser.dialect); else dialect.setText("still empty");
        if (mUser.age != "-1")
            age.setText(mUser.age); else age.setText("still empty");
        if (mUser.desc != "-1")
            desc.setText(mUser.desc); else desc.setText("still empty");
        if (mUser.photoUrl != "-1") {


        */

    }}





