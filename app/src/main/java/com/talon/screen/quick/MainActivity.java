package com.talon.screen.quick;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.talon.screen.quick.network.ClickBean;
import com.talon.screen.quick.network.MWebSocketClient;
import com.talon.screen.quick.network.MWebSocketServer;
import com.talon.screen.quick.util.BitmapUtils;
import com.talon.screen.quick.util.IPUtils;
import com.talon.screen.quick.util.LogWrapper;
import com.talon.screen.quick.util.ScreenShotHelper;
import com.talon.screen.quick.util.ScreenUtils;
import com.talon.screen.quick.util.ThreadPool;

import org.w3c.dom.Text;

import java.net.URI;

/**
 * @author by talon, Date on 2020/6/20.
 * note: 主界面
 */
public class MainActivity extends AppCompatActivity implements ScreenShotHelper.OnScreenShotListener, MWebSocketClient.CallBack {

    private final String TAG = "btn_quick";
    private static final int REQUEST_MEDIA_PROJECTION = 100;

    private TextView tv_ip;
    private RelativeLayout rootView;
    private Button btClick1, btClick2;

    private MWebSocketServer webSocketServer;
    private MWebSocketClient webSocketClient;
    private boolean socketIsStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_ip = findViewById(R.id.tv_ip);
        rootView = findViewById(R.id.rootView);
        btClick1 = findViewById(R.id.clickone);
        btClick2 = findViewById(R.id.clickTwo);
        btClick1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "点击了第一个button", Toast.LENGTH_SHORT).show();
            }
        });

        btClick2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "点击了第二个button", Toast.LENGTH_SHORT).show();
            }
        });
        webSocketServer = MWebSocketServer.getInstance();
        webSocketServer.setCallBack(new MWebSocketServer.CallBack() {
            @Override
            public void onServerStatus(boolean isStarted) {
                socketIsStarted = isStarted;
            }
        });
        webSocketServer.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        tv_ip.setText(String.format("当前IP：%s", IPUtils.getIpAddressString()));
    }

    /**
     * 推送端：1. 开启服务  2. 申请截图权限  3. 传输数据
     *
     * @param view
     */
    public void StartQuick(View view) {
        if (!socketIsStarted)
            Toast.makeText(this, "socket 服务启动异常！", Toast.LENGTH_SHORT).show();
        else
            tryStartScreenShot();
    }

    /**
     * 播放端：1. 输入IP  2. 接收到数据  3. 展示
     *
     * @param view
     */
    public void Join(View view) {
        showEditDialog();
    }


    public void connect(View view) {
        try {
            URI url = new URI("ws://" + "192.168.137.69" + ":" + Config.ANDROID_SERVER_PORT);
            webSocketClient = new MWebSocketClient(url, MainActivity.this);
            webSocketClient.setConnectionLostTimeout(5 * 1000);
            boolean flag = webSocketClient.connectBlocking();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION && data != null) {
            if (resultCode == RESULT_OK) {
                // 截屏的回调
                ScreenShotHelper screenShotHelper = new ScreenShotHelper(this, resultCode, data, this);
                screenShotHelper.startScreenShot();
            } else if (resultCode == RESULT_CANCELED) {
                LogWrapper.d(TAG, "用户取消");
            }
        }
    }

    /**
     * 申请截屏权限
     */
    private void tryStartScreenShot() {
        MediaProjectionManager mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mProjectionManager != null) {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        }

    }

    @Override
    public void onShotFinish(Bitmap bitmap) {
        LogWrapper.d(TAG, "bitmap:" + bitmap.getWidth());
        webSocketServer.sendBytes(BitmapUtils.getByteBitmap(bitmap));
    }

    private String host;

    private void showEditDialog() {
        final EditText editText = new EditText(this);
        editText.setText("192.168.2.112");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server").setIcon(android.R.drawable.ic_dialog_info).setView(editText)
                .setNegativeButton("Cancel", null);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                host = editText.getText().toString();
                if (!TextUtils.isEmpty(host)) {
                    Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                    intent.putExtra("host", host);
                    startActivity(intent);
                }
            }
        });
        builder.show();
    }


    public View findViewAtPoint(ViewGroup viewGroup, int x, int y) {
        Log.e("::::sendClickLocation=", x + "");
        Log.e("::::sendClickLocation=", y + "");

        int[] btClick1Location = new int[2];
        Log.e("::::btClick1 childID=", btClick1.getId() + "");
        btClick1.getLocationOnScreen(btClick1Location);
        int btClick1x = btClick1Location[0];
        int btClick1y = btClick1Location[1];
        Log.e("::::btClick1x=", btClick1x + "");
        Log.e("::::btClick1y=", btClick1y + "");
        Log.e("::::btClick1x w=", btClick1x + btClick1.getWidth() + "");
        Log.e("::::btClick1y h=", btClick1y + btClick1.getHeight() + "");
        int screenWidth = ScreenUtils.getScreenWidth(this);
        int screenHeight = ScreenUtils.getScreenHeight(this);
        Log.e("::::screenWidth=", screenWidth + "");
        Log.e("::::screenHeight=", screenHeight + "");

        View targetView = null;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            Log.e("::::child childCount =", viewGroup.getChildCount() + "");
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                Log.e("::::child text =", ((TextView) child).getText().toString() + "");
            }
            int[] location = new int[2];
            child.getLocationOnScreen(location);
            int childX = location[0];
            int childY = location[1];

            Log.e("::::childX=px", x + "");
            Log.e("::::childY=py", y + "");
            Log.e("::::childX=", childX + "");
            Log.e("::::childY=", childY + "");
            Log.e("::::childX+width=", childX + child.getWidth() + "");
            Log.e("::::childY+height=", childY + child.getHeight() + "");
            Log.e("::::childID=", child.getId() + "");
            if (x >= childX && x <= (childX + child.getWidth()) &&
                    y >= childY && y <= (childY + child.getHeight())) {
                // 找到了目标控件
                Log.e("::::找到了目标控件=", "true");
                Log.e("::::找到了目标控件=", child.getId() + "");
                targetView = child;
                if (child instanceof TextView) {
                    String djContext = ((TextView) child).getText().toString();
                    Log.e("::::child djContext =", djContext);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, djContext, Toast.LENGTH_LONG).show());
                }
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
        targetView.setBackgroundColor(Color.RED);
        return targetView;
    }


    @Override
    public void onClientStatus(boolean isConnected) {

    }

    @Override
    public void onBitmapReceived(Bitmap bitmap) {

    }

    @Override
    public void onClickReceived(ClickBean clickBean) {
        Log.e("::::onClickReceived=", clickBean.toString());
        if (clickBean == null) {
            return;
        }

        int x = 0;
        int y = 0;
        int currentWidth = ScreenUtils.getScreenWidth(this);
        int currentHight = ScreenUtils.getScreenHeight(this);
        float desenity = getResources().getDisplayMetrics().density;
        if (currentWidth == clickBean.orginWidth) {
            float scale = desenity / clickBean.density;
            x = (int) (clickBean.clickX * scale);
            y = (int) (clickBean.clickY * scale);
        } else {
            x = clickBean.clickX * (clickBean.orginWidth / ScreenUtils.getScreenWidth(this));
            y = clickBean.clickY * (clickBean.orginHeight / ScreenUtils.getScreenHeight(this));
        }

        findViewAtPoint(rootView, x, y);
    }
}
