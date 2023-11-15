package buwai.bypassscreenshotdetection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import buwai.bypassscreenshotdetection.flowview.FloatingWindowService;

public class TransparentActivity extends Activity {

    private static final int CLICK_TO_OPEN_DIALOG_LIMIT = 1;

    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transparent);

        View llWrapper = findViewById(R.id.ll_wrapper);
        LinearLayout llContain = findViewById(R.id.ll_contain);
        Switch switchTipsButton = findViewById(R.id.sw_tips);
        Button btnOpenFloatView = findViewById(R.id.btn_open_float_view);

        switchTipsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 在这里处理状态变化事件
                if (isChecked) {
                    // 开启状态
                    llContain.setVisibility(View.GONE);
                } else {
                    // 关闭状态
                }
            }
        });

        llWrapper.setOnClickListener(v -> {
            clickCount++;
            if (clickCount < CLICK_TO_OPEN_DIALOG_LIMIT) {
                return;
            }

            clickCount = 0;

            if (llContain.getVisibility() == View.VISIBLE) {
                return;
            }
            switchTipsButton.setChecked(false);
            llContain.setVisibility(View.VISIBLE);
        });

        btnOpenFloatView.setOnClickListener(v -> FloatingWindowService.open(TransparentActivity.this));
    }

    private void openTipsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.transparent_page_close_dialog_tips);
        builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 结束进程
                Process.killProcess(Process.myPid());
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 关闭对话框
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}