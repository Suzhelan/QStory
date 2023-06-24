package lin.xposed.LayoutView.Other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import lin.xposed.HookUtils.CommonTool;
import lin.xposed.LayoutView.ShapeList.BasicBackground;
import lin.xposed.LayoutView.ShapeList.mButton;
import lin.xposed.R;
import lin.xposed.Utils.UpdateTool;

import java.util.ArrayList;

public class emmmmm {
    private static final ArrayList<Item> demo = new ArrayList<>();

    static {
        demo.add(new Item("再普通也值得万般宠溺", v -> {
        }));
        demo.add(new Item("TG频道", v -> {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            Uri content = Uri.parse("https://t.me/WhenFlowersAreInBloom");
            intent.setData(content);
            CommonTool.getActivity().startActivity(intent);
        }));
        demo.add(new Item("检测更新", v -> {

            try {
                UpdateTool.update(CommonTool.getActivity());
            } catch (Exception e) {
                CommonTool.Toast(e);
            }
        }));
    }

    @SuppressLint("RtlHardcoded")
    public static void addEmmmView(ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        BasicBackground background = new BasicBackground();
        background.setAlpha(0.5);
        for (Item item : demo) {
            mButton button2 = new mButton(context);
            button2.layout.setBackground(background);
            button2.setText(item.name);
            button2.setOnClickListener(item.onClickListener);
            viewGroup.addView(button2.layout, getParams());
        }

        mButton button = new mButton(context);
        button.layout.setBackground(null);
        button.layout.setGravity(Gravity.LEFT);
        button.setTextSize(15);
        button.setText(context.getString(R.string.关于));
        viewGroup.addView(button.layout, getParams());
    }

    private static ViewGroup.LayoutParams getParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(25, 15, 25, 15);
        return params;
    }

    private static class Item {
        String name;
        View.OnClickListener onClickListener;

        public Item(String name, View.OnClickListener clickListener) {
            this.name = name;
            this.onClickListener = clickListener;
        }
    }
}
