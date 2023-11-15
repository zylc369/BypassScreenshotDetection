package buwai.bypassscreenshotdetection.flowview;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * @author xunyu.lc
 * @version CREATE TIME: 2023/11/15 15:51
 */
public class SqWindowManagerFloatView extends DragViewLayout {

    public SqWindowManagerFloatView(final Context context, final int floatImgId) {
        super(context);
        setClickable(true);
        final ImageView floatView = new ImageView(context);
        floatView.setImageResource(floatImgId);
        floatView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "点击了悬浮球", Toast.LENGTH_SHORT).show();
            }
        });
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addView(floatView, params);
    }

}
