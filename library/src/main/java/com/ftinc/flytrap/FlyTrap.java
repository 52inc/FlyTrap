package com.ftinc.flytrap;

import android.app.Activity;
import android.os.Bundle;

import com.ftinc.flytrap.view.FlyTrapView;

/**
 * Created by drew.heavner on 7/2/14.
 */
public class FlyTrap extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create FlyTrapView
        FlyTrapView view = new FlyTrapView(this);
        setContentView(view);

        view.setOnFlyTrapActionListener(new FlyTrapView.OnFlyTrapActionListener() {
            @Override
            public void onDone() {

                finish();

            }
        });

    }

}
