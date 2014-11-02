package me.ceo.bitch.appathon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class WriteReviewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_write_review, menu);
        MenuItem closereviewitem = menu.findItem(R.id.close_write_review);
        closereviewitem.setOnMenuItemClickListener
                (
                        new MenuItem.OnMenuItemClickListener ()
                        {
                            public boolean onMenuItemClick(MenuItem item)
                            { return (closeWriteReview()); }
                        }
                );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean closeWriteReview(){
        finish();
        return true;
    }
}
