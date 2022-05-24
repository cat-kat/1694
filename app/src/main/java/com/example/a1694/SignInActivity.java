package com.example.a1694;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {


    /*
    Сделать возвращения по кнопкам назад (безопасное)
    Почистить тут все
    Сделать красивые кнопки и EditText
    Сделать красивое отображение выбранной картинки
    Сделать возможность жизни без картинки
    Подумать над плавностью
    Проверить log in
    Начать делать MapActivity (перенести большую часть из Mapiti)

     */
    FirebaseAuth mAuth;
    Button nextInputPhone;
    EditText phoneNumber;
    String mPhoneNumber;
    FirebaseUser user;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider mCallbacks;
    FrameLayout signin1;
    FrameLayout signin2;
    EditText verCode;
    FrameLayout signin3;
    FrameLayout signin4;
    EditText nickname;
    String mNickname;
    String mName;
    String mDialect;
    String mHometown;
    ImageView loadedImage;
    StorageReference mStorageRef;
    Uri uploadUri;
    EditText name, hometown, dialect;
    User mUser;
ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        phoneNumber = findViewById(R.id.phoneNumber);
        nextInputPhone = findViewById(R.id.signInButton);
        mAuth = FirebaseAuth.getInstance();
        signin1 = findViewById(R.id.step1);
        signin2 = findViewById(R.id.step2);
        signin3 = findViewById(R.id.step3);
        signin4 = findViewById(R.id.step4);
        verCode = findViewById(R.id.verCode);
        nickname = findViewById(R.id.nickname);
        loadedImage = (ImageView) findViewById(R.id.buttomImage);
        name = findViewById(R.id.name);
        hometown = findViewById(R.id.hometown);
        dialect = findViewById(R.id.dialect);
        progressBar = findViewById(R.id.progressBar);


        //  sendMessage();
    }


    public void authUser() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        String phoneNum = phoneNumber.getText().toString();
// Whenever verification is triggered with the whitelisted number,
// provided it is not set for auto-retrieval, onCodeSent will be triggered.
        FirebaseAuth auth = FirebaseAuth.getInstance();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNum)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(String verificationId,
                                           PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        // Save the verification id somewhere
                        // ...
                        Log.d("AUTOR", "CODE SENT");
                        // The corresponding whitelisted code above should be used to complete sign-in.
                        // MainActivity.this.enableUserManuallyInputCode();
                        Log.d("AUTOR", "onCodeSent:" + verificationId);

                        // Save verification ID and resending token so we can use them later
                        mVerificationId = verificationId;
                        mResendToken = forceResendingToken;
                        signin1.setVisibility(View.INVISIBLE);
                        signin2.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(ProgressBar.INVISIBLE);


                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        // Sign in with the credential
                        // ...
                        Log.d("AUTOR", "OK");

                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.d("AUTOR", "NOT OK");


                        // ...
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    public void onClickNext1(View view) {
        if (phoneNumber.getText().toString().isEmpty()) {
            Log.d("AUTOR", "Write number");
        } else {
            Log.d("BUTTON-CLICK", "Button on");
            authUser();

        }
    }

    public void onClickNext2(View view) {
        if (verCode.getText().toString().isEmpty()) {
            Log.d("AUTOR", "Write code");
        } else {
            Log.d("BUTTON-CLICK", "Button verCode on");
            checkCode();

        }
    }

    public void checkCode() {
        String code = verCode.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            user = task.getResult().getUser();

                            Log.d("AUTOR", "I'M OK!!");
                            Log.d("AUTOR-user", user.toString() + user.getUid());
                            String name = user.getDisplayName();
                            Log.d("AUTOR", "My name:" + name);
                            if (name == null) {
                                Log.d("AUTOR", "GO CREATING!!");
                                createUser();
                            } else loginUser();

                            // Update UI
                        } else {
                            Log.d("AUTOR", "I'M NOT OK!!");
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    public void createUser() {
        signin2.setVisibility(View.INVISIBLE);
        signin3.setVisibility(View.VISIBLE);
        Log.d("AUTOR", "signin3 in");
    }

    public void onClickNext3(View view) {
        Log.d("AUTOR", "Пришел сохраняться");
        mNickname = nickname.getText().toString();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(mNickname)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
        mUser = new User(mNickname, "-1", "-1", "-1", "-1", user.getUid().toString());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(mUser.id);
        ref.setValue(mUser);

        signin3.setVisibility(View.INVISIBLE);
        signin4.setVisibility(View.VISIBLE);
    }

    public void onClickNext4(View view) {
        upload();
    }

    public void onClickImageChoose(View v) {
        getImage();
    }

    private void getImage() {
        Intent intentChooser = new Intent();
        intentChooser.setType("image/");
        intentChooser.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentChooser, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null && data.getData() != null) {
            if (resultCode == RESULT_OK) {
                loadedImage.setImageURI(data.getData());
                //upload();
            }
        }
    }

    public void upload() {
        Log.d("AUTOR", "Я пришел в аплоад");
        Bitmap bitmap = ((BitmapDrawable) loadedImage.getDrawable()).getBitmap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteImage = baos.toByteArray();
        mStorageRef = FirebaseStorage.getInstance().getReference("UsersPhoto");
        final StorageReference mRef = mStorageRef.child(String.valueOf(System.currentTimeMillis()));
        UploadTask ut = mRef.putBytes(byteImage);
        Task<Uri> task = ut.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri = task.getResult();
                Log.d("AUTOR", "Он комплетед сработал");
                saveUser();

            }
        });
    }

    public void saveUser() {
        Log.d("AUTOR", "Я пошел все сохранять!");
        mName = name.getText().toString();
        mHometown = hometown.getText().toString();
        mDialect = dialect.getText().toString();

        if (mName != "" && mName != null)
            FirebaseDatabase.getInstance().getReference().child("users").child(mUser.id).child("name").setValue(mName);

        if (mHometown != "" && mHometown != null)
            FirebaseDatabase.getInstance().getReference().child("users").child(mUser.id).child("hometown").setValue(mHometown);

        if (mDialect != "" && mDialect != null)
            FirebaseDatabase.getInstance().getReference().child("users").child(mUser.id).child("dialect").setValue(mDialect);

        FirebaseDatabase.getInstance().getReference().child("users").child(mUser.id).child("photoUrl").setValue(uploadUri.toString());
        Log.d("AUTOR", "Я все сохранил!");
        startActivity(new Intent(SignInActivity.this, MapsActivity.class));
    }


    public void loginUser() {
        startActivity(new Intent(SignInActivity.this, MapsActivity.class));

    }
}


