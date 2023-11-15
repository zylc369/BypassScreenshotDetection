package buwai.bypassscreenshotdetection;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;

import buwai.bypassscreenshotdetection.consts.AppConst;
import buwai.bypassscreenshotdetection.flowview.FloatingWindowService;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private boolean hasFloatViewPermission = false;
    private boolean hasAllPermissions = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnClickMe = findViewById(R.id.btn_click_me);
        btnClickMe.setOnClickListener(v -> {
            openFloatingWindowWhenHasAllPermissions(true);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        requestPermission();
    }

    protected void requestPermission() {
        requestFloatViewPermission();

        requestForegroundServicePermission();
    }

    protected void requestFloatViewPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // 悬浮窗权限未授予，做相应处理

            Log.w(TAG, "requestFloatViewPermission. 悬浮窗权限未授予，请求权限");

            Toast.makeText(this, "悬浮窗权限未授予，请求权限", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, AppConst.RequestCode.FLOATING_WINDOW_PERMISSION);
        } else {
            hasFloatViewPermission = true;

            // 悬浮窗权限已授予，可以使用悬浮窗功能
            Log.i(TAG, "requestFloatViewPermission. 悬浮窗权限已授予，可以使用悬浮窗功能");
        }

        openFloatingWindowWhenHasAllPermissions(false);
    }

    protected void requestForegroundServicePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.FOREGROUND_SERVICE},
                        AppConst.RequestCode.COMMON_SERVICE_CODE);
            } else {
                hasAllPermissions = true;
            }
        } else {
            hasAllPermissions = true;
        }

        openFloatingWindowWhenHasAllPermissions(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConst.RequestCode.FLOATING_WINDOW_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                hasFloatViewPermission = true;

                // 用户已授予悬浮窗权限

                Toast.makeText(this, "已授予悬浮窗权限，功能可以使用", Toast.LENGTH_LONG).show();
                Log.i(TAG, "已授予悬浮窗权限，功能可以使用");
            } else {
                // 用户未授予悬浮窗权限
                Toast.makeText(this, "未授予悬浮窗权限，功能将无法使用", Toast.LENGTH_LONG).show();
                Log.w(TAG, "未授予悬浮窗权限，功能将无法使用");
            }

            openFloatingWindowWhenHasAllPermissions(false);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult. " +
                "permissions=" + JSON.toJSONString(permissions) +
                ",grantResults=" + JSON.toJSONString(grantResults));

        if (requestCode == AppConst.RequestCode.COMMON_SERVICE_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "未授予权限，功能将无法使用", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "未授予权限，功能将无法使用");
                        return;
                    }
                }

                // 权限被授予，可以调用startForeground()方法

                hasAllPermissions = true;
                Toast.makeText(this, "已授所有权限，功能可以使用", Toast.LENGTH_LONG).show();
                Log.i(TAG, "已授所有权限，功能可以使用");
            } else {
                // 权限被拒绝，无法调用startForeground()方法

                Toast.makeText(this, "未授予权限，功能将无法使用", Toast.LENGTH_LONG).show();
                Log.w(TAG, "未授权限，功能将无法使用");
            }

            openFloatingWindowWhenHasAllPermissions(false);
        }
    }

    protected void openFloatingWindowWhenHasAllPermissions(boolean isClickTrigger) {
        if (!hasFloatViewPermission) {
            if (isClickTrigger) {
                Toast.makeText(this, "没有悬浮窗权限，请开启", Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (!hasAllPermissions) {
            if (isClickTrigger) {
                Toast.makeText(this, "没有其他必须的权限，请全部开启", Toast.LENGTH_LONG).show();
            }
            return;
        }

        FloatingWindowService.open(MainActivity.this);
    }
}