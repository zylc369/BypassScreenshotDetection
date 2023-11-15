package buwai.bypassscreenshotdetection.flowview;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import buwai.bypassscreenshotdetection.MainActivity;
import buwai.bypassscreenshotdetection.R;
import buwai.bypassscreenshotdetection.TransparentActivity;
import buwai.bypassscreenshotdetection.consts.AppConst;

/**
 * @author xunyu.lc
 * @version CREATE TIME: 2023/11/15 15:03
 */
public class FloatingWindowService extends Service {

    private WindowManager windowManager;
    private View floatingView;

    public static void open(Activity activity) {
        Intent intent = new Intent(activity, FloatingWindowService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.startForegroundService(intent);
        } else {
            activity.startService(intent);
        }

        activity.finish();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化WindowManager
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // 创建悬浮窗的视图
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_window, null);
        // 设置悬浮窗的参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        // 添加视图到WindowManager
        windowManager.addView(floatingView, params);

//        SqWindowManagerFloatView floatView = new SqWindowManagerFloatView(this, R.mipmap.ic_launcher);
//        floatView.show(params);

        // 绕过按钮
        Button btnBypass = floatingView.findViewById(R.id.btn_bypass);
        btnBypass.setOnClickListener(v -> {
            Intent intent = new Intent(FloatingWindowService.this, TransparentActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            stopSelf();
        });

        // 关闭按钮
        Button btnClose = floatingView.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> {
            stopSelf();
        });


        // 创建Notification并设置为前台服务通知
        Notification notification = createNotification();
        startForeground(AppConst.RequestCode.FLOATING_WINDOW_PERMISSION, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除悬浮窗视图
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        String channelId = getPackageName();
        // 创建通知渠道（适用于Android 8.0及以上版本）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Your Channel Name";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            // 可以设置其他通知渠道的属性
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // 创建通知构建器
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.running))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false);

        // 创建通知对象
        Notification notification = builder.build();
        return notification;
    }


}
