package com.talon.screen.quick;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.talon.screen.quick.network.ClickBean;
import com.talon.screen.quick.network.MWebSocketClient;
import com.talon.screen.quick.network.MWebSocketServer;
import com.talon.screen.quick.util.BitmapUtils;
import com.talon.screen.quick.util.LogWrapper;
import com.talon.screen.quick.util.ScreenUtils;
import com.talon.screen.quick.util.ThreadPool;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author by talon, Date on 2020/7/19.
 * note: 播放视频
 */
public class PlayActivity extends AppCompatActivity implements MWebSocketClient.CallBack,CallBack {

    private MWebSocketClient webSocketClient;
    private ScreenImageView screenImgView;
    private MWebSocketServer webSocketServer;

    Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        screenImgView = findViewById(R.id.screenImgView);
        screenImgView.setOnCallBack(this);
        Intent intent = getIntent();
        if (intent != null) {
            String host = intent.getStringExtra("host");
            try {
                URI url = new URI("ws://" + host + ":" + Config.ANDROID_SERVER_PORT);
                webSocketClient = new MWebSocketClient(url, this);
                webSocketClient.setConnectionLostTimeout(5 * 1000);
                boolean flag = webSocketClient.connectBlocking();
                Toast.makeText(PlayActivity.this, "链接状态：" + flag, Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            webSocketServer=MWebSocketServer.getInstance();
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int action = event.getAction();
//
//        if (action == MotionEvent.ACTION_UP ||
//                action == MotionEvent.ACTION_DOWN) {
//            int x = (int) event.getX();
//            int y = (int) event.getY();
//            ClickBean clickBean = new ClickBean();
//            clickBean.clickX = x;
//            clickBean.clickY = y;
//            clickBean.orginWidth= ScreenUtils.getScreenWidth(this);
//            clickBean.orginHeight=ScreenUtils.getScreenHeight(this);
//            clickBean.density=getResources().getDisplayMetrics().density;
//            String message = new Gson().toJson(clickBean);
//            webSocketServer.sendClickLocation(message);
//
//        }
//
//
//        return super.onTouchEvent(event);
//    }

    public View findViewAtPoint(ViewGroup viewGroup, int x, int y) {

        View targetView = null;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            int[] location = new int[2];
            child.getLocationOnScreen(location);

            int childX = location[0];
            int childY = location[1];

            if (x >= childX && x <= (childX + child.getWidth()) &&
                    y >= childY && y <= (childY + child.getHeight())) {
                // 找到了目标控件
                targetView = child;
                targetView.performClick();
                break;
            }

            if (child instanceof ViewGroup) {
                View foundView = findViewAtPoint((ViewGroup) child, x, y);
                if (foundView != null) {
                    // 找到了目标控件
                    targetView = foundView;
                    break;
                }
            }
        }
        return targetView;
    }


    @Override
    public void onClientStatus(boolean isConnected) {

    }

    @Override
    public void onBitmapReceived(final Bitmap bitmap) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.e("绘制图片？","onBitmapReceived");
                screenImgView.setBitMap(bitmap);
            }
        });
    }

    @Override
    public void onClickReceived(ClickBean clickBean) {

    }

    @Override
    public void messageBack(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        ClickBean clickBean = new ClickBean();
        clickBean.clickX = x;
        clickBean.clickY = y;
        clickBean.orginWidth= ScreenUtils.getScreenWidth(this);
        clickBean.orginHeight=ScreenUtils.getScreenHeight(this);
        clickBean.density=getResources().getDisplayMetrics().density;
        String message = new Gson().toJson(clickBean);
        Log.e("::::sendClickLocation=",message.toString());
        webSocketServer.sendClickLocation(message);
    }
}
