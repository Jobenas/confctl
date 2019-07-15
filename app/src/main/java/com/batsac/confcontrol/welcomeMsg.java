package com.batsac.confcontrol;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class welcomeMsg extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_msg);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       final ImageButton presentationButton = findViewById(R.id.presentationButton);
       final ImageButton settingsButton = findViewById(R.id.settingsButton);

       presentationButton.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View view, MotionEvent motionEvent) {

               switch(motionEvent.getAction())
               {
                   case MotionEvent.ACTION_DOWN:
                       presentationButton.setBackgroundResource(R.drawable.b16p);

                       return true;
                   case MotionEvent.ACTION_UP:
                       presentationButton.setBackgroundResource(R.drawable.b16);

                       Intent intent = new Intent(getBaseContext(), presentModeActivity.class);
                       startActivity(intent);

                       return true;
               }

               return false;
           }
       });

       settingsButton.setOnTouchListener(new View.OnTouchListener() {
           @Override
           public boolean onTouch(View view, MotionEvent motionEvent) {

               switch (motionEvent.getAction())
               {
                   case MotionEvent.ACTION_DOWN:
                       settingsButton.setBackgroundResource(R.drawable.b17p);

                       return true;
                   case MotionEvent.ACTION_UP:
                       settingsButton.setBackgroundResource(R.drawable.b17);

                       Intent intent = new Intent(getBaseContext(), settingsActivity.class);
                       startActivity(intent);

                       return true;
               }

               return false;
           }
       });

    }

}
