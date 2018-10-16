
package com.app.social.chitchat.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class UserProfile extends AppCompatActivity {

    private ImageView mUserImage;
    private TextView mProfileName, mGender, mBirthday, mFriends;
    private Button sendRequestButton;
    private DatabaseReference mUsersDatabase, mFriendRequestDatabase, mFriendDatabase, mRootRef, mFriendsCount, mNotificationDatabase;
    private FirebaseUser mCurrentUser;
    private ProgressDialog mProgress;
    private String mCurrent_state;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        final String user_Id = getIntent().getStringExtra("user_id");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_Id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase.keepSynced(true);
        mFriendDatabase.keepSynced(true);
        mRootRef.keepSynced(true);

        mUserImage = (ImageView) findViewById(R.id.profilePic);
        mProfileName = (TextView) findViewById(R.id.profileName);
        mGender = (TextView) findViewById(R.id.profileGender);
        mBirthday = (TextView) findViewById(R.id.profileDob);
        mFriends = (TextView) findViewById(R.id.profileFriends);

        sendRequestButton = (Button) findViewById(R.id.buttonSendRequest);

        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mNotificationDatabase.keepSynced(true);

        mFriendsCount = FirebaseDatabase.getInstance().getReference().child("Friends");
        mFriendsCount.keepSynced(true);

        Query query = mFriendsCount.child(user_Id);
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

        mCurrent_state = "not_friends";

        mProgress = new ProgressDialog(UserProfile.this);
        mProgress.setTitle("Loading data...");
        mProgress.setMessage("Please wait while User data is load");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String profileName = dataSnapshot.child("name").getValue().toString();
                String profileGender = dataSnapshot.child("gender").getValue().toString();
                String profileDob = dataSnapshot.child("Birthday").getValue().toString();
                final String profileImage = dataSnapshot.child("profile_pic").getValue().toString();

                mProfileName.setText(profileName);
                mGender.setText(profileGender);
                mBirthday.setText(profileDob);
                Picasso.with(UserProfile.this).load(profileImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.blank_pic).into(mUserImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(UserProfile.this).load(profileImage).placeholder(R.drawable.blank_pic).into(mUserImage);
                    }
                });

                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_Id)) {
                            String req_type = dataSnapshot.child(user_Id).child("request_type").getValue().toString();
                            if (req_type.equals("recieved")) {
                                mCurrent_state = "request_recieved";
                                sendRequestButton.setText("Accept Request");

                            }
                            else if (req_type.equals("sent")) {
                                mCurrent_state = "request_sent";
                                sendRequestButton.setText("Cancel Request");

                            }
                            mProgress.dismiss();
                        }
                        else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_Id)) {
                                        mCurrent_state = "friends";
                                        sendRequestButton.setText("Unfriend");

                                    }
                                    mProgress.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgress.dismiss();
                                }
                            });
                        }

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

        sendRequestButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                sendRequestButton.setEnabled(false);

                //Not friend state

                if (mCurrent_state.equals("not_friends")) {

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_Id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        mFriendRequestDatabase.child(user_Id).child(mCurrentUser.getUid()).child("request_type")
                                                .setValue("recieved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                HashMap<String, String> notificationData = new HashMap<>();
                                                notificationData.put("from", mCurrentUser.getUid());
                                                notificationData.put("type", "request");

                                                mNotificationDatabase.child(user_Id).push().setValue(notificationData).addOnSuccessListener(
                                                        new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                sendRequestButton.setEnabled(true);
                                                                mCurrent_state = "request_sent";
                                                                sendRequestButton.setText("Cancel Request");
                                                                Toast.makeText(UserProfile.this, "Request sent succussfully", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                );
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(UserProfile.this, "Request sending failed", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                }

                //Cancel request
                if (mCurrent_state.equals("request_sent")) {

                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_Id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mFriendRequestDatabase.child(user_Id).child(mCurrentUser.getUid()).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    sendRequestButton.setEnabled(true);
                                                    mCurrent_state = "not_friends";
                                                    sendRequestButton.setText("Send Request");

                                                }
                                            });

                                }
                            });
                }

                //request recieved state
                if (mCurrent_state.equals("request_recieved")) {
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_Id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendDatabase.child(user_Id).child(mCurrentUser.getUid()).child("date").setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_Id).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            mFriendRequestDatabase.child(user_Id).child(mCurrentUser.getUid()).removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {

                                                                            sendRequestButton.setEnabled(true);
                                                                            mCurrent_state = "friends";
                                                                            sendRequestButton.setText("Unfriend");

                                                                        }
                                                                    });

                                                        }
                                                    });

                                        }
                                    });

                        }
                    });
                }
            }
        });

    }

    public void onFriendsClick(View view) {
        startActivity(new Intent(UserProfile.this, FriendList.class));
    }

    public void onMessageClick(View view) {
        startActivity(new Intent(UserProfile.this, FriendList.class));
    }
}
