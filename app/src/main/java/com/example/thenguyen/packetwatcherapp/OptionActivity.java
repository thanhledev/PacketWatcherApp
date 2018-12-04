package com.example.thenguyen.packetwatcherapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class OptionActivity extends AppCompatActivity {

    Button serverBtn;
    Button manualClientBtn;
    Button autoClientBtn;
    Intent newScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        // get buttons
        serverBtn = findViewById(R.id.serverScreen);
        manualClientBtn = findViewById(R.id.manualClientScreen);
        autoClientBtn = findViewById(R.id.autoClientScreen);

        // handle click
        serverBtn.setOnClickListener((v) -> {
            newScreen = new Intent(this, ServerActivity.class);
            startActivity(newScreen);
        });

        manualClientBtn.setOnClickListener((v) -> {
            newScreen = new Intent(this, ManualClientActivity.class);
            startActivity(newScreen);
        });

        autoClientBtn.setOnClickListener((v) -> {
            newScreen = new Intent(this, AutoClientActivity.class);
            startActivity(newScreen);
        });
    }
}
