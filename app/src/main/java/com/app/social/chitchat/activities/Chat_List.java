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
import com.app.social.chitchat.models.Messages;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat_List extends AppCompatActivity {

    private RecyclerView mChat_list;
    private DatabaseReference mChatDatabase, mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat__list);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_userId = mAuth.getCurrentUser().getUid();
        mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrent_userId);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mChatDatabase.keepSynced(true);
        mUsersDatabase.keepSynced(true);

        mChat_list = findViewById(R.id.chat_list);

        mChat_list.setHasFixedSize(true);
        mChat_list.setLayoutManager(new LinearLayoutManager(this));

    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Messages, ChatViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Messages, ChatViewHolder>(
                Messages.class,
                R.layout.user_message_single_layout,
                ChatViewHolder.class,
                mChatDatabase
        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, Messages model, int position) {

                final String userId = getRef(position).getKey();

                mUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String name = dataSnapshot.child("name").getValue().toString();
                        final String userThumb = dataSnapshot.child("profile_thumb_image").getValue().toString();

                        viewHolder.setName(name);
                        viewHolder.setUserImage(userThumb,getApplicationContext());


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent chatIntent = new Intent(getApplicationContext(), Chat.class);
                                chatIntent.putExtra("user_id", userId);
                                chatIntent.putExtra("user_name", name);
                                chatIntent.putExtra("user_image", userThumb);
                                startActivity(chatIntent);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mChat_list.setAdapter(firebaseRecyclerAdapter);
    }


    public static class ChatViewHolder extends RecyclerView.ViewHolder  {

        View mView;

        public ChatViewHolder(View itemView) {

            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            TextView userNameView = mView.findViewById(R.id.user_profile_name);
            userNameView.setText(name);
        }

        public void setUserImage(final String thumb_image, final Context ctx) {
            final CircleImageView userImageView = mView.findViewById(R.id.user_message_pic);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.blank_pic).into(userImageView);
        }
    }
}