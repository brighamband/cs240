package edu.byu.cs240.familymapclient.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import edu.byu.cs240.familymapclient.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getActionBar().setTitle("testing");
    }
}