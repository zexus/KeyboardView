package com.aspirecn.safekeyboard.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.aspirecn.safeguard.AntiHijack;
import com.aspirecn.safeguard.AntiScreenRecord;
import com.aspirecn.safeguard.SafeEditText;
import com.aspirecn.safeguard.SafeKeyboardView;
import com.aspirecn.safekeyboard.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.safe_keyboard_view) SafeKeyboardView keyboardView;
    @BindView(R.id.edit_text) SafeEditText safeEditText;
    @BindView(R.id.linearLayout_keyboard_view) LinearLayout linearLayout_keyboard_view;
    @BindView(R.id.linearLayout_edit_text) LinearLayout linearLayout_edit_text;

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
        safeEditText.setEditKeyboardView(linearLayout_keyboard_view, keyboardView, true);
        AntiScreenRecord.enable(getWindow());
    }
}
