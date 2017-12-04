package com.adefruandta.spinningwheel;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ArrayRes;
import android.support.annotation.ColorInt;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adefruandta on 3/12/17.
 */

public class SpinningWheelView extends View implements WheelRotation.RotationListener {

    // region static attr

    private final static int MIN_COLORS = 3;

    private final static float ANGLE = 360f;

    private final static int COLORS_RES = R.array.rainbow_dash;

    private final static float TOUCH_SCALE_FACTOR = (180.0f / 320) / 2;

    private final static int TEXT_SIZE = 25;

    private final static int TEXT_COLOR = Color.BLACK;

    private final static int ARROW_COLOR = Color.BLACK;

    private static final int ARROW_SIZE = 50;

    // endregion

    // region attr

    @ColorInt
    private int wheelStrokeColor;

    private float wheelStrokeWidth;

    private float wheelStrokeRadius;

    private int wheelTextColor;

    private float wheelTextSize;

    private int wheelArrowColor;

    private float wheelArrowWidth;

    private float wheelArrowHeight;

    private WheelRotation wheelRotation;

    private Circle circle;

    private float angle = 0;

    private float previousX;

    private float previousY;

    private List items;

    private Point[] points;

    @ColorInt
    private int[] colors;

    private OnRotationListener onRotationListener;

    private boolean onRotationListenerTicket;

    private boolean onRotation;

    private Paint textPaint;

    private Paint strokePaint;

    private Paint trianglePaint;

    private Paint itemPaint;

    // endregion

    // region constructor

    public SpinningWheelView(Context context) {
        super(context);
    }

