package com.app.social.chitchat.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.social.chitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class ViewEditAccount extends AppCompatActivity {

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private ImageView mProfileImage;
    private TextView mProfileName, mBirthday, mGender, mFriends;
    private static final int GALLERY_PICK = 1;
    private StorageReference mImageStorage;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mFriendsCount;
    int count = 0;
    String pName,uBirthday,uGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_account);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mProfileImage = (ImageView) findViewById(R.id.profilePic);

        mProfileName = (TextView) findViewById(R.id.profileName);
        mBirthday = (TextView) findViewById(R.id.profileDob);
        mGender = (TextView) findViewById(R.id.profileGender);


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_UserId = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_UserId);
        mFriendsCount = FirebaseDatabase.getInstance().getReference().child("Friends");
        mUserDatabase.keepSynced(true);
        mFriendsCount.keepSynced(true);

        mFriends = (TextView) findViewById(R.id.profileFriends);

        Query query = mFriendsCount.child(current_UserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                count = (int) dataSnapshot.getChildrenCount();
                mFriends.setText(count + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        try {
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    pName = dataSnapshot.child("name").getValue().toString();
                    uBirthday = dataSnapshot.child("Birthday").getValue().toString();
                    uGender = dataSnapshot.child("gender").getValue().toString();
                    final String pImage = dataSnapshot.child("profile_pic").getValue().toString();

                    mProfileName.setText(pName);
                    mBirthday.setText(uBirthday);
                    mGender.setText(uGender);
                    Picasso.with(ViewEditAccount.this).load(pImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.blank_pic).into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ViewEditAccount.this).load(pImage).placeholder(R.drawable.blank_pic).into(mProfileImage);
                        }
                    });

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        catch (Exception e) {

        }

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();

            }
        });

    }

    public void showFileChooser() {
        Intent galleryIntent1 = new Intent();
        galleryIntent1.setType("image/*");
        galleryIntent1.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(galleryIntent1, "Select Image"), GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUrl = data.getData();

            CropImage.activity(imageUrl).setAspectRatio(1,1).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Uploading");
                mProgressDialog.setMessage("Please wait while image is uplaoded");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                String current_UserId = mCurrentUser.getUid();

                final File thumb_filePath = new File(resultUri.getPath());

                final Bitmap thumb_bitmap = new Compressor(ViewEditAccount.this)
                        .setMaxWidth(200)
                        .setMaxHeight(200)
                        .setQuality(75)
                        .compressToBitmap(thumb_filePath);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filePath = mImageStorage.child("profile_images").child(current_UserId + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumb_images").child(current_UserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    if (thumb_task.isSuccessful()) {

                                        String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        Map updateHashmap = new HashMap();
                                        updateHashmap.put("profile_pic",download_url);
                                        updateHashmap.put("profile_thumb_image", thumb_downloadUrl);

                                        mUserDatabase.updateChildren(updateHashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(ViewEditAccount.this, "Uploading Succussfull", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(ViewEditAccount.this, "Failed uploading", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                        else {

                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

    public void onEditClick(View view) {

        Intent intent = new Intent(ViewEditAccount.this, ProfileEdit.class);
        intent.putExtra("uName", pName);
        intent.putExtra("uBirthday", uBirthday);
        intent.putExtra("uGender", uGender);
        startActivity(intent);

    }

    public void blogClick(View view) {
        startActivity(new Intent(ViewEditAccount.this, BlogsA.class));
    }

    public void onMessageClick(View view) {
        startActivity(new Intent(ViewEditAccount.this, FriendList.class));
    }

    public void onFriendsClick(View view) {
        startActivity(new Intent(ViewEditAccount.this, FriendList.class));
    }
}