package me.ceo.bitch.appathon;

/**
 * Created by sahil on 11/2/14.
 */
public class NearbyListItem {
    private String itemTitle;

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public NearbyListItem(String title){
        this.itemTitle = title;
    }
}
