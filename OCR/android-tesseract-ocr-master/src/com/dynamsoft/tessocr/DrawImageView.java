package com.dynamsoft.tessocr;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DrawImageView extends ImageView{

	public DrawImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	Paint paint = new Paint();
	{
		paint.setAntiAlias(true);
		paint.setColor(0xff00ff00);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(5.5f);//设置线宽
		paint.setAlpha(255);
	};
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		//240 * 80
		//canvas.drawRect(new Rect(120, 280, 360, 360), paint);//绘制矩形

		//140*40
		canvas.drawRect(new Rect(170, 300, 310, 340), paint);
	}


	
	


	

}
