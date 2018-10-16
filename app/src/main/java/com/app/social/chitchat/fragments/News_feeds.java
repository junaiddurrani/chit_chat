package com.app.social.chitchat.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.social.chitchat.R;
import com.app.social.chitchat.activities.AddBlog;
import com.app.social.chitchat.activities.Reviews;
import com.app.social.chitchat.activities.UserProfile;
import com.app.social.chitchat.activities.ViewEditAccount;
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

public class News_feeds extends Fragment {

    private RecyclerView blog_list;
    View mMainView;
    private DatabaseReference mDatabase, mDatabaseLike, mUsersDatabase, mCommentsDatabase,mProfileImage, mBlogUserDatabase;
    private FirebaseUser mCurrentUser;
    private Boolean mProcessLike = false;
    LinearLayoutManager llm;
    private CircleImageView profileImage;
    private TextView add_status;
    private ImageButton addPhoto;
    private FirebaseAuth mAuth;

    public News_feeds() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView =  inflater.inflate(R.layout.fragment_news_feeds, container, false);

        add_status = mMainView.findViewById(R.id.add_status);
        addPhoto = mMainView.findViewById(R.id.addPhoto);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddBlog.class));
            }
        });
        add_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AddBlog.class));
            }
        });

        mAuth = FirebaseAuth.getInstance();

        profileImage = mMainView.findViewById(R.id.profileImage);
        if (mAuth.getCurrentUser() != null) {
            mProfileImage = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mProfileImage.child("profile_thumb_image").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String image = (String) dataSnapshot.getValue();
                    Picasso.with(getContext()).load(image).placeholder(R.drawable.blank_pic).into(profileImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ViewEditAccount.class);
                startActivity(intent);
            }
        });

        blog_list = mMainView.findViewById(R.id.blog_list);
        blog_list.setHasFixedSize(true);

        blog_list.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);
        blog_list.setLayoutManager(llm);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Blog");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mCommentsDatabase = FirebaseDatabase.getInstance().getReference().child("Comments");
        mBlogUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        mUsersDatabase.keepSynced(true);
        mCommentsDatabase.keepSynced(true);
        mBlogUserDatabase.keepSynced(true);

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Blog, BlogHolderView> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogHolderView>(
                Blog.class,
                R.layout.blog_row,
                BlogHolderView.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final BlogHolderView viewHolder, Blog model, int position) {

                final String list_user_id = getRef(position).getKey();
                final String current_userId = mCurrentUser.getUid();

                viewHolder.setLikeButton(list_user_id);
                viewHolder.setDesc(model.getDesc());
                viewHolder.setImage(model.getImage(),getContext());
                viewHolder.setUsername(model.getUsername());
                viewHolder.setPosttime(model.getPost_time());
                final String userId = viewHolder.setUid(model.getUid());

                mDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            String id = (String) dataSnapshot.child("uid").getValue();
                            mBlogUserDatabase.child(id).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String image = (String) dataSnapshot.child("profile_thumb_image").getValue();
                                    viewHolder.setProfile_image(image, getContext());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.textName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String userId = (String) dataSnapshot.child("uid").getValue();
                                if (userId.equals(current_userId)) {
                                    Intent intent = new Intent(getContext(), ViewEditAccount.class);
                                    startActivity(intent);
                                }
                                else {
                                    Intent intent = new Intent(getContext(), UserProfile.class);
                                    intent.putExtra("user_id", userId);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                viewHolder.profile_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String userId = (String) dataSnapshot.child("uid").getValue();
                                if (userId.equals(current_userId)) {
                                    Intent intent = new Intent(getContext(), ViewEditAccount.class);
                                    startActivity(intent);
                                }
                                else {
                                    Intent intent = new Intent(getContext(), UserProfile.class);
                                    intent.putExtra("user_id", userId);
                                    startActivity(intent);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
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

                viewHolder.commentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*Intent intent = new Intent(getContext(), ReviewsActivity.class);
                        intent.putExtra("post_key", list_user_id);
                        startActivity(intent);*/
                        Bundle bundle = new Bundle();
                        bundle.putString("post_key", list_user_id);
                        Reviews reviewsActivity = new Reviews();
                        reviewsActivity.setArguments(bundle);
                        reviewsActivity.show(getActivity().getSupportFragmentManager(), reviewsActivity.getTag());
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

                viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

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
        TextView textName;
        TextView likeButton, commentButton, likesCount, commentsCount;
        DatabaseReference mLikeDatabase;
        FirebaseUser mCurrentUser;
        ImageView imageView;
        CircleImageView profile_image;

        public BlogHolderView(View itemView) {
            super(itemView);

            mView = itemView;
            textName = mView.findViewById(R.id.blogUserName);
            likeButton = mView.findViewById(R.id.likeButton);
            imageView = mView.findViewById(R.id.postImage);
            commentButton = mView.findViewById(R.id.commentButton);
            likesCount = mView.findViewById(R.id.likesCount);
            commentsCount = mView.findViewById(R.id.commentsCount);
            profile_image = mView.findViewById(R.id.blog_user_profile_image);

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
                        likeButton.setTextColor(Color.rgb(152,0,0));
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

        public void setProfile_image(final String image, final Context ctx) {
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.blank_pic).into(profile_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).placeholder(R.drawable.blank_pic).into(profile_image);
                }
            });
        }

        public String setUid(String uid) {
            return uid;
        }

        public void setProfileImage(final String image, final Context ctx) {
            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_image).into(profile_image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(ctx).load(image).placeholder(R.drawable.default_image).into(profile_image);
                }
            });
        }
    }
}

