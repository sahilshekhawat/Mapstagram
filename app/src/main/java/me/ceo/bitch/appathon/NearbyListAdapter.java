package me.ceo.bitch.appathon;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sahil on 11/2/14.
 */
public class NearbyListAdapter extends ArrayAdapter{

    private Context context;
    private boolean useList = true;

    public NearbyListAdapter(Context context, List items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
    }

    private class ViewHolder{
        TextView titleText;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        NearbyListItem fragmentItemListItem = (NearbyListItem)getItem(position);
        View viewToUse = null;
        LayoutInflater mInflater = (LayoutInflater) context .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            if(useList){
                viewToUse = mInflater.inflate(R.layout.list_row2, null);
            } else {
                viewToUse = mInflater.inflate(R.layout.list_row2, null);
            }
            holder = new ViewHolder();
            holder.titleText = (TextView)viewToUse.findViewById(R.id.title);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }
        holder.titleText.setText(fragmentItemListItem.getItemTitle()); return viewToUse;
    }
}
