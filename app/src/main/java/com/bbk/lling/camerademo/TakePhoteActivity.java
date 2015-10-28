package com.bbk.lling.camerademo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bbk.lling.camerademo.camare.CameraPreview;
import com.bbk.lling.camerademo.camare.FocusView;
import com.bbk.lling.camerademo.cropper.CropImageView;
import com.bbk.lling.camerademo.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @Class: TakePhoteActivity
 * @Description: 拍照界面
 * @author: lling(www.cnblogs.com/liuling)
 * @Date: 2015/10/25
 */
public class TakePhoteActivity extends Activity implements CameraPreview.OnCameraStatusListener,
        SensorEventListener {
    private static final String TAG = "TakePhoteActivity";
    public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final String PATH = Environment.getExternalStorageDirectory()
            .toString() + "/AndroidMedia/";
    CameraPreview mCameraPreview;
    CropImageView mCropImageView;
    RelativeLayout mTakePhotoLayout;
    LinearLayout mCropperLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置横屏
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_take_phote);
        // Initialize components of the app
        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);
        mCameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        FocusView focusView = (FocusView) findViewById(R.id.view_focus);
        mTakePhotoLayout = (RelativeLayout) findViewById(R.id.take_photo_layout);
        mCropperLayout = (LinearLayout) findViewById(R.id.cropper_layout);

        mCameraPreview.setFocusView(focusView);
        mCameraPreview.setOnCameraStatusListener(this);
        mCropImageView.setGuidelines(2);

        mSensorManager = (SensorManager) getSystemService(Context.
                SENSOR_SERVICE);
        mAccel = mSensorManager.getDefaultSensor(Sensor.
                TYPE_ACCELEROMETER);

    }

    boolean isRotated = false;

    @Override
    protected void onResume() {
        super.onResume();
        if(!isRotated) {
            TextView hint_tv = (TextView) findViewById(R.id.hint);
            ObjectAnimator animator = ObjectAnimator.ofFloat(hint_tv, "rotation", 0f, 90f);
            animator.setStartDelay(800);
            animator.setDuration(1000);
            animator.setInterpolator(new LinearInterpolator());
            animator.start();
            View view =  findViewById(R.id.crop_hint);
            AnimatorSet animSet = new AnimatorSet();
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "rotation", 0f, 90f);
            ObjectAnimator moveIn = ObjectAnimator.ofFloat(view, "translationX", 0f, -50f);
            animSet.play(animator1).before(moveIn);
            animSet.setDuration(10);
            animSet.start();
            isRotated = true;
        }
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    public void takePhoto(View view) {
        if(mCameraPreview != null) {
            mCameraPreview.takePicture();
        }
    }

    public void close(View view) {
        finish();
    }

    /**
     * 关闭截图界面
     * @param view
     */
    public void closeCropper(View view) {
        showTakePhotoLayout();
    }

    /**
     * 开始截图，并保存图片
     * @param view
     */
    public void startCropper(View view) {
        //获取截图并旋转90度
        CropperImage cropperImage = mCropImageView.getCroppedImage();
        Log.e(TAG, cropperImage.getX() + "," + cropperImage.getY());
        Log.e(TAG, cropperImage.getWidth() + "," + cropperImage.getHeight());
        Bitmap bitmap = Utils.rotate(cropperImage.getBitmap(), -90);
//        Bitmap bitmap = mCropImageView.getCroppedImage();
        // 系统时间
        long dateTaken = System.currentTimeMillis();
        // 图像名称
        String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken)
                .toString() + ".jpg";
        Uri uri = insertImage(getContentResolver(), filename, dateTaken, PATH,
                filename, bitmap, null);
        cropperImage.getBitmap().recycle();
        cropperImage.setBitmap(null);
        Intent intent = new Intent(this, ShowCropperedActivity.class);
        intent.setData(uri);
        intent.putExtra("path", PATH + filename);
        intent.putExtra("width", bitmap.getWidth());
        intent.putExtra("height", bitmap.getHeight());
        intent.putExtra("cropperImage", cropperImage);
        startActivity(intent);
        bitmap.recycle();
        finish();
        super.overridePendingTransition(R.anim.fade_in,
                R.anim.fade_out);
