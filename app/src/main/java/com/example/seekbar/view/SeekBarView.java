package com.example.seekbar.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private static final int DEFAULT_INDICATOR_WIDTH = 50; // 默认指示器的宽度
    private static final int DEFAULT_SEEK_BAR_HEIGHT = 20; // 默认进度条的高度
    private static final int DEFAULT_PADDING_SPACING = 40; // 默认左右的padding值
    private static final int DEFAULT_SPACE_HEIGHT = 20; // 默认刻度线的高度

    private int minValue;
    private int maxValue;
    private int spacingValue; // 平均刻度
    private int seekBarY = 0; // 进度条的y坐标
    private int upWareY = 0; // 向上箭头的y坐标
    private int downWareY = 0; // 向下箭头的y坐标
    private int showTextY = 0; // 显示进度的y坐标
    // 实际更新的值，根据两个的坐标刷新布局
    private int leftWareX = 0; // 左边向上箭头的x坐标
    private int rightWareX = 0; // 右边向下箭头的x坐标
    private int clickType = 0; // 手指落下在哪个箭头上，0：left，1：right

    private Paint paint;

    private Bitmap upwardBitmap;
    private Bitmap downwardBitmap;

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

        upwardBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_upward);
        downwardBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_downward);
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

        // 初始化进度条的y坐标
        seekBarY = height / 2 + DEFAULT_TEXT_HEIGHT;
        upWareY = seekBarY;
        downWareY = seekBarY - downwardBitmap.getHeight();
        showTextY = seekBarY - downwardBitmap.getHeight() - DEFAULT_TEXT_HEIGHT;
        leftWareX = DEFAULT_PADDING_SPACING;
        rightWareX = getWidth() - DEFAULT_PADDING_SPACING;
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
        drawTextShow("0", clickType, showTextY, canvas);
        drawIndicator(leftWareX, upWareY, upwardBitmap, canvas);
        drawIndicator(rightWareX, downWareY, downwardBitmap, canvas);
    }

    /**
     * 画进度条
     */
    private void drawSeekBar(Canvas canvas) {
        int width = getWidth();
        int lastWidth = width - DEFAULT_PADDING_SPACING * 2; // 计算剩余的宽度

        resetPaint(Color.RED, DEFAULT_SEEK_BAR_HEIGHT, Paint.Style.FILL);
        canvas.drawLine(DEFAULT_PADDING_SPACING, seekBarY, width - DEFAULT_PADDING_SPACING, seekBarY, paint);

        int num = (maxValue - minValue) / spacingValue; // 计算出多少个间隔
        int spacingWidth = lastWidth / num; // 一个间距占的宽度
        for (int i = 1; i < num; i++) {
            drawSpace(i * spacingWidth + DEFAULT_PADDING_SPACING, seekBarY - DEFAULT_SEEK_BAR_HEIGHT / 2 - DEFAULT_SPACE_HEIGHT, canvas);
            String text = String.valueOf(minValue + spacingValue * i);
            // 画刻度值，x轴减去字体宽度的一半，字体居中
            drawSpaceText(text, i * spacingWidth + DEFAULT_PADDING_SPACING, seekBarY - DEFAULT_SEEK_BAR_HEIGHT / 2 - DEFAULT_SPACE_HEIGHT, canvas);
        }
    }

    /**
     * 画刻度线
     */
    private void drawSpace(int x, int y, Canvas canvas) {
        resetPaint(Color.GRAY, 2, Paint.Style.FILL);
        canvas.drawLine(x, y, x, y + DEFAULT_SPACE_HEIGHT, paint);
    }

    /**
     * 画刻度
     */
    private void drawSpaceText(String text, int x, int y, Canvas canvas) {
        resetPaint(Color.GRAY, 2, Paint.Style.FILL);
        canvas.drawText(text, x - getTextWidth(text) / 2, y - 4, paint); // 让刻度值与刻度有4像素的间距
    }

    /**
     * 或指示器
     */
    private void drawIndicator(int x, int y, Bitmap bitmap, Canvas canvas) {
        int width = bitmap.getWidth();
        canvas.drawBitmap(bitmap, x - width / 2, y, paint);
    }

    /**
     * 滑动过程中刻度会跟着显示
     * 跟着当前指示针的位置显示
     */
    private void drawTextShow(String text, int clickType, int y, Canvas canvas) {
        resetPaint(Color.RED, 2, Paint.Style.FILL);
        int x;
        if (clickType == 0) {
            x = leftWareX;
        } else {
            x = rightWareX;
        }
        canvas.drawText(text, x - getTextWidth(text) / 2, y, paint);
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
