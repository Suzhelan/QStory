package lin.xposed.LayoutView.ShapeList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import lin.xposed.R;

@SuppressLint("AppCompatCustomView")
public class mButton {
    public LinearLayout layout;
    Button button;

    @SuppressLint({"ResourceAsColor", "InflateParams"})
    public mButton(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        layout = (LinearLayout) inflater.inflate(R.layout.base_button_layout, null, false);
        button = layout.findViewById(R.id.base_button_name);
    }

    public void setTextSize(float i) {
        button.setTextSize(i);
    }

    public void setText(String str) {
        this.button.setText(str);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.button.setOnClickListener(onClickListener);
    }
}
