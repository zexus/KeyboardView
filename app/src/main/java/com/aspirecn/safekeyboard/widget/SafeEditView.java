package com.aspirecn.safekeyboard.widget;


import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aspirecn.safekeyboard.R;
import com.aspirecn.safekeyboard.container.KeyModel;
import com.aspirecn.safekeyboard.utils.SystemUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * 自定义键盘，在输入密码等安全性要求较高的时候使用此编辑框
 *
 * @author Phoenix
 * @date 2016-12-7 16:49
 */
public class SafeEditView extends EditText implements SafeKeyboardView.OnKeyboardActionListener {
    private Context context;

    private Keyboard keyboardNumber;
    private Keyboard keyboardEnglish;
    private ViewGroup viewGroup;
    private SafeKeyboardView keyboardView;

    //标识数字键盘和英文键盘的切换
    private boolean isShift = true;
    //标识英文键盘大小写切换
    private boolean isCapital = false;

    //点击【完成】、键盘隐藏、键盘显示时的回调
    private OnKeyboardListener onKeyboardListener;

    public SafeEditView(Context context) {
        this(context, null);
    }

    public SafeEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SafeEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initEditView();
    }

    /**
     * 初始化自定义键盘
     */
    private void initEditView() {
        keyboardNumber = new Keyboard(context, R.xml.keyboard_number);
        keyboardEnglish = new Keyboard(context, R.xml.keyboard_english);
    }

    /**
     * 设置键盘
     *
     * @param viewGroup
     * @param keyboardView
     * @param isNumber     true:表示默认数字键盘，false：表示默认英文键盘
     */
    public void setEditView(ViewGroup viewGroup, SafeKeyboardView keyboardView, boolean isNumber) {
        this.viewGroup = viewGroup;
        this.keyboardView = keyboardView;
        this.isShift = isNumber;

        if (isNumber) {
            garbleKeyboardNumber();
            keyboardView.setKeyboard(keyboardNumber);
            keyboardView.setCurrentKeyboard(0);
        } else {
            keyboardView.setKeyboard(keyboardEnglish);
            keyboardView.setCurrentKeyboard(1);
        }
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(!isNumber);
        keyboardView.setOnKeyboardActionListener(this);
    }

    private void garbleKeyboardNumber() {
        List<Keyboard.Key> keyList = keyboardNumber.getKeys();
        List<Keyboard.Key> newkeyList = new ArrayList<Keyboard.Key>();
        for (int i = 0; i < keyList.size(); i++) {
            if (keyList.get(i).label != null
                    && "0123456789".contains(keyList.get(i).label.toString())) {
                newkeyList.add(keyList.get(i));
            }
        }

        List<KeyModel> resultList = new ArrayList<KeyModel>();
        LinkedList<KeyModel> interList = new LinkedList<KeyModel>();
        for (int i = 0; i < newkeyList.size(); i++) {
            interList.add(new KeyModel(48 + i, i + ""));
        }

        Random random = new Random();
        for (int i = 0; i < newkeyList.size(); i++) {
            int num = random.nextInt(newkeyList.size() - i);
            resultList.add(new KeyModel(interList.get(num).getCode(),
                    interList.get(num).getLable()));
            interList.remove(num);
        }

        for (int i = 0; i < newkeyList.size(); i++) {
            newkeyList.get(i).label = resultList.get(i).getLable();
            newkeyList.get(i).codes[0] = resultList.get(i).getCode();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        SystemUtil.closeKeyboard(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        SystemUtil.closeKeyboard(this);
        keyboardView = null;
        viewGroup = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        requestFocus();
        requestFocusFromTouch();
        SystemUtil.closeKeyboard(this);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!isShow()) {
                show();
            }
        }
        return true;
    }

    @Override
    public void onPress(int primaryCode) {
        if (onKeyboardListener != null) {
            onKeyboardListener.onPress(primaryCode);
        }
        if (isShift) {
            return;
        }
        setPreview(primaryCode);
    }

    @Override
    public void onRelease(int primaryCode) {
        switch (primaryCode) {
            case Keyboard.KEYCODE_DONE:// 完成-4
                hide(true);
                break;

            default:
                break;
        }
    }

    @Override
    public void onKey(int primaryCode, int[] ints) {
        Editable editable = getText();
        int start = getSelectionStart();

        switch (primaryCode) {
            case Keyboard.KEYCODE_MODE_CHANGE:// 英文键盘与数字键盘切换-2
                shiftKeyboard();
                break;
            case Keyboard.KEYCODE_DELETE:// 回退-5
                if (editable != null && editable.length() > 0 && start > 0) {
                    editable.delete(start - 1, start);
                }
                break;
            case Keyboard.KEYCODE_SHIFT:// 英文大小写切换-1
                shiftEnglish();
                keyboardView.setKeyboard(keyboardEnglish);
                break;
            case Keyboard.KEYCODE_DONE:// 完成-4
                break;

            default:
                editable.insert(start, Character.toString((char) primaryCode));
                break;
        }
    }

    /**
     * 切换键盘
     */
    private void shiftKeyboard() {
        if (isShift) {
            keyboardView.setKeyboard(keyboardEnglish);
            keyboardView.setCurrentKeyboard(1);
        } else {
            keyboardView.setKeyboard(keyboardNumber);
            keyboardView.setCurrentKeyboard(0);
        }
        isShift = !isShift;
    }

    /**
     * 英文键盘大小写切换
     */
    private void shiftEnglish() {
        List<Keyboard.Key> keyList = keyboardEnglish.getKeys();
        for (Keyboard.Key key : keyList) {
            if (key.label != null && isKey(key.label.toString())) {
                if (isCapital) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                } else {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
            }
        }
        isCapital = !isCapital;
    }

    /**
     * 判断是否需要预览Key
     *
     * @param primaryCode keyCode
     */
    private void setPreview(int primaryCode) {
        List<Integer> list = Arrays.asList(Keyboard.KEYCODE_MODE_CHANGE, Keyboard.KEYCODE_DELETE, Keyboard.KEYCODE_SHIFT, Keyboard.KEYCODE_DONE, 32);
        if (list.contains(primaryCode)) {
            keyboardView.setPreviewEnabled(false);
        } else {
            keyboardView.setPreviewEnabled(true);
        }
    }

    /**
     * 判断此key是否正确，且存在
     *
     * @param key
     * @return
     */
    private boolean isKey(String key) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        if (lowercase.indexOf(key.toLowerCase()) > -1) {
            return true;
        }
        return false;
    }

    /**
     * 设置键盘隐藏
     *
     * @param isCompleted true：表示点击了【完成】
     */
    public void hide(boolean isCompleted) {
        int visibility = keyboardView.getVisibility();
        if (visibility == View.VISIBLE) {
            keyboardView.setVisibility(View.INVISIBLE);
            if (viewGroup != null) {
                viewGroup.setVisibility(View.GONE);
            }
        }
        if (onKeyboardListener != null) {
            onKeyboardListener.onHide(isCompleted);
        }
    }

    /**
     * 设置键盘对话框显示，并且屏幕上移
     */
    public void show() {
        //设置键盘显示
        int visibility = keyboardView.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            keyboardView.setVisibility(View.VISIBLE);
            if (viewGroup != null) {
                viewGroup.setVisibility(View.VISIBLE);
            }
        }
        if (onKeyboardListener != null) {
            onKeyboardListener.onShow();
        }
    }

    /**
     * 键盘状态
     *
     * @return true：表示键盘开启 false：表示键盘隐藏
     */
    public boolean isShow() {
        return keyboardView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            hide(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public interface OnKeyboardListener {
        /**
         * 键盘隐藏了
         *
         * @param isCompleted true：表示点击了【完成】
         */
        void onHide(boolean isCompleted);

        /**
         * 键盘弹出了
         */
        void onShow();

        /**
         * 按下
         *
         * @param primaryCode
         */
        void onPress(int primaryCode);
    }

    /**
     * 对外开放的方法
     *
     * @param onKeyboardListener
     */
    public void setOnKeyboardListener(OnKeyboardListener onKeyboardListener) {
        this.onKeyboardListener = onKeyboardListener;
    }
}
