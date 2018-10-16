package com.app.social.chitchat.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.social.chitchat.R;
import com.app.social.chitchat.models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendList extends AppCompatActivity {

    private RecyclerView mFriends_list;
    private DatabaseReference mFriendsDatabase, mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        mFriends_list = (RecyclerView) findViewById(R.id.friend_list);
        mAuth = FirebaseAuth.getInstance();
        mCurrent_userId = mAuth.getCurrentUser().getUid();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_userId);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase.keepSynced(true);

        mFriends_list.setHasFixedSize(true);
        mFriends_list.setLayoutManager(new LinearLayoutManager(FriendList.this));


        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecylerViewAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                FriendsViewHolder.class,
                mFriendsDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder viewHolder, final Friends model, int position) {

                viewHolder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String name = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("profile_thumb_image").getValue().toString();

                        if (dataSnapshot.hasChild("online")) {
                            if (dataSnapshot.child("online").getValue().equals("true") || dataSnapshot.child("online").getValue().equals(ServerValue.TIMESTAMP)){
                                String user_online = (String) dataSnapshot.child("online").getValue();
                                viewHolder.setUserOnline(user_online);
                            }
                        }

                        viewHolder.setName(name);
                        viewHolder.setUserImage(userThumb, FriendList.this);
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence options[] = new CharSequence[] {"Open Profile", "Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendList.this);
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0) {
                                            Intent profileIntent = new Intent(FriendList.this, UserProfile.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);
                                        }
                                        if (which == 1) {
                                            Intent chatIntent = new Intent(FriendList.this, Chat.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", name);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        mFriends_list.setAdapter(friendsRecylerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setDate(String date) {
            TextView textView = (TextView) mView.findViewById(R.id.userEmail);
            textView.setText(date);
        }

        public void setName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.userName);
            userNameView.setText(name);
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

        public void setUserOnline(String online) {
            ImageView onlineStatus = (ImageView) mView.findViewById(R.id.onlineDot);
            if (online.equals("true")) {
                onlineStatus.setVisibility(View.VISIBLE);
            }
            else {
                onlineStatus.setVisibility(View.INVISIBLE);
            }
        }

    }

}