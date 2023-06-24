package lin.xposed.Initialize.ProxyTool;

import android.os.Bundle;
import androidx.annotation.Nullable;
import lin.xposed.HookUtils.CommonTool;

public class TestActivity extends BaseProxyActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CommonTool.Toast("create");
    }
}
