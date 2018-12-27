package com.aspirecn.safekeyboard.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.aspirecn.safekeyboard.R;
import com.aspirecn.safekeyboard.utils.DensityUtil;
import com.aspirecn.safekeyboard.utils.ScreenUtil;
import com.aspirecn.safekeyboard.widget.SafeEditView;
import com.aspirecn.safekeyboard.widget.SafeKeyboardView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.keyboard_view)
    SafeKeyboardView keyboardView;
    @BindView(R.id.edit_view)
    SafeEditView safeEditView;
    @BindView(R.id.ll_keyboard)
    LinearLayout llKeyboard;
    @BindView(R.id.ll_guan)
    LinearLayout llGuan;

    private int height = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSubView();
        initEvent();
    }

    private void initEvent() {
        safeEditView.setOnKeyboardListener(new SafeEditView.OnKeyboardListener() {
            @Override
            public void onHide(boolean isCompleted) {
                if (height > 0) {
                    llGuan.scrollBy(0, -(height + DensityUtil.dp2px(MainActivity.this, 16)));
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
                        safeEditView.getLocationOnScreen(pos);
                        //编辑框的Bottom坐标和键盘Top坐标的差
                        height = (pos[1] + safeEditView.getHeight())
                                - (ScreenUtil.getScreenHeight(MainActivity.this) - keyboardView.getHeight());
                        if (height > 0) {
                            //编辑框和键盘之间预留出16dp的距离
                            llGuan.scrollBy(0, height + DensityUtil.dp2px(MainActivity.this, 16));
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
        safeEditView.setEditView(llKeyboard, keyboardView, true);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
    }
}
