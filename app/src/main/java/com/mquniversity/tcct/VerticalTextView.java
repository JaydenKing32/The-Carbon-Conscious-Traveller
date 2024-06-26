package com.mquniversity.tcct;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;

// https://github.com/yoog568/VerticalTextView
public class VerticalTextView extends androidx.appcompat.widget.AppCompatTextView {
    private final static int ORIENTATION_UP_TO_DOWN = 0;
    private final static int ORIENTATION_DOWN_TO_UP = 1;
    private final static int ORIENTATION_LEFT_TO_RIGHT = 2;
    private final static int ORIENTATION_RIGHT_TO_LEFT = 3;

    private final Rect text_bounds = new Rect();
    private final Path path = new Path();
    private int direction;

    public VerticalTextView(Context context) {
        super(context);
    }

    public VerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VerticalTextView);
        direction = a.getInt(R.styleable.VerticalTextView_direction, 0);
        a.recycle();

        requestLayout();
        invalidate();

    }

    public void setDirection(int direction) {
        this.direction = direction;

        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), text_bounds);
        if (direction == ORIENTATION_LEFT_TO_RIGHT || direction == ORIENTATION_RIGHT_TO_LEFT) {
            setMeasuredDimension(measureHeight(widthMeasureSpec), measureWidth(heightMeasureSpec));
        } else if (direction == ORIENTATION_UP_TO_DOWN || direction == ORIENTATION_DOWN_TO_UP) {
            setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        }

    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            return specSize;
        } else {
            int result = text_bounds.height() + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                return Math.min(result, specSize);
            } else {
                return result;
            }
        }
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            return specSize;
        } else {
            int result = text_bounds.width() + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                return Math.min(result, specSize);
            } else {
                return result;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        int startX;
        int startY;
        int stopX;
        int stopY;

        if (direction == ORIENTATION_UP_TO_DOWN) {
            startX = (getWidth() - text_bounds.height() >> 1);
            startY = (getHeight() - text_bounds.width() >> 1);
            stopX = (getWidth() - text_bounds.height() >> 1);
            stopY = (getHeight() + text_bounds.width() >> 1);
            path.moveTo(startX, startY);
            path.lineTo(stopX, stopY);
        } else if (direction == ORIENTATION_DOWN_TO_UP) {
            startX = (getWidth() + text_bounds.height() >> 1);
            startY = (getHeight() + text_bounds.width() >> 1);
            stopX = (getWidth() + text_bounds.height() >> 1);
            stopY = (getHeight() - text_bounds.width() >> 1);
            path.moveTo(startX, startY);
            path.lineTo(stopX, stopY);
        } else if (direction == ORIENTATION_LEFT_TO_RIGHT) {
            startX = (getWidth() - text_bounds.width() >> 1);
            startY = (getHeight() + text_bounds.height() >> 1);
            stopX = (getWidth() + text_bounds.width() >> 1);
            stopY = (getHeight() + text_bounds.height() >> 1);
            path.moveTo(startX, startY);
            path.lineTo(stopX, stopY);
        } else if (direction == ORIENTATION_RIGHT_TO_LEFT) {
            startX = (getWidth() + text_bounds.width() >> 1);
            startY = (getHeight() - text_bounds.height() >> 1);
            stopX = (getWidth() - text_bounds.width() >> 1);
            stopY = (getHeight() - text_bounds.height() >> 1);
            path.moveTo(startX, startY);
            path.lineTo(stopX, stopY);
        }

        this.getPaint().setColor(this.getCurrentTextColor());
        canvas.drawTextOnPath(getText().toString(), path, 0, 0, this.getPaint());

        canvas.restore();
    }
}
