package com.app.social.chitchat.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.social.chitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddBlog extends AppCompatActivity {

    private ImageView mSelectImage;
    private static final int GALLERY_REQUEST = 1;
    private EditText mDescText;
    private Button mAddPost;
    private FirebaseUser mCurrentUser;
    private ProgressBar mProgress;
    private StorageReference mStorage, filePath;
    private Uri imageUri = null;
    private DatabaseReference mDatabase, mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_blog);

        mProgress = findViewById(R.id.progressBar);
        mStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mDescText = (EditText) findViewById(R.id.descText);
        mAddPost = (Button) findViewById(R.id.addPost);

        mSelectImage = (ImageView) findViewById(R.id.selectImage);
        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        mAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddPost.setEnabled(false);
                startPosting();
            }
        });

    }

    private void startPosting() {

        mProgress.setVisibility(View.VISIBLE);
        final String desc = mDescText.getText().toString();
        final String current_UserId = mCurrentUser.getUid();

        filePath = mStorage.child("Blog_images").child(imageUri.getLastPathSegment());

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final String downloadUri = taskSnapshot.getDownloadUrl().toString();
                final DatabaseReference newPost = mDatabase.push();
                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                newPost.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        mUserDatabase.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String name = dataSnapshot.getValue().toString();
                                Map<String, String> postMap = new HashMap<>();
                                postMap.put("desc", desc);
                                postMap.put("image", downloadUri);
                                postMap.put("uid", current_UserId);
                                postMap.put("post_time", currentDate);
                                postMap.put("username", name);
                                newPost.setValue(postMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            mProgress.setVisibility(View.INVISIBLE);
                                            mAddPost.setEnabled(true);
                                            Toast.makeText(AddBlog.this, "Posted Succussfully", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(AddBlog.this, MainActivity.class));
                                            finish();
                                        }
                                        else {
                                            Toast.makeText(AddBlog.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            mSelectImage.setImageURI(imageUri);
        }
    }

}
