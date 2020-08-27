package com.qx.pjbj.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.blankj.utilcode.util.ConvertUtils;
import com.qx.pjbj.R;


/**
 * 自定义圆角图片ImageView
 * Create by QianXiao
 * On 2020/6/24
 * com.qx.pjbj.view.OvalImageView
 * 参考：https://blog.csdn.net/u013408061/article/details/96899207
 */
public class OvalImageView extends AppCompatImageView {
    /**
     * 圆角半径 单位:dp
     */
    private float radius = 8;
    private final String RADIU = "radius";

    //8个数值，分四组，分别对应每个角所使用的椭圆的横轴半径和纵轴半径，如｛x1,y1,x2,y2,x3,y3,x4,y4｝
    private float[] rids = {
            ConvertUtils.dp2px(radius), ConvertUtils.dp2px(radius),//左上角
            ConvertUtils.dp2px(radius), ConvertUtils.dp2px(radius),//右上角
            ConvertUtils.dp2px(radius), ConvertUtils.dp2px(radius),//右下角
            ConvertUtils.dp2px(radius), ConvertUtils.dp2px(radius)};//左下角

    private void refreshRids(){
        rids = new float[]{
                ConvertUtils.dp2px(radius), ConvertUtils.dp2px(radius),//左上角
                ConvertUtils.dp2px(radius), ConvertUtils.dp2px(radius),//右上角
                ConvertUtils.dp2px(radius), ConvertUtils.dp2px(radius),//右下角
                ConvertUtils.dp2px(radius), ConvertUtils.dp2px(radius)};
    }

    public OvalImageView(Context context) {
        super(context);
    }


    public OvalImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("Recycle") TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.OvalImageView);
        radius = ta.getDimensionPixelSize(R.styleable.OvalImageView_radius,8);
        refreshRids();
    }


    public OvalImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        @SuppressLint("Recycle") TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.OvalImageView);
        radius = ta.getDimensionPixelSize(R.styleable.OvalImageView_radius,8);
        refreshRids();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, width);
    }

    /**
     * 画图
     *
     * @param canvas
     */
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        int w = this.getWidth();
        int h = this.getHeight();
        /*向路径中添加圆角矩形。radii数组定义圆角矩形的四个圆角的x,y半径。radii必须传入8个数值，*/
        path.addRoundRect(new RectF(0, 0, w, h), rids, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }
}
