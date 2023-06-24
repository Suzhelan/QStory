package lin.xposed.LayoutView.ShapeList;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class BasicBackground extends GradientDrawable {
    //透明度 0~255
    public static final double ALPHA = 0.8;
    //背景颜色
    public static final String COLOR = "#FFFFFF";
    //边框颜色
    public static final String COLOR_Lines = "#E6a1a3a6";
    //圆角度
    public static final int RADIUS = 30;

    public BasicBackground() {
        super();
        super.setCornerRadius(RADIUS);
        super.setColor(Color.parseColor(COLOR));
        super.setPadding(15, 10, 15, 10);
        this.setAlpha(ALPHA);
        super.setShape(GradientDrawable.RECTANGLE);
        super.setStroke(2, Color.parseColor(COLOR_Lines));
    }

    public BasicBackground setAlpha(double d) {
        int alpha = (int) (255 * d);
        super.setAlpha(alpha);
        return this;
    }

}
