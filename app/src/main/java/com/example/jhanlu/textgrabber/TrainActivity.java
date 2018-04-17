package com.example.jhanlu.textgrabber;

import android.content.res.AssetFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jhanlu.textgrabber.util.TransDigit;

import java.io.FileDescriptor;
import java.lang.reflect.Array;
import java.util.Arrays;

public class TrainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);
    }

    public void clear(View view) {
        LinePathView pathView = (LinePathView) findViewById(R.id.view2);
        pathView.clear();
    }

    public void train(View view) {
        LinePathView pathView = (LinePathView) findViewById(R.id.view2);
        EditText tv = (EditText) findViewById(R.id.editText);
        String message = tv.getText().toString();
        TransDigit td = new TransDigit();

        if(pathView.getTouched()) {
            if(message.equals("") || td.digitToStirng(message)==null) {
                Toast.makeText(this, "请输入有效数字或英文字母", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    pathView.saveSample("/sdcard/pic.png", td.digitToStirng(message));
                    pathView.clear();
                    Toast.makeText(this, "训练成功", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(this, "训练成功", Toast.LENGTH_SHORT).show();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(this, "您没有写字~", Toast.LENGTH_SHORT).show();
        }
    }
}
