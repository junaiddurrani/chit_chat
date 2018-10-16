package com.app.social.chitchat.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.app.social.chitchat.R;
import com.app.social.chitchat.fragments.Friends_requests;
import com.app.social.chitchat.fragments.More_menus;
import com.app.social.chitchat.fragments.News_feeds;
import com.app.social.chitchat.fragments.User_profile;
import com.app.social.chitchat.fragments.post_notifications;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;
    private TabLayout tabs;
    private ViewPager viewPager;
    int[] tabs_icons = {R.drawable.news_feeds, R.drawable.friend_request, R.drawable.profile, R.drawable.notification, R.drawable.menu};
    int[] tabs_icons_select = {R.drawable.news_feeds_select, R.drawable.friend_request_select,
    R.drawable.profile_select, R.drawable.notification_select, R.drawable.menu_select};
    private ImageButton btnChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        if (mAuth.getCurrentUser() != null) {

            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.keepSynced(true);
        }

        if (mCurrentUser == null) {
            sendToLogin();
        }
        else {
            mUserRef.child("online").setValue("true");
        }

        viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);


        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeTabs(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabsIcon();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        setTitle("");

        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.main_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        btnChat = action_bar_view.findViewById(R.id.btnChat);
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Chat_List.class));
            }
        });


    }

    private void sendToLogin() {
        startActivity(new Intent(this, Login.class));
        finish();
    }

    private void tabsIcon() {
        tabs.getTabAt(0).setIcon(tabs_icons_select[0]);
        tabs.getTabAt(1).setIcon(tabs_icons[1]);
        tabs.getTabAt(2).setIcon(tabs_icons[2]);
        tabs.getTabAt(3).setIcon(tabs_icons[3]);
        tabs.getTabAt(4).setIcon(tabs_icons[4]);
    }

    private void changeTabs(int i) {
        if (i == 0) {
            tabs.getTabAt(0).setIcon(tabs_icons_select[0]);
            tabs.getTabAt(1).setIcon(tabs_icons[1]);
            tabs.getTabAt(2).setIcon(tabs_icons[2]);
            tabs.getTabAt(3).setIcon(tabs_icons[3]);
            tabs.getTabAt(4).setIcon(tabs_icons[4]);
        }
        if (i==1) {
            tabs.getTabAt(0).setIcon(tabs_icons[0]);
            tabs.getTabAt(1).setIcon(tabs_icons_select[1]);
            tabs.getTabAt(2).setIcon(tabs_icons[2]);
            tabs.getTabAt(3).setIcon(tabs_icons[3]);
            tabs.getTabAt(4).setIcon(tabs_icons[4]);
        }
        if (i==2) {
            tabs.getTabAt(0).setIcon(tabs_icons[0]);
            tabs.getTabAt(1).setIcon(tabs_icons[1]);
            tabs.getTabAt(2).setIcon(tabs_icons_select[2]);
            tabs.getTabAt(3).setIcon(tabs_icons[3]);
            tabs.getTabAt(4).setIcon(tabs_icons[4]);
        }
        if (i==3) {
            tabs.getTabAt(0).setIcon(tabs_icons[0]);
            tabs.getTabAt(1).setIcon(tabs_icons[1]);
            tabs.getTabAt(2).setIcon(tabs_icons[2]);
            tabs.getTabAt(3).setIcon(tabs_icons_select[3]);
            tabs.getTabAt(4).setIcon(tabs_icons[4]);
        }
        if (i==4) {
            tabs.getTabAt(0).setIcon(tabs_icons[0]);
            tabs.getTabAt(1).setIcon(tabs_icons[1]);
            tabs.getTabAt(2).setIcon(tabs_icons[2]);
            tabs.getTabAt(3).setIcon(tabs_icons[3]);
            tabs.getTabAt(4).setIcon(tabs_icons_select[4]);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new News_feeds());
        adapter.addFrag(new Friends_requests());
        adapter.addFrag(new User_profile());
        adapter.addFrag(new post_notifications());
        adapter.addFrag(new More_menus());
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment) {
            mFragmentList.add(fragment);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

}