//        doAnimation(cropperImage);
    }

    private void doAnimation(CropperImage cropperImage) {
        ImageView imageView = new ImageView(this);
        View view = LayoutInflater.from(this).inflate(
                R.layout.image_view_layout, null);
        ((RelativeLayout) view.findViewById(R.id.root_layout)).addView(imageView);
        RelativeLayout relativeLayout = ((RelativeLayout) findViewById(R.id.root_layout));
//        relativeLayout.addView(imageView);
        imageView.setX(cropperImage.getX());
        imageView.setY(cropperImage.getY());
        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        lp.width = (int)cropperImage.getWidth();
        lp.height = (int) cropperImage.getHeight();
        imageView.setLayoutParams(lp);
        imageView.setImageBitmap(cropperImage.getBitmap());
        try {
            getWindow().addContentView(view, lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*AnimatorSet animSet = new AnimatorSet();
        ObjectAnimator translationX = ObjectAnimator.ofFloat(this, "translationX", cropperImage.getX(), 0);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this, "translationY", cropperImage.getY(), 0);*/

        TranslateAnimation translateAnimation = new TranslateAnimation(
                0, -cropperImage.getX(), 0, -(Math.abs(cropperImage.getHeight() - cropperImage.getY())));// 当前位置移动到指定位置
        RotateAnimation rotateAnimation = new RotateAnimation(0, -90,
                Animation.ABSOLUTE, cropperImage.getX() ,Animation.ABSOLUTE, cropperImage.getY());
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(rotateAnimation);
        animationSet.setFillAfter(true);
        animationSet.setDuration(2000L);
        imageView.startAnimation(animationSet);
//        finish();
    }

    /**
     * 拍照成功后回调
     * 存储图片并显示截图界面
     * @param data
     */
    @Override
    public void onCameraStopped(byte[] data) {
        Log.i("TAG", "==onCameraStopped==");
        // 创建图像
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        // 系统时间
        long dateTaken = System.currentTimeMillis();
        // 图像名称
        String filename = DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken)
                .toString() + ".jpg";
        // 存储图像（PATH目录）
        Uri source = insertImage(getContentResolver(), filename, dateTaken, PATH,
                filename, bitmap, data);
        //准备截图
        try {
            mCropImageView.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), source));
//            mCropImageView.rotateImage(90);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        showCropperLayout();
    }

    /**
     * 存储图像并将信息添加入媒体数据库
     */
    private Uri insertImage(ContentResolver cr, String name, long dateTaken,
                            String directory, String filename, Bitmap source, byte[] jpegData) {
        OutputStream outputStream = null;
        String filePath = directory + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(directory, filename);
            if (file.createNewFile()) {
                outputStream = new FileOutputStream(file);
                if (source != null) {
                    source.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                } else {
                    outputStream.write(jpegData);
                }
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable t) {
                }
            }
        }
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Images.Media.TITLE, name);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATA, filePath);
        return cr.insert(IMAGE_URI, values);
    }

    private void showTakePhotoLayout() {
        mTakePhotoLayout.setVisibility(View.VISIBLE);
        mCropperLayout.setVisibility(View.GONE);
    }

    private void showCropperLayout() {
        mTakePhotoLayout.setVisibility(View.GONE);
        mCropperLayout.setVisibility(View.VISIBLE);
        mCameraPreview.start();   //继续启动摄像头
    }


    private float mLastX = 0;
    private float mLastY = 0;
    private float mLastZ = 0;
    private boolean mInitialized = false;
    private SensorManager mSensorManager;
    private Sensor mAccel;
    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!mInitialized){
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }
        float deltaX  = Math.abs(mLastX - x);
        float deltaY = Math.abs(mLastY - y);
        float deltaZ = Math.abs(mLastZ - z);

        if(deltaX > 0.8 || deltaY > 0.8 || deltaZ > 0.8){
            mCameraPreview.setFocus();
        }
        mLastX = x;
        mLastY = y;
        mLastZ = z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
