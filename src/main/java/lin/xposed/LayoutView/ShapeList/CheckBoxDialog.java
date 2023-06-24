package lin.xposed.LayoutView.ShapeList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.R;

public class CheckBoxDialog extends mDialog {
    private final Context context;
    ScrollView scrollView;
    LinearLayout rootView;
    String[] nameList;
    boolean[] checked;

    public CheckBoxDialog(Context context) {
        super(context);
        CommonTool.InjectResourcesToContext(context);
        this.context = context;
        this.setBackground(new BasicBackground().setAlpha(1d));
        scrollView = new ScrollView(context);
        rootView = new LinearLayout(context);
        rootView.setOrientation(LinearLayout.VERTICAL);
        rootView.setPadding(20, 20, 20, 0);
        scrollView.addView(rootView);
        setContentView(scrollView);
    }

    //初始状态
    public void initView(String[] itemNames, boolean[] checked, OnCheckedChange[] onCheckedChange) throws Exception {
        nameList = itemNames;
        this.checked = checked;
        if (itemNames.length != checked.length) throw new Exception("item length != checked length");
        LayoutInflater inflater = LayoutInflater.from(context);
        for (int i = 0; i < itemNames.length; i++) {
            String name = itemNames[i];
            RelativeLayout itemLayout = (RelativeLayout) inflater.inflate(R.layout.base_check_box, null);
            TextView nameView = itemLayout.findViewById(R.id.item_name_CheckBox);
            nameView.setText(name);
            CheckBox checkBox = itemLayout.findViewById(R.id.CheckBox_item);
            checkBox.setChecked(checked[i]);
            int finalI = i;
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onCheckedChange[finalI].Action(name, isChecked);
                }
            });
            rootView.addView(itemLayout, getLayoutParams());
        }
    }

    public void setTitle(String leftName, View view) {
        RelativeLayout title = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.base_title_layout, null);
        TextView titleNameView = title.findViewById(R.id.leftName_title);
        titleNameView.setText(leftName);
        scrollView.addView(title, 0);
    }

    public void setTitle(String leftName) {

    }

    private RelativeLayout.LayoutParams getLayoutParams() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(15, 15, 15, 0);
        return params;
    }

    public interface OnCheckedChange {
        void Action(String name, boolean Checked);
    }
}
