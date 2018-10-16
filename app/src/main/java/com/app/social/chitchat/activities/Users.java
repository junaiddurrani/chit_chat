package com.app.social.chitchat.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.app.social.chitchat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Users extends AppCompatActivity {

    private RecyclerView usersList;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        setTitle("Users");

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();

        usersList = (RecyclerView) findViewById(R.id.usersList);
        usersList.setHasFixedSize(true);
        usersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        //mUserRef.child("online").setValue(true);

        FirebaseRecyclerAdapter<com.app.social.chitchat.models.Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<com.app.social.chitchat.models.Users, UsersViewHolder>(
                com.app.social.chitchat.models.Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUsersDatabase
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder viewHolder, com.app.social.chitchat.models.Users model, int position) {
                final String userId = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setEmail(model.getEmail());
                viewHolder.setUserImage(model.getProfile_thumb_image(), getApplicationContext());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (userId.equals(mAuth.getCurrentUser().getUid())) {
                            startActivity(new Intent(Users.this, ViewEditAccount.class));
                        }
                        else {
                            Intent intent = new Intent(Users.this, UserProfile.class);
                            intent.putExtra("user_id", userId);
                            startActivity(intent);
                        }
                    }
                });

            }
        };

        usersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.userName);
            userNameView.setText(name);
        }

        public void setEmail(String email) {
            TextView userEmailview = (TextView) mView.findViewById(R.id.userEmail);
            userEmailview.setText(email);
        }

        public void setUserImage(final String thumb_image, final Context ctx) {
            final CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.userImage);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.blank_pic).into(userImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.blank_pic).into(userImageView);
                }
            });
        }

    }
}
