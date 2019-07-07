package com.example.seekbar.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
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
    private static final int DEFAULT_LEFT_RIGHT_SPACE = 20; // 默认刻度线的高度

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
    private int clickType = 0; // 手指落下在哪个箭头上，0：left，1：right, 2：outer
    private boolean isUpdate; // 手指是否离开屏幕，true：在屏幕上，false：离开
    private int[] spacings; // 每个间距的集合
    private int lastWidth; // 除去两边间距后剩余的宽度
    private int oneSpace; // 一个刻度的长度
    private int left;
    private int right;

    private Paint paint;

    private Bitmap upwardBitmap;
    private Bitmap downwardBitmap;

    private SeekBarUpdateListener updateListener;

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
        lastWidth = getWidth() - DEFAULT_PADDING_SPACING * 2;
        setLeft();
        setRight();
        if (spacings != null && spacings.length > 0) {
            oneSpace = lastWidth / spacings.length;
        } else {
            oneSpace = lastWidth / ((maxValue - minValue) / spacingValue);
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
        drawSpace(canvas);
        if (isUpdate) {
            drawTextShow(clickType, showTextY, canvas);
        }
        drawIndicator(leftWareX, upWareY, upwardBitmap, canvas);
        drawIndicator(rightWareX, downWareY, downwardBitmap, canvas);
    }

    /**
     * 画进度条
     */
    private void drawSeekBar(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        // 画进度条的背景
        resetPaint(Color.GRAY, DEFAULT_SEEK_BAR_HEIGHT, Paint.Style.FILL);
        canvas.drawLine(DEFAULT_PADDING_SPACING, height / 2 + DEFAULT_TEXT_HEIGHT, width - DEFAULT_PADDING_SPACING, height / 2 + DEFAULT_TEXT_HEIGHT, paint);
        // 画可变化的进度条
        resetPaint(Color.RED, DEFAULT_SEEK_BAR_HEIGHT, Paint.Style.FILL);
        canvas.drawLine(leftWareX, seekBarY, rightWareX, seekBarY, paint);
    }

    /**
     * 画刻度线
     */
    private void drawSpace(Canvas canvas) {
        int num;
        if (spacings == null || spacings.length == 0) {
            num = (maxValue - minValue) / spacingValue; // 计算出多少个间隔
        } else {
            num = spacings.length;
        }

        int spacingAll = minValue;
        for (int i = 1; i < num; i++) {
            resetPaint(Color.GRAY, 2, Paint.Style.FILL);
            canvas.drawLine(i * oneSpace + DEFAULT_PADDING_SPACING, seekBarY - DEFAULT_SEEK_BAR_HEIGHT / 2 - DEFAULT_SPACE_HEIGHT, i * oneSpace + DEFAULT_PADDING_SPACING, seekBarY - DEFAULT_SEEK_BAR_HEIGHT / 2, paint);

            String text;
            if (spacings == null || spacings.length == 0) {
                text = String.valueOf(minValue + spacingValue * i);
            } else {
                spacingAll += spacings[i - 1];
                text = String.valueOf(spacingAll);
            }
            // 画刻度值，x轴减去字体宽度的一半，字体居中
            drawSpaceText(text, i * oneSpace + DEFAULT_PADDING_SPACING, seekBarY - DEFAULT_SEEK_BAR_HEIGHT / 2 - DEFAULT_SPACE_HEIGHT, canvas);
        }
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
    private void drawTextShow(int clickType, int y, Canvas canvas) {
        resetPaint(Color.RED, 2, Paint.Style.FILL);
        int x;
        if (clickType == 0) {
            x = leftWareX;
        } else {
            x = rightWareX;
        }
        int current = getCurrentSeek(x);
        String text = String.valueOf(current);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (updateListener != null) {
                    updateListener.onUpdateStart(leftWareX, rightWareX);
                }
                clickLocation(x, y);
                setUpdate();
                dispatchUpdate(MotionEvent.ACTION_DOWN);
                break;
            case MotionEvent.ACTION_MOVE:
                updateX((int) x);
                dispatchUpdate(MotionEvent.ACTION_MOVE);
                break;
            case MotionEvent.ACTION_UP:
                isUpdate = false;
                postInvalidate();
                dispatchUpdate(MotionEvent.ACTION_UP);
                break;
        }
        return true;
    }

    private void setUpdate() {
        if (clickType == 0 || clickType == 1) {
            isUpdate = true;
        }
    }

    /**
     * 待完善 TODO
     */
    private void updateX(int x) {
        if (x <= DEFAULT_PADDING_SPACING) { // 超出左边界
            if (clickType == 0) {
                leftWareX = DEFAULT_PADDING_SPACING;
            } else if (clickType == 1) {
                rightWareX = DEFAULT_PADDING_SPACING;
            }
        } else if (x >= getWidth() - DEFAULT_PADDING_SPACING) { // 超出右边界
            if (clickType == 0) {
                leftWareX = getWidth() - DEFAULT_PADDING_SPACING;
            } else if (clickType == 1) {
                rightWareX = getWidth() - DEFAULT_PADDING_SPACING;
            }
        } else {
            int resultX = 0;
            if (clickType == 0) {
                resultX = leftWareX;
                leftWareX = x;
            } else if (clickType == 1) {
                resultX = rightWareX;
                rightWareX = x;
            }
            if (leftWareX + 30 < rightWareX) { // 左边超过右边的情况

            } else {
                if (clickType == 0) {
                    leftWareX = resultX;
                } else if (clickType == 1) {
                    rightWareX = resultX;
                }
            }
        }
        // Log.e("zgf", "========" + x + "=====" + leftWareX + "=====" + rightWareX);
        postInvalidate();
    }

    private void dispatchUpdate(int event) {
        int left = getCurrentSeek(leftWareX);
        int right = getCurrentSeek(rightWareX);
        switch (event) {
            case MotionEvent.ACTION_DOWN:
                if (updateListener != null) {
                    updateListener.onUpdateStart(left, right);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (updateListener != null) {
                    updateListener.onUpdate(left, right);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (updateListener != null) {
                    updateListener.onUpdateEnd(left, right);
                }
                break;
        }
    }

    /**
     * 计算当前进度值
     */
    private int getCurrentSeek(int x) {
        if (spacings == null || spacings.length == 0) {
            float ratio = (float) (x - DEFAULT_PADDING_SPACING) / (float) (getWidth() - 2 * DEFAULT_PADDING_SPACING);
            return (int) (ratio * (maxValue - minValue));
        } else {
            // 计算出当前滑动到第几个刻度上
            float num = (float) (x - DEFAULT_PADDING_SPACING) / oneSpace;
            int intNum = (int) Math.floor(num);

            // 计算出当前的x轴到上一个刻度的距离
            int resultText = minValue;
            int currentX = x - DEFAULT_PADDING_SPACING;
            for (int i = 0; i < intNum; i++) {
                resultText += spacings[i];
                currentX -= oneSpace;
            }

            // 如果计算的intNum值等于spacings的长度，说明是最大刻度值
            // 计算当前刻度内的值
            int currentXSpace = 0;
            if (intNum < spacings.length) {
                currentXSpace = (int) ((float) currentX / (float) oneSpace * spacings[intNum]);
            }
            resultText += currentXSpace;

            return resultText;
        }
    }

    /**
     * 判断是否是点击到指示器的范围内
     * 如果没有点击到范围内不刷新view
     */
    private void clickLocation(float x, float y) {
        RectF leftRect = new RectF(leftWareX - upwardBitmap.getWidth() / 2, upWareY, leftWareX + upwardBitmap.getWidth() / 2, upWareY + upwardBitmap.getHeight());
        RectF rightRect = new RectF(rightWareX - downwardBitmap.getWidth() / 2, downWareY, rightWareX + downwardBitmap.getWidth() / 2, downWareY + downwardBitmap.getHeight());

        if (leftRect.contains(x, y)) {
            clickType = 0;
        } else if (rightRect.contains(x, y)) {
            clickType = 1;
        } else {
            clickType = 2;
        }
    }

    /**
     * 设置每个可变长度的间距值
     * 如果同时设置默认间距值大小和可变长度间距值，则以可变长度间距值为准
     * 当间距和加上最小值大于最大值时选择一种适配方式
     * 1、throw exception
     * 2、更新最大值为当前间距和加上最小值
     */
    public void setSpacing(int min, int max, int... spacings) throws Exception {
        if (min >= max) {
            // 抛出异常后，下面的内容就不会再执行
            throw new Exception("SeekBarView max is must big min value!");
        } else {
            minValue = min;
            maxValue = max;
        }
        int result = 0;
        for (int i = 0; i < spacings.length; i++) {
            result += spacings[i];
        }
        maxValue = result;
        this.spacings = spacings;
        postInvalidate();
    }

    public void setCurrentLeft(int left) {
        if (left >= maxValue || left < minValue) {
            return;
        }
        this.left = left;
        requestLayout();
        postInvalidate();
    }

    private void setLeft() {
        if (left > 0) {
            if (spacings == null || spacings.length == 0) {
                float num = (float) (left) / maxValue;
                leftWareX = (int) (num * lastWidth + DEFAULT_PADDING_SPACING);
            } else {
                // TODO: 2019-07-07 可变刻度设置
            }
        } else {
            leftWareX = DEFAULT_PADDING_SPACING;
        }
    }

    public void setCurrentRight(int right) {
        if (right > maxValue || right <= minValue) {
            return;
        }
        this.right = right;
        requestLayout();
        postInvalidate();
    }

    private void setRight() {
        if (right > 0) {
            if (spacings == null || spacings.length == 0) {
                float num = (float) (right) / maxValue;
                rightWareX = (int) (num * lastWidth + DEFAULT_PADDING_SPACING);
            } else {
                // TODO: 2019-07-07 可变刻度设置
            }
        } else {
            rightWareX = getWidth() - DEFAULT_PADDING_SPACING;
        }
    }

    public void setCurrentLeftAndRight(int left, int right) {
        if (left >= right) {
            return;
        }
        if (left >= maxValue || left < minValue) {
            return;
        }
        if (right > maxValue || right <= minValue) {
            return;
        }
        this.left = left;
        this.right = right;
        requestLayout();
        postInvalidate();
    }

    /**
     * 监听进度
     */
    public interface SeekBarUpdateListener {
        // 手指点击指示器
        void onUpdateStart(int left, int right);

        // 滑动，指示器滑动过程
        void onUpdate(int left, int right);

        // 手指离开
        void onUpdateEnd(int left, int right);
    }

    public void setOnSeekBarUpdateListener(SeekBarUpdateListener listener) {
        updateListener = listener;
    }
}
