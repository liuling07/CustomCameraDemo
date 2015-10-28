package com.bbk.lling.camerademo;

import android.animation.AnimatorSet;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.bbk.lling.camerademo.utils.Utils;

/**
 * @Class: ShowCropperedActivity
 * @Description: 显示截图结果界面
 * @author: lling(www.cnblogs.com/liuling)
 * @Date: 2015/10/25
 */
public class ShowCropperedActivity extends Activity {
    private static final String TAG = "ShowCropperedActivity";
    ImageView imageView;
    int beginHeight, endHeight, beginWidht, endWidth;
    CropperImage cropperImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_croppered);
        imageView = (ImageView) findViewById(R.id.image);
        String path = getIntent().getStringExtra("path");
        cropperImage = (CropperImage) getIntent().getSerializableExtra("cropperImage");
        int width = getIntent().getIntExtra("width", 0);
        int height = getIntent().getIntExtra("height", 0);
        beginWidht = (int)cropperImage.getHeight();
        beginHeight = (int)cropperImage.getWidth();
        if(width != 0 && height != 0) {
            int screenWidth = Utils.getWidthInPx(this);
            float scale = (float)screenWidth/(float)width;
            final ViewGroup.LayoutParams lp = imageView.getLayoutParams();
            int imgHeight = (int)(scale * height);
            endWidth = screenWidth;
            endHeight = imgHeight;
            lp.height = imgHeight;
            imageView.setLayoutParams(lp);
            Log.e(TAG, "imageView.getLayoutParams().width:" + imageView.getLayoutParams().width);
        }
        imageView.setImageURI(getIntent().getData());
        doAnimation();

    }

    private void doAnimation() {

        AnimatorSet animSet = new AnimatorSet();
        ValueAnimator scaleAnimator = getScaleAnimator(imageView, beginWidht, endWidth, beginHeight, endHeight);
        ObjectAnimator translationX = ObjectAnimator.ofFloat(imageView, "translationX", cropperImage.getX(), 0);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(imageView, "translationY", cropperImage.getY(), 0);
        /*ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, scaleX);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, scaleX);*/
//        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(imageView, "rotation", 90f, 0f);
        animSet.play(translationX).with(translationY)/*.with(rotateAnimator)*/.with(scaleAnimator);
        animSet.setInterpolator(new LinearInterpolator());
        animSet.setDuration(1000L);
        animSet.start();
        RotateAnimation rotateAnimation = new RotateAnimation(90, 0,
                Animation.ABSOLUTE, cropperImage.getX() + cropperImage.getWidth()/2 ,Animation.ABSOLUTE, cropperImage.getY() + cropperImage.getWidth()/2);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(1000L);
        imageView.startAnimation(rotateAnimation);
    }

    public static ValueAnimator getScaleAnimator(final View target, final int startWidth, final int endWidth
            , final int startHeight, final int endHeight) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            //持有一个IntEvaluator对象，方便下面估值的时候使用
            private IntEvaluator mEvaluator = new IntEvaluator();
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                //获得当前动画的进度值，整型，1-100之间
                int currentValue = (Integer) animator.getAnimatedValue();
                //计算当前进度占整个动画过程的比例，浮点型，0-1之间
                float fraction = currentValue / 100f;
                //这里我偷懒了，不过有现成的干吗不用呢
                //直接调用整型估值器通过比例计算出宽度，然后再设给Button
                target.getLayoutParams().width = mEvaluator.evaluate(fraction, startWidth, endWidth);
                target.getLayoutParams().height = mEvaluator.evaluate(fraction, startHeight, endHeight);
                target.requestLayout();
            }
        });
        return valueAnimator;
    }

}
