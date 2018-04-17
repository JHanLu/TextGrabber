package com.example.jhanlu.textgrabber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
    }

    public void onTrain(View view) {
        Intent intent = new Intent(this, TrainActivity.class);
        startActivity(intent);
    }

    public void onTest(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
