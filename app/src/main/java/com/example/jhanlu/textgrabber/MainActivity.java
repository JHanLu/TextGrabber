package com.example.jhanlu.textgrabber;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jhanlu.textgrabber.util.GetResult;

import java.io.File;
import java.io.FileDescriptor;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //6.0以上验证权限
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 101;
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                    return;
                }
            }
        }

    }

    public void clearView(View view) {
        LinePathView imgView = (LinePathView) findViewById(R.id.view);
        imgView.clear();
    }

    public void saveImg(View view) {
        LinePathView mPathView = (LinePathView) findViewById(R.id.view);
        if (mPathView.getTouched()) {
            try {
                double [] in = new double[28*28];
                double [] out = new double[6];
                in = mPathView.save("/sdcard/pic.png", true, 0);
                Toast.makeText(this, "图片已保存", Toast.LENGTH_SHORT).show();
                mPathView.clear();

                //读取训练文件bp.dat
                AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.bp);
                if(afd != null) {
                    System.out.println("afd读取成功");
                    FileDescriptor fd = afd.getFileDescriptor();
                    int off = (int) afd.getStartOffset();
                    int len = (int) afd.getLength();
                    out = stringFromJNI(in, fd, off, len);
                    System.out.println(Arrays.toString(out));

                    GetResult gr = new GetResult();
                    String output = gr.transform(out);
                    System.out.println(output);
                    TextView tv = findViewById(R.id.textView2);
                    tv.setText("识别结果：" + output);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //将bitmap保存到相册需扫描相册环境
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                File outputFile = new File("/sdcard/qm.png");
                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                final Uri contentUri = Uri.fromFile(outputFile);
                scanIntent.setData(contentUri);
                sendBroadcast(scanIntent);
            } else {
                final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
                sendBroadcast(intent);
            }
        } else {
            Toast.makeText(this, "您没有写字~", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native double[] stringFromJNI(double [] Attr, FileDescriptor fd, long off, long len);

}
