package com.app.social.chitchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.DateFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.social.chitchat.R;
import com.app.social.chitchat.activities.UserProfile;
import com.app.social.chitchat.models.Friend_request;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class Friends_requests extends Fragment {

    private View mMainView;
    private RecyclerView requestList;
    private DatabaseReference mRequestDatabase, mFriendRequestDatabase, mUserDatabase, mFriendDatabase;
    private FirebaseAuth mAuth;
    private String mCurrentUser_id;
    private FirebaseUser mCurrentUser;
    static String request;

    public Friends_requests() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends_requests, container, false);



        requestList = (RecyclerView) mMainView.findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser_id = mAuth.getCurrentUser().getUid();
        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request").child(mCurrentUser_id);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_request");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mRequestDatabase.keepSynced(true);
        mUserDatabase.keepSynced(true);

        requestList.setHasFixedSize(true);
        requestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friend_request, RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friend_request, RequestViewHolder>(
                Friend_request.class,
                R.layout.request_single_layout,
                RequestViewHolder.class,
                mRequestDatabase
        ) {
            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, Friend_request model, int position) {

                viewHolder.setRequest_type(model.getRequest_type());
                final String type = model.getRequest_type();

                final String list_user_id = getRef(position).getKey();

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (type.equals("recieved")) {
                            final String name = dataSnapshot.child("name").getValue().toString();
                            String image = dataSnapshot.child("profile_thumb_image").getValue().toString();
                            final String email = dataSnapshot.child("email").getValue().toString();

                            viewHolder.setName(name);
                            viewHolder.setEmail(email);
                            viewHolder.setProfilePicture(image,getContext());
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent profileIntent = new Intent(getContext(), UserProfile.class);
                                    profileIntent.putExtra("user_id", list_user_id);
                                    startActivity(profileIntent);
                                }
                            });
                        }

                        else if (type.equals("sent")){
                            Button requestAccept_button, requestDecline_button;
                            requestAccept_button = (Button) mMainView.findViewById(R.id.requestAcceptButton);
                            requestDecline_button = (Button) mMainView.findViewById(R.id.requestDeclineButton);
                            ImageView imageView = mMainView.findViewById(R.id.userRequestImage);
                            TextView textView = (TextView) mMainView.findViewById(R.id.userRequestName);
                            TextView textView2 = (TextView) mMainView.findViewById(R.id.userRequestEmail);
                            requestAccept_button.setVisibility(View.GONE);
                            requestDecline_button.setVisibility(View.GONE);
                            imageView.setVisibility(View.GONE);
                            textView.setVisibility(View.GONE);
                            textView2.setVisibility(View.GONE);
                        }

                        viewHolder.requestAccept_button.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onClick(View v) {

                                //request recieved state
                                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                                mFriendDatabase.child(mCurrentUser.getUid()).child(list_user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mFriendDatabase.child(list_user_id).child(mCurrentUser.getUid()).child("date").setValue(currentDate)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mFriendRequestDatabase.child(mCurrentUser.getUid()).child(list_user_id).removeValue()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                        mFriendRequestDatabase.child(list_user_id).child(mCurrentUser.getUid()).removeValue()
                                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                    @Override
                                                                                    public void onSuccess(Void aVoid) {
                                                                                        Toast.makeText(getActivity(),"Request Accepted", Toast.LENGTH_LONG).show();

                                                                                    }
                                                                                });

                                                                    }
                                                                });

                                                    }
                                                });

                                    }
                                });
                            }
                        });

                        viewHolder.requestDecline_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(list_user_id).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                mFriendRequestDatabase.child(list_user_id).child(mCurrentUser.getUid()).removeValue()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(getActivity(), "Request Cancelled", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                            }
                                        });

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        requestList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        View mView;
        private Button requestAccept_button, requestDecline_button;

        public RequestViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            requestAccept_button = (Button) mView.findViewById(R.id.requestAcceptButton);
            requestDecline_button = (Button) mView.findViewById(R.id.requestDeclineButton);
        }

        public void setRequest_type(String type) {
            request = type;
        }

        public void setProfilePicture(final String image, final Context ctx) {
            final ImageView imageView = mView.findViewById(R.id.userRequestImage);
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.blank_pic).into(imageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).placeholder(R.drawable.blank_pic).into(imageView);
                }
            });
        }

        public void setName(String name) {
            TextView textView = (TextView) mView.findViewById(R.id.userRequestName);
            textView.setText(name);
        }

        public void setEmail(String email) {
            TextView textView = (TextView) mView.findViewById(R.id.userRequestEmail);
            textView.setText(email);
        }

    }
}
