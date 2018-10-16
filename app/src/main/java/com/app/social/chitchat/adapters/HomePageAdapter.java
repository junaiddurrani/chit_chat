package com.app.social.chitchat.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.social.chitchat.R;
import com.app.social.chitchat.models.HomeListItems;

import java.util.ArrayList;

/**
 * Created by Durrani on 14-Jul-17.
 */

public class HomePageAdapter extends ArrayAdapter<HomeListItems> {

    public HomePageAdapter(Activity context, ArrayList<HomeListItems> words) {
        super(context,0,words);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.more_list_view_items,parent,false);
        }

        HomeListItems items = getItem(position);
        TextView names = (TextView) convertView.findViewById(R.id.list_itemNames);
        ImageView image = (ImageView) convertView.findViewById(R.id.list_itemImage);
        names.setText(items.getHomePage_items());
        if (items.hasImage()) {
            image.setImageResource(items.getHomePageImage());
            image.setVisibility(View.VISIBLE);
        }
        else {
            image.setVisibility(View.GONE);
        }

        return convertView;
    }
}
