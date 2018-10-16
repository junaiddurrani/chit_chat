package com.app.social.chitchat.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.social.chitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText textUserEmail,textUserPassword, textUserProfileName;
    private DatabaseReference mDatabaseReference;
    private ProgressDialog mProgressBar;
    private Button btnSignup, btnSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        textUserEmail = findViewById(R.id.txtEmail);
        textUserPassword = findViewById(R.id.txtPassword);
        textUserProfileName = findViewById(R.id.txtName);
        mProgressBar = new ProgressDialog(this);

        btnSignIn = findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
                finish();
            }
        });

        btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = textUserEmail.getText().toString();
                String password = textUserPassword.getText().toString();
                String name = textUserProfileName.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(name)) {
                    mProgressBar.setMessage("Creating your account");
                    mProgressBar.setCanceledOnTouchOutside(false);
                    mProgressBar.show();
                    registerAUser(name,email,password);
                }
                else {
                    Toast.makeText(Register.this, "Enter correct email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerAUser(final String name, final String email, final String password) {

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String UserId = current_user.getUid();

                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(UserId);
                    //mDatabaseReference.keepSynced(true);

                    String device_token = FirebaseInstanceId.getInstance().getToken();

                    HashMap<String, String> user_map = new HashMap<String, String>();
                    user_map.put("name",name);
                    user_map.put("email",email);
                    user_map.put("password", password);
                    user_map.put("gender","Not set");
                    user_map.put("Birthday","Not set");
                    user_map.put("profile_pic", "default");
                    user_map.put("profile_thumb_image","default");
                    user_map.put("device_token", device_token);

                    mDatabaseReference.setValue(user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mProgressBar.hide();
                                startActivity(new Intent(Register.this, MainActivity.class));
                                finish();
                            }
                        }
                    });
                }
                else {
                    mProgressBar.dismiss();
                    Toast.makeText(Register.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
