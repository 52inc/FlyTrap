package com.ftinc.flytrap.example;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ftinc.flytrap.FlyTrap;
import com.ftinc.flytrap.model.EmailDelivery;
import com.ftinc.flytrap.util.Utils;


public class FlyTrapActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fly_trap);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.fly_trap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {

            FlyTrap.startFlyTrap(this, new FlyTrap.Config.Builder()
                .setAccentColor(getResources().getColor(android.R.color.holo_red_light))
                .setActiveColor(getResources().getColor(android.R.color.holo_blue_light))
                .setRadius(Utils.dpToPx(this, 56))
                .setScreenshotQuality(View.DRAWING_CACHE_QUALITY_AUTO)
                .setDeliverySystem(new EmailDelivery("drew@52inc.co", "FlyTrap Feedback", "Somethings wrong here...."))
                .build());

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
