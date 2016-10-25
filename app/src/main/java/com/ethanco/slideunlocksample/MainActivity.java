package com.ethanco.slideunlocksample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ethanco.slideunlock.SlideUnlock;

public class MainActivity extends AppCompatActivity {

    private SlideUnlock slideUnlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        slideUnlock = (SlideUnlock) findViewById(R.id.slide_unlock);
        slideUnlock.addUnlockListeners(new SlideUnlock.OnUnlockListener() {
            @Override
            public void onUnlock() {
                finish();
            }
        });
    }
}
