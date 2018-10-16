package com.app.social.chitchat.activities;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.social.chitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileEdit extends AppCompatActivity {

    private TextInputLayout mProfileName;
    private RadioButton genderMale, genderFemale;
    private Button mSaveChanges;

    private TextView dob;
    int year_x, month_x, day_x;
    static final int DIALOG_ID = 0;

    private DatabaseReference mEditDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        setTitle("Edit Profile");

        mProgress = findViewById(R.id.progressBar);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_UserId = mCurrentUser.getUid();
        mEditDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_UserId);
        mEditDatabase.keepSynced(true);


        mProfileName = (TextInputLayout) findViewById(R.id.textNewProfileName);
        genderMale = (RadioButton) findViewById(R.id.genderMale);
        genderFemale = (RadioButton) findViewById(R.id.genderFemale);
        mSaveChanges = (Button) findViewById(R.id.saveChanges);
        dob = (TextView) findViewById(R.id.dob);

        String name = getIntent().getStringExtra("uName");
        String birthday = getIntent().getStringExtra("uBirthday");
        String gender = getIntent().getStringExtra("uGender");

        mProfileName.getEditText().setText(name);
        dob.setText(birthday);

        String male = genderMale.getText().toString();
        String female = genderFemale.getText().toString();

        if (gender.equals(male)) {
            genderMale.setChecked(true);
        }
        else if (gender.equals(female)) {
            genderFemale.setChecked(true);
        }


        mSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgress.setVisibility(View.VISIBLE);

                String pName = mProfileName.getEditText().getText().toString();
                String uGender = checkGender(genderMale, genderFemale);
                String uDob = dob.getText().toString();

                mEditDatabase.child("name").setValue(pName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgress.setVisibility(View.INVISIBLE);
                        }
                        else {
                            Toast.makeText(ProfileEdit.this, "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                mEditDatabase.child("Birthday").setValue(uDob).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgress.setVisibility(View.INVISIBLE);
                        }
                        else {
                            Toast.makeText(ProfileEdit.this, "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                mEditDatabase.child("gender").setValue(uGender).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgress.setVisibility(View.INVISIBLE);
                        }
                        else {
                            Toast.makeText(ProfileEdit.this, "Some error occured", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



            }
        });


        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

    }


    private String checkGender(RadioButton genderMale, RadioButton genderFemale) {

        String gender;

        if (genderMale.isChecked()) {
            return gender = genderMale.getText().toString();
        }
        else {
            return  gender = genderFemale.getText().toString();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID) {
            return new DatePickerDialog(this, datePickerListener, year_x, month_x, day_x);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            year_x = year;
            month_x = month;
            day_x = dayOfMonth;
            dob.setText(day_x + "/" + month_x + "/" + year_x);
        }
    };

}

