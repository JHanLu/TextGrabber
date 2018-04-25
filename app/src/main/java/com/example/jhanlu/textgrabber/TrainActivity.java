package com.example.jhanlu.textgrabber;

import android.content.res.AssetFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jhanlu.textgrabber.util.TransDigit;

import java.io.File;
import java.io.FileDescriptor;
import java.lang.reflect.Array;
import java.util.Arrays;

public class TrainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

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
                double []in = new double [28*28];
                double []out = new double [6];
                String []outstr = new String [6];
                outstr = td.digitToStirng(message).split(" ");
                for(int i=0; i<6; i++) {
                    out[i] = Double.parseDouble(outstr[i]);
                }
                try {
                    in = pathView.saveSample("/sdcard/pic.png", td.digitToStirng(message)); // 保存手写字符为训练数据
                    if(fileIsExists("/sdcard/bp.dat")) { // sd卡有训练好的权值
                        Log.d("train", "从sd卡读取");
                        trainSample(in, out);
                    } else {
                        Log.d("train", "从raw读取");
                        AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.bp);
                        if(afd != null) {
                            System.out.println("afd读取成功");
                            FileDescriptor fd = afd.getFileDescriptor();
                            int off = (int) afd.getStartOffset();
                            trainSample1(in, out, fd, off);
                        }
                    }
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

    public boolean fileIsExists(String str) {
        try {
            File f = new File(str);
            if(!f.exists()) {
                return false;
            }
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    public native void trainSample(double []Attrin, double []Attrout);
    public native void trainSample1(double []Attrin, double []Attrout, FileDescriptor fd, long off);
}
