package lin.xposed.LayoutView.ShapeList;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import lin.xposed.LayoutView.ScreenParamUtils;
import lin.xposed.R;

public class mDialog extends Dialog {
    public LinearLayout layout;
    public boolean isStop;
    boolean ChangedSize;
    private Context context;

    public mDialog(Context context) {
        super(context, R.style.dialog);
    }

    public mDialog(Context context, int R) {
        super(context, R);
    }

    public void buildBaseContainer() {
        this.layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(10, 0, 0, 0);
    }

    @Override
    protected void onStop() {
        isStop = true;
        super.onStop();
    }

    @Override
    public void show() {
        //如果从大小从未修改就展示前自动修改
        if (!ChangedSize) setDialogWindowAttr(0.7, 0.5);
        super.show();
    }

    public void setBackground(Drawable drawable) {
        this.getWindow().setBackgroundDrawable(drawable);
    }

    public void setDialogWindowAttr(double width, double height) {
        setDialogWindowAttr((int) (ScreenParamUtils.getScreenWidth() * width),
                (int) (ScreenParamUtils.getScreenHeight() * height));
    }

    public void setDialogWindowAttr(double width, int height) {
        setDialogWindowAttr((int) (ScreenParamUtils.getScreenWidth() * width), height);
    }

    //在dialog.show()或者setView()之后调用 因为设置完dialog的View 宽高会被子布局改变
    public void setDialogWindowAttr(int width, int height) {
        Window window = this.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = width;
        lp.height = height;
        this.getWindow().setAttributes(lp);
        this.ChangedSize = true;
    }
}
