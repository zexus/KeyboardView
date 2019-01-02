package com.aspirecn.safekeyboard.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.aspirecn.safeguard.AntiHijack;
import com.aspirecn.safeguard.AntiScreenRecord;
import com.aspirecn.safeguard.SafeEditText;
import com.aspirecn.safeguard.SafeKeyboardView;
import com.aspirecn.safekeyboard.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.safe_keyboard_view)
    SafeKeyboardView keyboardView;

    @BindView(R.id.edit_text)
    SafeEditText safeEditText;

    @BindView(R.id.ll_keyboard)
    LinearLayout llKeyboard;

    @BindView(R.id.ll_guan)
    LinearLayout llGuan;

    private int height = 0;

    @Override
    protected void onStop() {
        super.onStop();
        new Thread(new Runnable() {
            @Override
            public void run() {
                AntiHijack.checkHijack(getApplicationContext());
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSubView();
        initEvent();
    }

    private void initEvent() {
        safeEditText.setOnKeyboardListener(new SafeEditText.OnKeyboardListener() {
            @Override
            public void onHide(boolean isCompleted) {
                if (height > 0) {
                    int displayHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getApplicationContext().getResources().getDisplayMetrics());
                    llGuan.scrollBy(0, -(height + displayHeight));
                }

                if (isCompleted) {
                    Log.i("", "你点击了完成按钮");
                }
            }

            @Override
            public void onShow() {
                llGuan.post(new Runnable() {
                    @Override
                    public void run() {
                        //pos[0]: X，pos[1]: Y
                        int[] pos = new int[2];
                        //获取编辑框在整个屏幕中的坐标
                        safeEditText.getLocationOnScreen(pos);
                        //编辑框的Bottom坐标和键盘Top坐标的差
                        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        wm.getDefaultDisplay().getMetrics(displayMetrics);
                        height = (pos[1] + safeEditText.getHeight())
                                - (displayMetrics.heightPixels - keyboardView.getHeight());
                        if (height > 0) {
                            //编辑框和键盘之间预留出16dp的距离
                            int displayHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getApplicationContext().getResources().getDisplayMetrics());
                            llGuan.scrollBy(0, height + displayHeight);
                        }
                    }
                });
            }

            @Override
            public void onPress(int primaryCode) {

            }
        });
    }

    private void setSubView() {
        safeEditText.setEditView(llKeyboard, keyboardView, true);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
//                WindowManager.LayoutParams.FLAG_SECURE);
        AntiScreenRecord.disable(getWindow());
    }
}
