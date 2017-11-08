package com.example.vinnie.facedetectiongooglevisionapi;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity implements CompoundButton.OnCheckedChangeListener {

    Switch developerSwitch;
    public boolean developer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BottomNavigationView btmNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        btmNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_home:
                        Intent home = new Intent(SettingsActivity.this, HomeActivity.class);
                        startActivity(home);
                        break;
                }
                return false;
            }
        });

        developerSwitch = (Switch) findViewById(R.id.developerSwitch);
        developerSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b){
        if (developerSwitch.isChecked()) {
            developer = true;
        } else {
            developer = false;
        }
    }

    public boolean getDeveloper() {
        return developer;
    }
}
