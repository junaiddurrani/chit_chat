package com.app.social.chitchat.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.social.chitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText userName,userPassword;
    private ProgressDialog mLoginProgress;
    private DatabaseReference mUserDatabase;
    private Button btnSignin, btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        userName = findViewById(R.id.txtEmail);
        userPassword = findViewById(R.id.txtPassword);

        mLoginProgress = new ProgressDialog(this);

        btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        btnSignin = findViewById(R.id.btnLogin);
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userN = userName.getText().toString();
                String pass = userPassword.getText().toString();

                if (!TextUtils.isEmpty(userN) || !TextUtils.isEmpty(pass)) {

                    mLoginProgress.setMessage("Signing In");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    loginUser(userN,pass);
                }
            }
        });

    }

    private void loginUser(String userN, String pass) {
        mAuth.signInWithEmailAndPassword(userN,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mLoginProgress.dismiss();
                    String current_userId = mAuth.getCurrentUser().getUid();
                    String deviceTokenId = FirebaseInstanceId.getInstance().getToken();

                    mUserDatabase.child(current_userId).child("device_token").setValue(deviceTokenId).addOnSuccessListener(
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    startActivity(new Intent(Login.this, MainActivity.class));
                                    finish();
                                }
                            }
                    );

                }
                else {
                    mLoginProgress.hide();
                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
