package com.app.social.chitchat.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.social.chitchat.R;
import com.app.social.chitchat.adapters.CommentsAdapter;
import com.app.social.chitchat.models.Comments;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class Reviews extends BottomSheetDialogFragment {

    String mPost_key;
    private RecyclerView mCommentList;
    private DatabaseReference mCommentDatabase;
    private DatabaseReference mUsersDatabase;
    private String mCurrentUser;
    private final List<Comments> commentList = new ArrayList<>();
    private CommentsAdapter mAdapter;
    private Button postButton;
    private EditText commentBox;
    private SwipeRefreshLayout mRefreshLayout;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View contentView = View.inflate(getContext(), R.layout.activity_reviews,null);
        dialog.setContentView(contentView);


        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        View parent = (View) contentView.getParent();
        parent.setFitsSystemWindows(true);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(parent);
        contentView.measure(0, 0);
        DisplayMetrics displaymetrics = new DisplayMetrics();        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int screenHeight = displaymetrics.heightPixels;
        bottomSheetBehavior.setPeekHeight(screenHeight);

        if (params.getBehavior() instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior)params.getBehavior()).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        params.height = screenHeight;
        parent.setLayoutParams(params);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mPost_key = getArguments().getString("post_key");
        mCommentList = contentView.findViewById(R.id.commentList);
        mCommentDatabase = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser);
        mCommentDatabase.keepSynced(true);
        mUsersDatabase.keepSynced(true);

        mCommentList.setHasFixedSize(true);
        mCommentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new CommentsAdapter(commentList);
        mCommentList.setAdapter(mAdapter);
        loadComments();
        postButton = contentView.findViewById(R.id.postComment);
        commentBox = contentView.findViewById(R.id.write_commment);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendComment();
            }
        });

        mRefreshLayout = contentView.findViewById(R.id.commentRefresh);

    }

    private void sendComment() {
        String comment = commentBox.getText().toString();
        if (!TextUtils.isEmpty(comment)) {

            DatabaseReference user_comment_push = mCommentDatabase.child("Comments")
                    .child(mPost_key).push();

            String push_id = user_comment_push.getKey();

            Map commentMap = new HashMap();
            commentMap.put("comment", comment);
            commentMap.put("from", mCurrentUser);
            commentMap.put("type", "text");
            commentMap.put("time", ServerValue.TIMESTAMP);

            commentBox.setText("");

            user_comment_push.updateChildren(commentMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        Log.d("Chat_Log", databaseError.getMessage().toString());
                    }

                }
            });

        }
    }

    private void loadComments() {
        mCommentDatabase.child("Comments").child(mPost_key).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Comments comments = dataSnapshot.getValue(Comments.class);
                commentList.add(comments);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Context setContext() {
        return getActivity();
    }

}
