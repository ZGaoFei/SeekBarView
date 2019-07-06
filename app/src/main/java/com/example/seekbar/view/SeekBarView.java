package com.example.seekbar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.example.seekbar.R;

/**
 * 可变刻度进度条
 */
public class SeekBarView extends View {
    private static final int MIN = 0;
    private static final int MAX = 100;
    private static final int DEFAULT_SPACING = 10;
    private static final int DEFAULT_SPACING_PX = 20;
    private static final int DEFAULT_TEXT_HEIGHT = 20; // 默认刻度显示文字高度
    private static final int DEFAULT_INDICATOR_HEIGHT = 50; // 默认指示器的高度
    private static final int DEFAULT_SEEK_BAR_HEIGHT = 20; // 默认进度条的高度
    private static final int DEFAULT_PADDING_SPACING = 20; // 默认左右的padding值
    private static final int DEFAULT_SPACE_HEIGHT = 20; // 默认刻度线的高度

    private int minValue;
    private int maxValue;
    private int spacingValue; // 平均刻度

    private Paint paint;

    public SeekBarView(Context context) throws Exception {
        this(context, null);
    }

    public SeekBarView(Context context, AttributeSet attrs) throws Exception {
        this(context, attrs, 0);
    }

    public SeekBarView(Context context, AttributeSet attrs, int defStyleAttr) throws Exception {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) throws Exception {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarView);
        minValue = a.getInt(R.styleable.SeekBarView_min, MIN);
        maxValue = a.getInt(R.styleable.SeekBarView_max, MAX);
        if (maxValue <= minValue) {
            throw new Exception("SeekBarView max is must big min value!");
        }
        spacingValue = a.getInt(R.styleable.SeekBarView_spacing, DEFAULT_SPACING);
        if (spacingValue > maxValue - minValue) {
            throw new Exception("SeekBarView spacing is must min max-min value");
        }
        a.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        // 适配wrap_content
        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            // 如果是wrap_content，显示的宽度为最大值减去最小值除与间距值乘与默认间距像素
            width = (maxValue - minValue) / spacingValue * DEFAULT_SPACING_PX;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            // 如果是wrap_content，显示的高度为默认三部分的高度和
            height = DEFAULT_TEXT_HEIGHT + DEFAULT_SEEK_BAR_HEIGHT + DEFAULT_INDICATOR_HEIGHT * 2;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 交由父布局显示位置
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawSeekBar(canvas);
    }

    /**
     * 画进度条
     */
    private void drawSeekBar(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        int lastWidth = width - DEFAULT_PADDING_SPACING * 2; // 计算剩余的宽度
        int y = height / 2 + DEFAULT_TEXT_HEIGHT; // 计算进度条的初始y坐标

        resetPaint(Color.RED, DEFAULT_SEEK_BAR_HEIGHT, Paint.Style.FILL);
        canvas.drawLine(DEFAULT_PADDING_SPACING, y, width - DEFAULT_PADDING_SPACING, y, paint);

        int num = (maxValue - minValue) / spacingValue; // 计算出多少个间隔
        int spacingWidth = lastWidth / num; // 一个间距占的宽度
        for (int i = 1; i < num; i++) {
            drawSpace(i * spacingWidth + DEFAULT_PADDING_SPACING, y - DEFAULT_SEEK_BAR_HEIGHT / 2 - DEFAULT_SPACE_HEIGHT, canvas);
            String text = String.valueOf(minValue + spacingValue * i);
            // 画刻度值，x轴减去字体宽度的一半，字体居中
            drawSpaceText(text, i * spacingWidth + DEFAULT_PADDING_SPACING - getTextWidth(text) / 2, y - DEFAULT_SEEK_BAR_HEIGHT / 2 - DEFAULT_SPACE_HEIGHT, canvas);
        }
    }

    /**
     * 画刻度线
     */
    private void drawSpace(int x, int y, Canvas canvas) {
        resetPaint(Color.GRAY, 3, Paint.Style.FILL);
        canvas.drawLine(x, y, x, y + DEFAULT_SPACE_HEIGHT, paint);
    }

    /**
     * 画刻度
     */
    private void drawSpaceText(String text, int x, int y, Canvas canvas) {
        resetPaint(Color.GRAY, 2, Paint.Style.FILL);
        canvas.drawText(text, x, y, paint);
    }

    /**
     * 获取字体的宽度
     */
    private int getTextWidth(String text) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        }
        return (int) paint.measureText(text);
    }

    private void resetPaint(int color, float width, Paint.Style style) {
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(style);
        paint.setTextSize(20);
    }
}
