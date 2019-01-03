package com.aspirecn.safeguard;

import android.view.Window;
import android.view.WindowManager;

public class AntiScreenRecord {
    public static void enable(Window window) {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
    }
}
