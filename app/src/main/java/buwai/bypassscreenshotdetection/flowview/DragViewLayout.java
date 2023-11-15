package buwai.bypassscreenshotdetection.flowview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSONArray;

/**
 * @author xunyu.lc
 * @version CREATE TIME: 2023/11/15 15:49
 */
public class DragViewLayout extends RelativeLayout {

    private static final String TAG = "DragViewLayout";

    //view所在位置
    int mLastX, mLastY;

    //屏幕高宽
    int mScreenWidth, mScreenHeight;

    //view高宽
    int mWidth, mHeight;

    /**
     * 是否在拖拽过程中
     */
    boolean isDrag = false;

    /**
     * 系统最小滑动距离
     *
     * @param context
     */
    int mTouchSlop = 0;

    WindowManager mWindowManager;

    //手指触摸位置
    private float xInScreen;
    private float yInScreen;
    private float xInView;
    public float yInView;


    public DragViewLayout(Context context) {
        this(context, null);
    }

    public DragViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;
//        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTouchSlop = 2;
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) (xInScreen = event.getRawX());
                mLastY = (int) (yInScreen = event.getRawY());
                yInView = event.getY();
                xInView = event.getX();

                WindowManager.LayoutParams floatLayoutParams =
                        (WindowManager.LayoutParams) getLayoutParams();

                int[] locationInWindow = new int[2];
                getLocationInWindow(locationInWindow);

                int[] locationOnScreen = new int[2];
                getLocationOnScreen(locationOnScreen);

                Log.i(TAG, "dispatchTouchEvent - down. xInView=" + xInView +
                        ",mLastX=" + mLastX + ",yInView=" + yInView + ",mLastY=" + mLastY +
                        "|old -> x=" + floatLayoutParams.x + ",y=" + floatLayoutParams.y +
                        ",locationInWindow=" + JSONArray.toJSONString(locationInWindow) +
                        ",locationOnScreen=" + JSONArray.toJSONString(locationOnScreen));
                break;
            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, "dispatchTouchEvent - move. x=" + event.getRawX() +
                        ",y=" + event.getRawY() + ",mTouchSlop=" + mTouchSlop);
                int dx = (int) event.getRawX() - mLastX;
                int dy = (int) event.getRawY() - mLastY;
                if (Math.abs(dx) > mTouchSlop || Math.abs(dy) > mTouchSlop) {
                    isDrag = true;
                }
                mLastX = (int) (xInScreen = event.getRawX());
                mLastY = (int) (yInScreen = event.getRawY());
                //拖拽时调用WindowManager updateViewLayout更新悬浮球位置
                updateFloatPosition(false);

//                WindowManager.LayoutParams floatLayoutParams =
//                        (WindowManager.LayoutParams) getLayoutParams();
//                floatLayoutParams.x = dx;
//                floatLayoutParams.y = dy;
                //更新位置
//                mWindowManager.updateViewLayout(this, floatLayoutParams);
                break;
            case MotionEvent.ACTION_UP:
                if (isDrag) {
                    //执行贴边
                    startAnim();
                    isDrag = false;

                    // 这种写法能够保证拖动的情况下，不要让悬浮框上的按钮响应点击事件
                    return false;
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    //更新悬浮球位置
    private void updateFloatPosition(boolean isUp) {
        // 悬浮框【左边】距离屏幕的距离
        int x = (int) (xInScreen - xInView);
        // 悬浮框【顶部】距离屏幕的距离
        int y = (int) (yInScreen - yInView);

        Log.i(TAG, "updateFloatPosition. isUp=" + isUp +
                ",x=" + x + ",y=" + y);

        if (isUp) {
            x = isRightFloat() ? mScreenWidth : 0;
        }
        if (y < 0) {
            y = 0;
        }
        if (y > mScreenHeight - mHeight) {
            y = mScreenHeight - mHeight;
        }

        WindowManager.LayoutParams floatLayoutParams =
                (WindowManager.LayoutParams) getLayoutParams();

        int[] locationOnScreen = new int[2];
        getLocationOnScreen(locationOnScreen);
        int yDiff = locationOnScreen[1] - floatLayoutParams.y;

        Log.i(TAG, "updateFloatPosition. x=" + x + "|" + floatLayoutParams.x +
                ",y=" + y + "|" + floatLayoutParams.y + ",yDiff=" + yDiff);

        floatLayoutParams.x = x;
        floatLayoutParams.y = y - yDiff;
        //更新位置
        mWindowManager.updateViewLayout(this, floatLayoutParams);
    }

    /**
     * 是否靠右边悬浮
     *
     * @return
     */
    boolean isRightFloat() {
        return xInScreen > mScreenWidth / 2;
    }


    //执行贴边动画
    private void startAnim() {
        ValueAnimator valueAnimator;
        WindowManager.LayoutParams floatLayoutParams =
                (WindowManager.LayoutParams) getLayoutParams();
        if (floatLayoutParams.x < mScreenWidth / 2) {
            valueAnimator = ValueAnimator.ofInt(floatLayoutParams.x, 0);
        } else {
            valueAnimator = ValueAnimator.ofInt(floatLayoutParams.x, mScreenWidth - mWidth);
        }
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                floatLayoutParams.x = (int) animation.getAnimatedValue();
                mWindowManager.updateViewLayout(DragViewLayout.this, floatLayoutParams);
            }
        });
        valueAnimator.start();
    }

    //悬浮球显示
//    public void show() {
//        WindowManager.LayoutParams floatLayoutParams = new WindowManager.LayoutParams
//        (WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams
//                .FIRST_SUB_WINDOW,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams
//                .FLAG_NOT_TOUCH_MODAL
//                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                PixelFormat.RGBA_8888);
//        floatLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
//        floatLayoutParams.x = 0;
//        floatLayoutParams.y = (int)(mScreenHeight * 0.4);
//        mWindowManager.addView(this, floatLayoutParams);
//    }

    public void show(WindowManager.LayoutParams floatLayoutParams) {
//        WindowManager.LayoutParams floatLayoutParams = new WindowManager.LayoutParams
//        (WindowManager.LayoutParams.WRAP_CONTENT,
//                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams
//                .FIRST_SUB_WINDOW,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams
//                .FLAG_NOT_TOUCH_MODAL
//                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
//                PixelFormat.RGBA_8888);
//        floatLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
//        floatLayoutParams.x = 0;
//        floatLayoutParams.y = (int)(mScreenHeight * 0.4);
        mWindowManager.addView(this, floatLayoutParams);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mWidth == 0) {
            //获取悬浮球高宽
            mWidth = getWidth();
            mHeight = getHeight();
        }
    }
}
