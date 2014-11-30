package me.ceo.bitch.appathon;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sahil on 11/2/14.
 */
public class SearchListAdapter extends ArrayAdapter{
    private Context context;
    private boolean useList = true;
    private final ImageDownloader imageDownloader = new ImageDownloader();

    public SearchListAdapter(Context context, List items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
    }

    private class ViewHolder{
        TextView titleText;
        ImageView image;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        SearchListItem fragmentItemListItem = (SearchListItem)getItem(position);
        View viewToUse = null;
        LayoutInflater mInflater = (LayoutInflater) context .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            if(useList){
                viewToUse = mInflater.inflate(R.layout.list_row4, null);
            } else {
                viewToUse = mInflater.inflate(R.layout.list_row4, null);
            }
            holder = new ViewHolder();
            holder.titleText = (TextView)viewToUse.findViewById(R.id.title);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }
        holder.image = (ImageView)viewToUse.findViewById(R.id.catimage);
        imageDownloader.download(fragmentItemListItem.getImageurl(), holder.image);
        holder.titleText.setText(fragmentItemListItem.getItemTitle()); return viewToUse;
    }
}
