package com.app.social.chitchat.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.app.social.chitchat.R;
import com.app.social.chitchat.activities.ChatEvent;
import com.app.social.chitchat.activities.FriendList;
import com.app.social.chitchat.activities.Group_Chat;
import com.app.social.chitchat.activities.Login;
import com.app.social.chitchat.activities.Pictures;
import com.app.social.chitchat.activities.TermsConditions;
import com.app.social.chitchat.activities.Tutorials;
import com.app.social.chitchat.activities.Users;
import com.app.social.chitchat.activities.ViewEditAccount;
import com.app.social.chitchat.adapters.HomePageAdapter;
import com.app.social.chitchat.models.HomeListItems;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class More_menus extends Fragment {

    LinearLayout profileLayout;
    private ImageView home_profile_pic;
    private TextView home_profile_name;

    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private View mMainView;

    String pName;
    String pThumb;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    FirebaseUser currentUser;

    public More_menus() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_more_menus, container, false);

        home_profile_pic = (ImageView) mMainView.findViewById(R.id.home_profilePicture);
        home_profile_name = (TextView) mMainView.findViewById(R.id.home_profileName);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_UserId = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_UserId);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pName = dataSnapshot.child("name").getValue().toString();
                pThumb = dataSnapshot.child("profile_thumb_image").getValue().toString();
                home_profile_name.setText(pName);

                Picasso.with(getContext()).load(pThumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.blank_pic).into(home_profile_pic, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                        Picasso.with(getContext()).load(pThumb).placeholder(R.drawable.blank_pic).into(home_profile_pic);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final ArrayList<HomeListItems> HomeListItemses = new ArrayList<HomeListItems>();
        HomeListItemses.add(new HomeListItems("Languages Tutorils", R.drawable.tutorials));
        HomeListItemses.add(new HomeListItems("Events",R.drawable.events));
        HomeListItemses.add(new HomeListItems("Group Chat",R.drawable.groupchat));
        HomeListItemses.add(new HomeListItems("Friends List",R.drawable.friendlist));
        HomeListItemses.add(new HomeListItems("People",R.drawable.people));
        HomeListItemses.add(new HomeListItems("Photos",R.drawable.photos));
        HomeListItemses.add(new HomeListItems("Account Setting",R.drawable.accountsetting));
        HomeListItemses.add(new HomeListItems("Terms and Conditions & Policy",R.drawable.termscondition));
        HomeListItemses.add(new HomeListItems("Logout",R.drawable.logout));

        HomePageAdapter adapter = new HomePageAdapter(getActivity(),HomeListItemses);
        final ListView listView = (ListView) mMainView.findViewById(R.id.home_listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {
                    case 0:
                        startActivity(new Intent(getActivity(), Tutorials.class));
                        break;
                    case 1:
                        startActivity(new Intent(getActivity(), ChatEvent.class));
                        break;
                    case 2:
                        startActivity(new Intent(getActivity(), Group_Chat.class));
                        break;
                    case 3:
                        startActivity(new Intent(getActivity(), FriendList.class));
                        break;
                    case 4:
                        startActivity(new Intent(getActivity(), Users.class));
                        break;
                    case 5:
                        startActivity(new Intent(getActivity(), Pictures.class));
                        break;
                    case 6:
                        startActivity(new Intent(getActivity(), ViewEditAccount.class));
                        break;
                    case 7:
                        startActivity(new Intent(getActivity(), TermsConditions.class));
                        break;
                    case 8:
                        mAuth = FirebaseAuth.getInstance();
                        currentUser = mAuth.getCurrentUser();
                        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
                        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
                        FirebaseAuth.getInstance().signOut();
                        sendToStartActivity();
                        break;
                    default:
                        break;
                }

            }
        });

        profileLayout = mMainView.findViewById(R.id.layout_one);
        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(getActivity(), ViewEditAccount.class));
                }
                catch (Exception e) {

                }
            }
        });

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void sendToStartActivity() {
        startActivity(new Intent(getActivity(), Login.class));
        getActivity().finish();
    }

}