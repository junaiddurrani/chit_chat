package com.app.social.chitchat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.social.chitchat.R;
import com.app.social.chitchat.activities.Reviews;
import com.app.social.chitchat.classes.GetTimeAgo;
import com.app.social.chitchat.models.Comments;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Junai on 9/4/2017.
 */

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private List<Comments> mCommentsList;
    private DatabaseReference mUserDatabase;
    Reviews reviews = new Reviews();
    Context context = reviews.setContext();

    private FirebaseAuth mAuth;

    public CommentsAdapter(List<Comments> mCommentsList) {
        this.mCommentsList = mCommentsList;
    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_single_layout, parent, false);
        return new CommentsViewHolder(v);
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView profilePic;
        public TextView profileName;
        public TextView dataTime;
        public TextView comment;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            profilePic = (CircleImageView) itemView.findViewById(R.id.comment_user_pic);
            profileName = (TextView) itemView.findViewById(R.id.comment_profile_name);
            dataTime = (TextView) itemView.findViewById(R.id.comment_date_time);
            comment = (TextView) itemView.findViewById(R.id.user_comment);

        }

    }

    @Override
    public void onBindViewHolder(final CommentsViewHolder viewHolder, int i) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Comments c = mCommentsList.get(i);
        String from_user = c.getFrom();

        viewHolder.comment.setText(c.getComment());
        GetTimeAgo getTimeAgo = new GetTimeAgo();
        long date = c.getTime();
        String time = GetTimeAgo.getTimeAgo(date, context);
        viewHolder.dataTime.setText("" + time);

       mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
       mUserDatabase.keepSynced(true);
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String image = dataSnapshot.child("profile_thumb_image").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                viewHolder.profileName.setText(name);

                Picasso.with(viewHolder.profilePic.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.blank_pic).into(viewHolder.profilePic, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(viewHolder.profilePic.getContext()).load(image)
                                .placeholder(R.drawable.blank_pic).into(viewHolder.profilePic);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    @Override
    public int getItemCount() {
        return mCommentsList.size();
    }

}
