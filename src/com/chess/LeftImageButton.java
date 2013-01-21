package com.chess;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.01.13
 * Time: 13:45
 */
public class LeftImageButton extends RoboButton {

	public static final float BORDER_OFFSET = 3.0f;
	public static final float LINE_WIDTH = 1.0f;

	private Drawable icon;
	private Paint borderPaint;
	private boolean initialized;
	private int[] borderColors;
	private int imageWidth;

	public LeftImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public LeftImageButton(Context context) {
		super(context);
	}

	public LeftImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		// back for image
		TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.EnhancedField);
		try {
			icon = array.getDrawable(R.styleable.EnhancedField_leftImage);
		} finally {
			array.recycle();
		}


		imageWidth = icon.getIntrinsicWidth();
		int imageHeight = icon.getIntrinsicHeight();
		icon.setBounds(0, 0, imageWidth, imageHeight);

		borderColors = new int[2];
		borderColors[0] = 0xFF423f3a; // TODO place in colors
		borderColors[1] = context.getResources().getColor(R.color.any_button_stroke);

		borderPaint = new Paint();
		borderPaint.setStrokeWidth(1);
		borderPaint.setStyle(Paint.Style.STROKE);

	}

	@Override
	protected void onDraw(Canvas canvas) { // TODO use Picture?

		int height = getHeight();
		for (int i = 0, cnt = borderColors.length; i < cnt; i++) {
			float left = height + i * LINE_WIDTH;
			float top = 0;
			float right = height + i * LINE_WIDTH;
			float bottom = 0;
			switch (i){
				case 0:
					top = BORDER_OFFSET - LINE_WIDTH;
					bottom = height - BORDER_OFFSET + LINE_WIDTH;
					break;
				case 1:
					top = BORDER_OFFSET - LINE_WIDTH;
					bottom = height - BORDER_OFFSET;
					break;
			}

			borderPaint.setColor(borderColors[i]);
			canvas.drawLine(left, top, right, bottom, borderPaint);
		}

		// place image
		canvas.save();
		float imgCenterX = BORDER_OFFSET/2 + (height - imageWidth)/2;
		float imgCenterY = (height - imageWidth)/2;
		canvas.translate(imgCenterX, imgCenterY);
		icon.draw(canvas);
		canvas.restore();

		// place additional clickable element
		canvas.translate(BORDER_OFFSET *4, 0);
		super.onDraw(canvas);
	}

}