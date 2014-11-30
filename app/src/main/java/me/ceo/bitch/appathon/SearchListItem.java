package me.ceo.bitch.appathon;

/**
 * Created by sahil on 11/2/14.
 */// 192.168.64.94:3000
public class SearchListItem {
    private String itemTitle;
    private String imageurl;

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public SearchListItem(String title, String imageurl){
        this.itemTitle = title;
        this.imageurl = imageurl;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
