package com.aspirecn.safeguard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SafeEditText extends EditText implements SafeKeyboardView.OnKeyboardActionListener {
    private Context context;
    private Keyboard keyboardNumber;
    private Keyboard keyboardEnglish;
    private ViewGroup viewGroup;
    private SafeKeyboardView keyboardView;
    private final int KEYCODE_CLEAR = -9;
    private boolean isShiftMode = true;
    private boolean isCapital = false;

    private OnKeyboardListener onKeyboardListener;

    public SafeEditText(Context context) {
        this(context, null);
    }

    public SafeEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SafeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initEditView();
    }

    private void initEditView() {
        keyboardNumber = new Keyboard(context, R.xml.keyboard_number);
        keyboardEnglish = new Keyboard(context, R.xml.keyboard_english);
    }

    public void setEditKeyboardView(ViewGroup viewGroup, SafeKeyboardView keyboardView, boolean isNumber) {
        this.viewGroup = viewGroup;
        this.keyboardView = keyboardView;
        this.isShiftMode = isNumber;

        if (isNumber) {
            garbleKeyboardNumber();
            keyboardView.setKeyboard(keyboardNumber);
            keyboardView.setCurrentKeyboard(0);
        } else {
            garbleKeyboardEnglish();
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
                    && isNumber(keyList.get(i).label.toString())) {
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

    private void garbleKeyboardEnglish() {
        List<Keyboard.Key> keyList = keyboardEnglish.getKeys();
        List<Keyboard.Key> newkeyList = new ArrayList<Keyboard.Key>();
        for (int i = 0; i < keyList.size(); i++) {
            if (keyList.get(i).label != null
                    && isKey(keyList.get(i).label.toString())) {
                newkeyList.add(keyList.get(i));
            }
        }

        List<KeyModel> resultList = new ArrayList<KeyModel>();
        LinkedList<KeyModel> interList = new LinkedList<KeyModel>();
        for (int i = 0; i < newkeyList.size(); i++) {
            interList.add(new KeyModel(97 + i, Character.toString((char)(i+97))));
        }

        Random random = new Random();
        for (int i = 0; i < newkeyList.size(); i++) {
            int num = random.nextInt(newkeyList.size() - i);
            resultList.add(new KeyModel(interList.get(num).getCode(),
                    interList.get(num).getLable()));
            interList.remove(num);
        }

        for (int i = 0; i < newkeyList.size(); i++) {
            if (isCapital) {
                newkeyList.get(i).label = resultList.get(i).getLable().toUpperCase();
                newkeyList.get(i).codes[0] = resultList.get(i).getCode() - 32;
            } else {
                newkeyList.get(i).label = resultList.get(i).getLable();
                newkeyList.get(i).codes[0] = resultList.get(i).getCode();
            }
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        closeKeyboard(this);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closeKeyboard(this);
        keyboardView = null;
        viewGroup = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        requestFocus();
        requestFocusFromTouch();
        closeKeyboard(this);
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
        if (isShiftMode) {
            return;
        }
        // setPreview(primaryCode);
    }

    @Override
    public void onRelease(int primaryCode) {
        switch (primaryCode) {
            case Keyboard.KEYCODE_DONE:
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
            case Keyboard.KEYCODE_MODE_CHANGE:
                shiftKeyboardMode();
                break;
            case Keyboard.KEYCODE_DELETE:
                if (editable != null && editable.length() > 0 && start > 0) {
                    editable.delete(start - 1, start);
                }
                break;
            case Keyboard.KEYCODE_SHIFT:
                shiftKeyboardCapital();
                keyboardView.setKeyboard(keyboardEnglish);
                break;
            case Keyboard.KEYCODE_DONE:
                break;
            case KEYCODE_CLEAR:
                editable.clear();
                break;
            default:
                editable.insert(start, Character.toString((char) primaryCode));
                break;
        }

        requestFocus();
        requestFocusFromTouch();
    }

    private void shiftKeyboardMode() {
        if (isShiftMode) {
            garbleKeyboardEnglish();
            keyboardView.setKeyboard(keyboardEnglish);
            keyboardView.setCurrentKeyboard(1);
        } else {
            garbleKeyboardNumber();
            keyboardView.setKeyboard(keyboardNumber);
            keyboardView.setCurrentKeyboard(0);
        }
        isShiftMode = !isShiftMode;
    }

    private void shiftKeyboardCapital() {
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

    private void setPreview(int primaryCode) {
        List<Integer> list = Arrays.asList(Keyboard.KEYCODE_MODE_CHANGE, Keyboard.KEYCODE_DELETE, Keyboard.KEYCODE_SHIFT, Keyboard.KEYCODE_DONE, 32);
        if (list.contains(primaryCode)) {
            keyboardView.setPreviewEnabled(false);
        } else {
            keyboardView.setPreviewEnabled(true);
        }
    }

    private boolean isKey(String key) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        if (lowercase.indexOf(key.toLowerCase()) > -1) {
            return true;
        }
        return false;
    }

    private boolean isNumber(String num) {
        return "0123456789".contains(num);
    }

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

    private void closeKeyboard(EditText editText) {
        InputMethodManager im = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void show() {
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
        void onHide(boolean isCompleted);
        void onShow();
        void onPress(int primaryCode);
    }

    class KeyModel {
        private Integer code;
        private String label;

        public KeyModel(Integer code, String lable) {
            this.code = code;
            this.label = lable;
        }

        public Integer getCode() {
            return code;
        }

        public String getLable() {
            return label;
        }
    }

    public void setOnKeyboardListener(OnKeyboardListener onKeyboardListener) {
        this.onKeyboardListener = onKeyboardListener;
    }
}