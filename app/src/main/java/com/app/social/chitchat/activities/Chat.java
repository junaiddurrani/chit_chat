package com.app.social.chitchat.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.social.chitchat.R;
import com.app.social.chitchat.adapters.MessageAdapter;
import com.app.social.chitchat.classes.GetTimeAgo;
import com.app.social.chitchat.models.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends AppCompatActivity {

    private String mChatUser;
    private DatabaseReference mRootRef;
    private RecyclerView mMessagesList;

    private TextView mTitleView;
    private TextView mLastSeenView;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;
    private CircleImageView mProfileImage;

    private EditText chat_message_view;
    private ImageButton chat_record_button;
    Button chat_send_button;

    private final List<Messages> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPreKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        setTitle("");

        mAdapter = new MessageAdapter(messageList);

        mRefreshLayout = findViewById(R.id.messagesRefresh);

        mMessagesList = findViewById(R.id.messagesList);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mAdapter);


        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name");
        final String image = getIntent().getStringExtra("user_image");

        //mRootRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mChatUser);

        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        mProfileImage = action_bar_view.findViewById(R.id.custom_bar_image);

        Picasso.with(Chat.this).load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.blank_pic).into(mProfileImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError() {
                Picasso.with(Chat.this).load(image).placeholder(R.drawable.blank_pic).into(mProfileImage);
            }
        });

        mTitleView = findViewById(R.id.profile_name);
        mLastSeenView = findViewById(R.id.last_seen);

        chat_message_view = findViewById(R.id.chat_message_view);
        chat_record_button = findViewById(R.id.chat_record_button);
        chat_send_button = findViewById(R.id.chat_send_button);

        loadMessages();

        mTitleView.setText(userName);

        try {
            mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String online = dataSnapshot.child("online").getValue().toString();
                    //String image = dataSnapshot.child("profile_pic").getValue().toString();


                    if (online.equals("true")) {
                        mLastSeenView.setText("Online");
                    }
                    else {
                        GetTimeAgo getTimeAgo = new GetTimeAgo();
                        long lastTime = Long.parseLong(online);
                        String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                        mLastSeenView.setText(lastSeenTime);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(mChatUser)) {
                        Map chatAddMap = new HashMap();
                        chatAddMap.put("seen", false);
                        chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                        Map chatUserMap = new HashMap();
                        chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                        chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                        mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError !=null) {
                                    Log.d("Chat_Log", databaseError.getMessage().toString());

                                }
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        catch (Exception e) {

        }

        mRootRef.keepSynced(true);

        chat_record_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 10);
                }
                else {
                    Toast.makeText(Chat.this, "This feature is not supported by your device", Toast.LENGTH_SHORT).show();
                }

            }
        });

        chat_send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();
            }
        });

    }

    private void loadMoreMessages() {
        DatabaseReference messageReference = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);
        final Query mesageQuery = messageReference.orderByKey().endAt(mLastKey).limitToLast(10);
        mesageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                messageList.add(itemPos++, messages);
                String messageKey = dataSnapshot.getKey();
                if (!mPreKey.equals(messageKey)) {
                    messageList.add(itemPos++, messages);
                }
                else {
                    mPreKey = mLastKey;
                }
                if (itemPos == 1) {
                    mLastKey = messageKey;
                }
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messageList.size() -1);
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
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

    public void loadMessages() {

        DatabaseReference messageReference = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);
        final Query mesageQuery = messageReference.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        mesageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                itemPos++;
                if (itemPos == 1) {
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPreKey = messageKey;
                }
                messageList.add(messages);
                mAdapter.notifyDataSetChanged();
                mMessagesList.scrollToPosition(messageList.size() -1);
                mRefreshLayout.setRefreshing(false);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    chat_message_view.setText(result.get(0));
                }
        }

    }

    private void sendMessage() {
        String message = chat_message_view.getText().toString();
        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("Messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            chat_message_view.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {
                        Log.d("Chat_Log", databaseError.getMessage().toString());
                    }

                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
