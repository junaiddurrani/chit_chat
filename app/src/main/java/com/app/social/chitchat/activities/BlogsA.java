package com.app.social.chitchat.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.social.chitchat.R;
import com.app.social.chitchat.models.Blog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogsA extends AppCompatActivity {

    private RecyclerView blog_list;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseLike;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mDatabaseCurrentUser;
    private Query mQueryCurrentUser;
    private Boolean mProcessLike = false;
    private DatabaseReference mCommentsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blogs);

        blog_list = findViewById(R.id.blog_list);
        blog_list.setHasFixedSize(true);
        blog_list.setLayoutManager(new LinearLayoutManager(BlogsA.this));

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Blog");
        mQueryCurrentUser = mDatabaseCurrentUser.orderByChild("uid").equalTo(mCurrentUser.getUid());
        mCommentsDatabase = FirebaseDatabase.getInstance().getReference().child("Comments");

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Blog, BlogHolderView> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogHolderView>(
                Blog.class,
                R.layout.blog_row,
                BlogHolderView.class,
                mQueryCurrentUser
        ) {
            @Override
            protected void populateViewHolder(final BlogHolderView viewHolder, Blog model, int position) {
                final String list_user_id = getRef(position).getKey();
                final String current_userId = mCurrentUser.getUid();

                viewHolder.setLikeButton(list_user_id);
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(model.getImage(),BlogsA.this);
                viewHolder.setUsername(model.getUsername());
                viewHolder.setPosttime(model.getPost_time());

                final String userId = viewHolder.setUid(model.getUid());

                viewHolder.textName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(BlogsA.this, UserProfile.class);
                        intent.putExtra("user_id", userId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });


                mUsersDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String image = dataSnapshot.child("profile_thumb_image").getValue().toString();
                        viewHolder.setProfileImage(image, getApplicationContext());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                Query commentCountQuery = mCommentsDatabase.child(list_user_id);
                commentCountQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = (int) dataSnapshot.getChildrenCount();
                        if (count != 0) {
                            viewHolder.commentsCount.setText(count + "");
                        }
                        else {
                            viewHolder.commentsCount.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Query countQuery = mDatabaseLike.child(list_user_id);
                countQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int count = (int) dataSnapshot.getChildrenCount();
                        if (count != 0) {
                            viewHolder.likesCount.setText(count + "");
                        }
                        else {
                            viewHolder.likesCount.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.likeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.child(list_user_id).hasChild(current_userId)) {
                                        mDatabaseLike.child(list_user_id).child(current_userId).removeValue();
                                        mProcessLike = false;

                                        Query countQuery = mDatabaseLike.child(list_user_id);
                                        countQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                int count = (int) dataSnapshot.getChildrenCount();
                                                if (count != 0) {
                                                    viewHolder.likesCount.setText(count + "");
                                                }
                                                else {
                                                    viewHolder.likesCount.setText("");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    else {
                                        mUsersDatabase.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                mDatabaseLike.child(list_user_id).child(current_userId).setValue(dataSnapshot.child("name").getValue());
                                                mProcessLike = false;

                                                Query countQuery = mDatabaseLike.child(list_user_id);
                                                countQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        int count = (int) dataSnapshot.getChildrenCount();
                                                        if (count != 0) {
                                                            viewHolder.likesCount.setText(count + "");
                                                        }
                                                        else {
                                                            viewHolder.likesCount.setText("");
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
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }
        };

        blog_list.setAdapter(firebaseRecyclerAdapter);

    }

    public static class BlogHolderView extends RecyclerView.ViewHolder {

        View mView;
        Button textName;
        TextView likeButton, commentButton, likesCount, commentsCount;
        DatabaseReference mLikeDatabase;
        FirebaseUser mCurrentUser;
        ImageView imageView;
        CircleImageView circleImageView;

        public BlogHolderView(View itemView) {
            super(itemView);

            mView = itemView;
            textName =  mView.findViewById(R.id.blogUserName);
            likeButton = mView.findViewById(R.id.likeButton);
            likesCount = mView.findViewById(R.id.likesCount);
            commentsCount = mView.findViewById(R.id.commentsCount);
            imageView = mView.findViewById(R.id.postImage);
            commentButton = mView.findViewById(R.id.commentButton);
            circleImageView = mView.findViewById(R.id.blog_user_profile_image);

            mLikeDatabase = FirebaseDatabase.getInstance().getReference().child("Likes");
            mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
            mLikeDatabase.keepSynced(true);
        }

        public void setLikeButton(final String user_id) {
            mLikeDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(user_id).hasChild(mCurrentUser.getUid())) {
                        likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart_like, 0, 0, 0);
                        likeButton.setTextColor(Color.RED);
                    }
                    else {
                        likeButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart_not_like, 0, 0, 0);
                        likeButton.setTextColor(Color.BLACK);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setDesc(String desc) {
            TextView textView = (TextView) mView.findViewById(R.id.postDesc);
            textView.setText(desc);
        }

        public void setImage(final String image, final Context ctx) {
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image).into(imageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).placeholder(R.drawable.default_image).into(imageView);
                }
            });
        }

        public void setUsername(String username) {
            textName.setText(username);
        }

        public void setPosttime(String posttime) {
            TextView textView = (TextView) mView.findViewById(R.id.blogPostedTime);
            textView.setText(posttime);
        }

        public String setUid(String uid) {
            return uid;
        }

        public void setProfileImage(final String image, final Context ctx) {
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image).into(circleImageView, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).placeholder(R.drawable.default_image).into(circleImageView);
                }
            });
        }

    }

}
