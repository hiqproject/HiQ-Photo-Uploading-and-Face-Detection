package com.example.vinnie.facedetectiongooglevisionapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class HomeActivity extends Activity implements View.OnClickListener {

    /* Declare Buttons(LinearLayouts) */
    LinearLayout bInstructions, bIdCard, bSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bInstructions = (LinearLayout) findViewById(R.id.bInstructions);
        bIdCard = (LinearLayout) findViewById(R.id.bIdCard);
        bSettings = (LinearLayout) findViewById(R.id.bSettings);

        bInstructions.setOnClickListener(this);
        bIdCard.setOnClickListener(this);
        bSettings.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.bInstructions:
                Intent instructions = new Intent(HomeActivity.this, InstructionsActivity.class);
                startActivity(instructions);
                break;
            case R.id.bIdCard:
                Intent idCardDeveloper = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(idCardDeveloper);
                break;
            case R.id.bSettings:
                Intent settings = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(settings);
                break;
        }
    }
}




