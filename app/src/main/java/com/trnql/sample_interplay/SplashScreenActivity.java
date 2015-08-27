package com.trnql.sample_interplay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.trnql.smart.base.SmartCompatActivity;
import com.trnql.smart.location.LocationEntry;
import com.trnql.zen.core.db.DbKVP;
import com.trnql.zen.utlis.AndroidUtils;
import com.trnql.zen.utlis.IconPaths;


public class SplashScreenActivity extends SmartCompatActivity {

    Button startButton;
    boolean dbNetworkCallMade = false;
    boolean latLngNetworkCallMade = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        _initViews();

        _getData();

    }

    private void _getData() {
        DbKVP db = getDBManager().getDB_KVP(R.id.db_kvp_locality);
        String latString = db.get("lat");
        String lngString = db.get("lng");

        if(latString != null && lngString != null){
            AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: About to load image from splash using DB coordinates");
            ImageDownloadIntentService.startActionDownloadImage(this, latString, lngString);
            dbNetworkCallMade = true;
        }
    }

    @Override
    protected void smartLatLngChange(LocationEntry location) {
        String latString = String.valueOf(location.getLatitude());
        String lngString = String.valueOf(location.getLongitude());
        if(!latLngNetworkCallMade && !dbNetworkCallMade && ImageDownloadIntentService.rateLimitNotExceeded()) {
            AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: About to load image from splash using Lat/Lng change coordinates");
            ImageDownloadIntentService.startActionDownloadImage(this, latString, lngString);
            latLngNetworkCallMade = true;
        }
    }

    private void _initViews() {
        // initialize OP with default image
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.earth);
        BitmapDrawable bmd = new BitmapDrawable(bm);
        getObservablePropertyManager().setValue(R.id.op_location_bitmap, bmd);
        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndroidUtils.log(IconPaths.TRNQL_Check, "ISSUE13: Launching MainActivity");
                Intent startIntent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(startIntent);
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
