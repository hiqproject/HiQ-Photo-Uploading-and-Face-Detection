package com.example.vinnie.facedetectiongooglevisionapi;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class InstructionsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        BottomNavigationView btmNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        btmNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_home:
                        Intent home = new Intent(InstructionsActivity.this, HomeActivity.class);
                        startActivity(home);
                        break;
                }
                return false;
            }
        });

        TextView headerTextOne = (TextView) findViewById(R.id.headerText);
        headerTextOne.setText(" General Overview");
        headerTextOne.setTextSize(22);
        headerTextOne.setTextColor(Color.BLACK);
        headerTextOne.setTypeface(headerTextOne.getTypeface(), Typeface.BOLD);

        TextView bodyTextOne = (TextView) findViewById(R.id.bodyText);
        bodyTextOne.setText("  This application is used as a tool to allow students \n  to capture a photo" +
                " for submission to generate a \n  QUT student ID card. \n\n  The app works by scanning a submitted" +
                " photo to \n  check if it contains a face and meets the requirements \n  for a student ID photo. \n\n" +
                "  If the photo does not meet the requirements, you will \n  be prompted to resubmit another" +
                " photo. Upon \n  successful submission, your photo will be sent to \n  QUT HiQ to generate your" +
                " student ID card.");
        bodyTextOne.setTextColor(Color.BLACK);

        TextView headerTextTwo = (TextView) findViewById(R.id.header2Text);
        headerTextTwo.setText("  \n Steps for Submission");
        headerTextTwo.setTextSize(22);
        headerTextTwo.setTextColor(Color.BLACK);
        headerTextTwo.setTypeface(headerTextTwo.getTypeface(), Typeface.BOLD);

        TextView bodyTextTwo = (TextView) findViewById(R.id.body2Text);
        bodyTextTwo.setText("  1. On the home menu select 'ID Photo'. \n\n" +
                "  2. Select 'Choose Photo' to choose a photo from your \n      photo library" +
                " or alternatively, select 'Capture Photo' \n      to take a new photo. \n\n" +
                "  3. Once the photo appears on the screen and you are \n      happy with it, select 'Submit'. \n" +
                "      NOTE: If you are not happy with the photo, you can \n      choose to select or take a" +
                " new one by following \n      Step 2. \n\n" +
                "  4. Once submission is successful, you have completed \n      the process and your student ID" +
                "  will be available for \n      collection at the QUT HiQ services.");
        bodyTextTwo.setTextColor(Color.BLACK);

        TextView headerTextThree = (TextView) findViewById(R.id.header3Text);
        headerTextThree.setText("  \n General Tips");
        headerTextThree.setTextSize(22);
        headerTextThree.setTextColor(Color.BLACK);
        headerTextThree.setTypeface(headerTextThree.getTypeface(), Typeface.BOLD);

        TextView bodyTextThree = (TextView) findViewById(R.id.body3Text);
        bodyTextThree.setText("  - When taking a photo, be sure to hold you phone \n    approximately" +
                " 25 - 35cm in front of your face. \n\n" +
                "  - Make sure to keep your face completely frontal and \n" +
                "    directly facing the camera. \n\n" +
                "  - Keep your eyes wide open and directly looking at the \n" +
                "    camera. \n\n" +
                "  - Make sure that hair or head wear is not obstructing \n" +
                "    your face. \n\n");
        bodyTextThree.setTextColor(Color.BLACK);
    }
}
