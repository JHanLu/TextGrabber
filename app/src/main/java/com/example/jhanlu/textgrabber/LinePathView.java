package com.example.jhanlu.textgrabber;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class LinePathView extends View {

    private Context mContext;

    /**
     * 笔画X坐标起点
     */
    private float mX;
    /**
     * 笔画Y坐标起点
     */
    private float mY;
    /**
     * 手写画笔
     */
    private final Paint mGesturePaint = new Paint();
    /**
     * 路径
     */
    private final Path mPath = new Path();
    /**
     * 签名画笔
     */
    private Canvas cacheCanvas;
    /**
     * 签名画布
     */
    private Bitmap cachebBitmap;
    /**
     * 是否已经画图
     */
    private boolean isTouched = false;
    /**
     * 画笔宽度 px；
     */
    private int mPaintWidth = 80;
    /**
     * 前景色
     */
    private int mPenColor = Color.BLACK;
    /**
     * 背景色（指最终写入结果文件的背景颜色，默认为白色）
     */
    private int mBackColor=Color.WHITE;

    public LinePathView(Context context) {
        super(context);
        init(context);
    }

    public LinePathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LinePathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        this.mContext = context;
        //设置抗锯齿
        mGesturePaint.setAntiAlias(true);
        mGesturePaint.setFilterBitmap(true);
        //设置签名笔画样式
        mGesturePaint.setStyle(Paint.Style.STROKE);
        //设置笔画宽度
        mGesturePaint.setStrokeWidth(mPaintWidth);
        //设置签名颜色
        mGesturePaint.setColor(mPenColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //创建跟view一样大的bitmap，用来保存签名
        cachebBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas(cachebBitmap);
        cacheCanvas.drawColor(mBackColor);
        isTouched=false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                isTouched = true;
                touchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                //将路径画到bitmap中，即一次笔画完成才去更新bitmap，而手势轨迹是实时显示在画板上的。
                cacheCanvas.drawPath(mPath, mGesturePaint);
                mPath.reset();
                break;
        }
        // 更新绘制
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画此次笔画之前的签名
        canvas.drawBitmap(cachebBitmap, 0, 0, mGesturePaint);
        // 通过画布绘制多点形成的图形
        canvas.drawPath(mPath, mGesturePaint);
    }

    // 手指点下屏幕时调用
    private void touchDown(MotionEvent event) {
        // 重置绘制路线
        mPath.reset();
        float x = event.getX();
        float y = event.getY();
        mX = x;
        mY = y;
        // mPath绘制的绘制起点
        mPath.moveTo(x, y);
    }

    // 手指在屏幕上滑动时调用
    private void touchMove(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        final float previousX = mX;
        final float previousY = mY;
        final float dx = Math.abs(x - previousX);
        final float dy = Math.abs(y - previousY);
        // 两点之间的距离大于等于2时，生成贝塞尔绘制曲线
        if (dx >= 2 || dy >= 2) {
            // 设置贝塞尔曲线的操作点为起点和终点的一半
            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;
            // 二次贝塞尔，实现平滑曲线；previousX, previousY为操作点，cX, cY为终点
            mPath.quadTo(previousX, previousY, cX, cY);
            // 第二次执行时，第一次结束调用的坐标值将作为第二次调用的初始坐标值
            mX = x;
            mY = y;
        }
    }
    /**
     * 清除画板
     */
    public void clear() {
        if (cacheCanvas != null) {
            isTouched = false;
            //更新画板信息
            mGesturePaint.setColor(mPenColor);
            cacheCanvas.drawColor(mBackColor);
            mGesturePaint.setColor(mPenColor);
            invalidate();
        }
    }


    /**
     * 保存画板
     * @param path 保存到路径
     */
    public double [] save(String path)  throws IOException {
        return save(path, false, 0);
    }

    /**
     * 保存画板
     * @param path       保存到路径
     * @param clearBlank 是否清除边缘空白区域
     * @param blank  要保留的边缘空白距离
     */
    public double [] save(String path, boolean clearBlank, int blank) throws IOException {

        Bitmap bitmap=cachebBitmap;
        if (clearBlank) {
            bitmap = clearBlank(bitmap, blank);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] buffer = bos.toByteArray();
        if (buffer != null) {
            File file = new File("/sdcard/qm1.png");
            if (file.exists()) {
                file.delete();
            }
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(buffer);
            outputStream.close();
        }

       bitmap = scaleBitmap(bitmap, 28, 28);
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos1);
        byte[] buffer1 = bos1.toByteArray();
        if (buffer1 != null) {
            File file1 = new File(path);
            if (file1.exists()) {
                file1.delete();
            }
            OutputStream outputStream1 = new FileOutputStream(file1);
            outputStream1.write(buffer1);
            outputStream1.close();
        }

        //输出bitmap
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        ArrayList list = new ArrayList();
        for(int i=0; i< w; i++) {
            for(int j=0; j<h; j++) {
                int color = bitmap.getPixel(i, j);
                double g = ((30*Color.red(color) + 59*Color.green(color) + 11*Color.blue(color))/100)/255.0;
                String gnum = roundByScale(g, 6);
                list.add(gnum);
            }
        }

        System.out.println("高：" + h + " 宽：" + w );
        double []arr = new double[28*28]; // 存放bitmap的数组，和list值一样
        for (int i = 0; i < list.size(); i++) {
            double dou = Double.parseDouble((String)list.get(i));
            BigDecimal bg = new BigDecimal(dou);
            double f1 = bg.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
            arr[i] = f1;
        }

        //输出结果到pic.txt
        try {
            FileWriter fw = null;
            File file1 = new File("/sdcard/img.txt");
            fw = new FileWriter(file1, false);
            PrintWriter pw = new PrintWriter(fw);
            for (int i = 0; i < list.size(); i++) {
                if (i == list.size() - 1) {
                    pw.println(list.get(i));
                } else {
                    pw.print(list.get(i) + " ");
                }
            }
            pw.flush();
            fw.flush();
            pw.close();
            fw.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    public double [] saveSample(String path, String str) throws IOException {
        Bitmap bitmap=cachebBitmap;
        bitmap = clearBlank(bitmap, 0);
        bitmap = scaleBitmap(bitmap, 28, 28);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        System.out.println("宽："+w+" 高："+h);
        ArrayList list = new ArrayList();
        for(int i=0; i< w; i++) {
            for(int j=0; j<h; j++) {
                int color = bitmap.getPixel(i, j);
                double g = ((30*Color.red(color) + 59*Color.green(color) + 11*Color.blue(color))/100)/255.0;
                String gnum = roundByScale(g, 6);
                list.add(gnum);
            }
        }

        double []arr = new double[28*28]; // 存放bitmap的数组，和list值一样
        for (int i = 0; i < list.size(); i++) {
            double dou = Double.parseDouble((String)list.get(i));
            BigDecimal bg = new BigDecimal(dou);
            double f1 = bg.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
            arr[i] = f1;
        }
        //输出结果到samples.txt
        try {
            FileWriter fw = null;
            File file1 = new File("/sdcard/samples.txt");
            fw = new FileWriter(file1, true);
            PrintWriter pw = new PrintWriter(fw);
            for (int i = 0; i < list.size(); i++) {
                if (i == list.size() - 1) {
                    pw.println(list.get(i));
                } else {
                    pw.print(list.get(i) + " ");
                }
            }
            pw.println(str);
            pw.flush();
            fw.flush();
            pw.close();
            fw.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

    //固定几位小数
    public static String roundByScale(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The   scale   must   be   a   positive   integer   or   zero");
        }
        if(scale == 0){
            return new DecimalFormat("0").format(v);
        }
        String formatStr = "0.";
        for(int i=0;i<scale;i++){
            formatStr = formatStr + "0";
        }
        return new DecimalFormat(formatStr).format(v);
    }

    /**
     * 缩小bitmap
     * @param origin
     * @param newWidth
     * @param newHeight
     * @return
     */
    //TODO 缩放不平滑
    public Bitmap scaleBitmap(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, true);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

    /**
     * 逐行扫描 清楚边界空白。
     *
     * @param bp
     * @param blank 边距留多少个像素
     * @return
     */
    private Bitmap clearBlank(Bitmap bp, int blank) {
        int HEIGHT = bp.getHeight();
        int WIDTH = bp.getWidth();
        int top = 0, left = 0, right = 0, bottom = 0;
        int[] pixs = new int[WIDTH];
        boolean isStop;
        //扫描上边距不等于背景颜色的第一个点
        for (int y = 0; y < HEIGHT; y++) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    top = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        //扫描下边距不等于背景颜色的第一个点
        for (int y = HEIGHT - 1; y >= 0; y--) {
            bp.getPixels(pixs, 0, WIDTH, 0, y, WIDTH, 1);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    bottom = y;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        pixs = new int[HEIGHT];
        //扫描左边距不等于背景颜色的第一个点
        for (int x = 0; x < WIDTH; x++) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    left = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        //扫描右边距不等于背景颜色的第一个点
        for (int x = WIDTH - 1; x > 0; x--) {
            bp.getPixels(pixs, 0, 1, x, 0, 1, HEIGHT);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    right = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        if (blank < 0) {
            blank = 0;
        }
        //计算加上保留空白距离之后的图像大小
        left = left - blank > 0 ? left - blank : 0;
        top = top - blank > 0 ? top - blank : 0;
        right = right + blank > WIDTH - 1 ? WIDTH - 1 : right + blank;
        bottom = bottom + blank > HEIGHT - 1 ? HEIGHT - 1 : bottom + blank;
        return Bitmap.createBitmap(bp, left, top, right - left, bottom - top);
    }

    /**
     * 设置画笔宽度 默认宽度为60px
     *
     * @param mPaintWidth
     */
    public void setPaintWidth(int mPaintWidth) {
        mPaintWidth = mPaintWidth > 0 ? mPaintWidth : 60;
        this.mPaintWidth = mPaintWidth;
        mGesturePaint.setStrokeWidth(mPaintWidth);

    }

    public void setBackColor(@ColorInt int backColor)
    {
        mBackColor=backColor;
    }

    /**
     * 设置画笔颜色
     *
     * @param mPenColor
     */
    public void setPenColor(int mPenColor) {
        this.mPenColor = mPenColor;
        mGesturePaint.setColor(mPenColor);
    }

    /**
     * 是否有签名
     *
     * @return
     */
    public boolean getTouched() {
        return isTouched;
    }

}