    public SpinningWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
    }

    public SpinningWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SpinningWheelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(attrs);
    }

    // endregion

    // region life cycle

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        initCircle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (circle == null) {
            initCircle();
        }

        if (hasData() && (points == null || points.length != getItemSize())) {
            initPoints();
        }

        drawCircle(canvas);

        drawWheel(canvas);

        drawWheelItems(canvas);

        drawTriangle(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (circle == null || !isEnabled() || onRotation) {
            return false;
        }

        float x = event.getX();
        float y = event.getY();

        if (!circle.contains(x, y)) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onRotationListenerTicket = true;
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = x - previousX;
                float dy = y - previousY;

                // reverse direction of rotation above the mid-line
                if (y > circle.getCy()) {
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < circle.getCx()) {
                    dy = dy * -1;
                }

                rotate((dx + dy) * TOUCH_SCALE_FACTOR);

                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onRotationListenerTicket = false;
                break;
        }

        previousX = x;
        previousY = y;

        return true;
    }

    // endregion

    // region rotation listener

    @Override
    public void onRotate(float angle) {
        rotate(angle);
    }

    @Override
    public void onStop() {
        onRotation = false;

        if (onRotationListener != null) {
            onRotationListener.onStopRotation(getSelectedItem());
        }
    }

    // endregion

    // region Functionality

    // angle mod 360 prevent to big angle, and overflow float
    // rotate without animation
    public void rotate(float angle) {
        this.angle += angle;
        this.angle %= ANGLE;
        invalidate();

        if (onRotationListenerTicket && angle != 0 && onRotationListener != null) {
            onRotationListener.onRotation();
            onRotationListenerTicket = false;
        }
    }

    /**
     * Rotate wheel with animation
     *
     * @param maxAngle: Max angle for render rotation
     * @param duration: time in millis wheel for rotation
     * @param interval: time to render rotation
     */
    public void rotate(float maxAngle, long duration, long interval) {
        if (maxAngle == 0) {
            return;
        }

        onRotationListenerTicket = true;
        onRotation = true;

        if (wheelRotation != null) {
            wheelRotation.cancel();
        }

        wheelRotation = WheelRotation
                .init(duration, interval)
                .setMaxAngle(maxAngle)
                .setListener(this);
        wheelRotation.start();
    }

    public int getWheelStrokeColor() {
        return wheelStrokeColor;
    }

    public void setWheelStrokeColor(int wheelStrokeColor) {
        this.wheelStrokeColor = wheelStrokeColor;
        invalidate();
    }

    public float getWheelStrokeWidth() {
        return wheelStrokeWidth;
    }

    public void setWheelStrokeWidth(float wheelStrokeWidth) {
        this.wheelStrokeWidth = wheelStrokeWidth;

        initWheelStrokeRadius();

        invalidate();
    }

    public float getWheelTextSize() {
        return wheelTextSize;
    }

    public void setWheelTextSize(float wheelTextSize) {
        this.wheelTextSize = wheelTextSize;
        invalidate();
    }

    public int getWheelTextColor() {
        return wheelTextColor;
    }

    public void setWheelTextColor(int wheelTextColor) {
        this.wheelTextColor = wheelTextColor;
        invalidate();
    }

    public int getWheelArrowColor() {
        return wheelArrowColor;
    }

    public void setWheelArrowColor(int wheelArrowColor) {
        this.wheelArrowColor = wheelArrowColor;
        invalidate();
    }

    public void setWheelArrowWidth(float wheelArrowWidth) {
        this.wheelArrowWidth = wheelArrowWidth;
        invalidate();
    }

    public void setWheelArrowHeight(float wheelArrowHeight) {
        this.wheelArrowHeight = wheelArrowHeight;
        invalidate();
    }

    public int[] getColors() {
        return colors;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
        invalidate();
    }

    // Set colors with array res
    // Minimal length 3
    public void setColors(@ArrayRes int colorsResId) {
        if (colorsResId == 0) {
            // init default colors
            setColors(COLORS_RES);
            return;
        }

        int[] typedArray;

        // if in edit mode
        if (isInEditMode()) {
            String[] sTypeArray = getResources().getStringArray(colorsResId);
            typedArray = new int[sTypeArray.length];

            for (int i = 0; i < sTypeArray.length; i++) {
                typedArray[i] = Color.parseColor(sTypeArray[i]);
            }
        }
        else {
            typedArray = getResources().getIntArray(colorsResId);
        }

        if (typedArray.length < MIN_COLORS) {
            // init default colors
            setColors(COLORS_RES);
            return;
        }

        int[] colors = new int[typedArray.length];

        for (int i = 0; i < typedArray.length; i++) {
            colors[i] = typedArray[i];
        }

        setColors(colors);
    }

    public List getItems() {
        return items;
    }

    public void setItems(List items) {
        this.items = items;

        initPoints();

        invalidate();
    }

    public void setItems(@ArrayRes int itemsResId) {
        if (itemsResId == 0) {
            return;
        }

        String[] typedArray = getResources().getStringArray(itemsResId);
        List items = new ArrayList();

        for (int i = 0; i < typedArray.length; i++) {
            items.add(typedArray[i]);
        }

        setItems(items);
    }

    public OnRotationListener getOnRotationListener() {
        return onRotationListener;
    }

    public void setOnRotationListener(OnRotationListener onRotationListener) {
        this.onRotationListener = onRotationListener;
    }

    public <T> T getSelectedItem() {
        if (circle == null || points == null) {
            return null;
        }

        int itemSize = getItemSize();
        float cx = circle.getCx();

        for (int i = 0; i < points.length; i++) {
            if (points[i].x <= cx && cx <= points[(i + 1) % itemSize].x) { // validate point x
                return (T) items.get(i);
            }
        }

        return null;
    }

    // endregion

    // region methods

    private void initAttrs(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Wheel, 0, 0);

        try {
            // init colors
            int colorsResId = typedArray.getResourceId(R.styleable.Wheel_wheel_colors, 0);
            setColors(colorsResId);

            int wheelStrokeColor = typedArray.getColor(R.styleable.Wheel_wheel_stroke_color,
                    ContextCompat.getColor(getContext(), android.R.color.transparent));
            setWheelStrokeColor(wheelStrokeColor);

            float wheelStrokeWidth = typedArray.getDimension(R.styleable.Wheel_wheel_stroke_width, 0f);
            setWheelStrokeWidth(wheelStrokeWidth);

            int itemsResId = typedArray.getResourceId(R.styleable.Wheel_wheel_items, 0);
            setItems(itemsResId);

            float wheelTextSize = typedArray.getDimension(R.styleable.Wheel_wheel_text_size, TEXT_SIZE);
            setWheelTextSize(wheelTextSize);

            int wheelTextColor = typedArray.getColor(R.styleable.Wheel_wheel_text_color, TEXT_COLOR);
            setWheelTextColor(wheelTextColor);

            int wheelArrowColor = typedArray.getColor(R.styleable.Wheel_wheel_arrow_color, ARROW_COLOR);
            setWheelArrowColor(wheelArrowColor);

            float wheelArrowWidth = typedArray.getDimension(R.styleable.Wheel_wheel_arrow_width, dpToPx(ARROW_SIZE));
            setWheelArrowWidth(wheelArrowWidth);

            float wheelArrowHeight = typedArray.getDimension(R.styleable.Wheel_wheel_arrow_height, dpToPx(ARROW_SIZE));
            setWheelArrowHeight(wheelArrowHeight);
        } finally {
            typedArray.recycle();
        }

        init();
    }

    private void init() {
        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(wheelTextColor);
        textPaint.setTextSize(wheelTextSize);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(wheelStrokeColor);
        strokePaint.setStrokeWidth(wheelStrokeWidth);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);

        trianglePaint = new Paint();
        trianglePaint.setColor(wheelArrowColor);
        trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        trianglePaint.setAntiAlias(true);

        itemPaint = new Paint();
        itemPaint.setStyle(Paint.Style.FILL);
    }

    private void initWheelStrokeRadius() {
        wheelStrokeRadius = wheelStrokeWidth / 2;
        wheelStrokeRadius = wheelStrokeRadius == 0 ? 1 : wheelStrokeRadius;
    }

    private void initCircle() {
        int width = getMeasuredWidth() == 0 ? getWidth() : getMeasuredWidth();
        int height = getMeasuredHeight() == 0 ? getHeight() : getMeasuredHeight();

        circle = new Circle(width, height);
    }

    private void initPoints() {
        if (this.items != null && !this.items.isEmpty()) {
            points = new Point[this.items.size()];
        }
    }

    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(circle.getCx(), circle.getCy(), circle.getRadius(), new Paint());
        drawCircleStroke(canvas);
    }

    private void drawCircleStroke(Canvas canvas) {
        canvas.drawCircle(circle.getCx(), circle.getCy(), circle.getRadius() - wheelStrokeRadius, strokePaint);
    }

    private void drawWheel(Canvas canvas) {
        if (!hasData()) {
            return;
        }

        // Prepare Point
        float cx = circle.getCx();
        float cy = circle.getCy();
        float radius = circle.getRadius();
        float endOfRight = cx + radius;
        float left = cx - radius + (wheelStrokeRadius * 2);
        float top = cy - radius + (wheelStrokeRadius * 2);
        float right = cx + radius - (wheelStrokeRadius * 2);
        float bottom = cy + radius - (wheelStrokeRadius * 2);

        // Rotate Wheel
        canvas.rotate(angle, cx, cy);

        // Prepare Pie
        RectF rectF = new RectF(left, top, right, bottom);

        float angle = 0;
        for (int i = 0; i < getItemSize(); i++) {
            canvas.save();
            canvas.rotate(angle, cx, cy);
            canvas.drawArc(rectF, 0, getAnglePerItem(), true, getItemPaint(i));
            canvas.restore();

            points[i] = circle.rotate(angle + this.angle, endOfRight, cy);

            angle += getAnglePerItem();
        }
    }

    private void drawWheelItems(Canvas canvas) {
        float cx = circle.getCx();
        float cy = circle.getCy();
        float radius = circle.getRadius();
        float x = cx - radius + (wheelStrokeRadius * 5);
        float y = cy;
        float textWidth = radius - (wheelStrokeRadius * 10);
        TextPaint textPaint = new TextPaint();
        textPaint.set(this.textPaint);

        float angle = getAnglePerItem() / 2;

        for (int i = 0; i < getItemSize(); i++) {
            CharSequence item = TextUtils
                    .ellipsize(items.get(i).toString(), textPaint, textWidth, TextUtils.TruncateAt.END);
            canvas.save();
            canvas.rotate(angle + 180, cx, cy); // +180 for start from right
            canvas.drawText(item.toString(), x, y, this.textPaint);
            canvas.restore();

            angle += getAnglePerItem();
        }
    }

    private void drawTriangle(Canvas canvas) {
        // Prepare Point
        float cx = circle.getCx();
        float cy = circle.getCy();
        float radius = circle.getRadius();

        // Handle triangle not following the rotation
        canvas.rotate(-angle, cx, cy);

        drawTriangle(canvas, trianglePaint, cx, cy - radius, wheelArrowWidth, wheelArrowHeight);
    }

    private void drawTriangle(Canvas canvas, Paint paint, float x, float y, float width, float height) {
        float halfWidth = width / 2;
        float halfHeight = height / 2;

        Path path = new Path();
        path.moveTo(x - halfWidth, y - halfHeight); // Top left
        path.lineTo(x + halfWidth, y - halfHeight); // Top right
        path.lineTo(x, y + halfHeight); // Bottom Center
        path.lineTo(x - halfWidth, y - halfHeight); // Back to top left
        path.close();

        canvas.drawPath(path, paint);
    }

    private Paint getItemPaint(int position) {
        int i = position % colors.length;

        // if start color == end color, get middle color
        if (getItemSize() - 1 == position && position % colors.length == 0) {
            i = colors.length / 2;
        }

        itemPaint.setColor(colors[i]);

        return itemPaint;
    }

    private int getItemSize() {
        return items == null ? 0 : items.size();
    }

    private float getAnglePerItem() {
        return ANGLE / (float) getItemSize();
    }

    private boolean hasData() {
        return items != null && !items.isEmpty();
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    // endregion

    // region Listener

    public interface OnRotationListener<T> {

        void onRotation();

        void onStopRotation(T item);
    }

    // endregion
}
