package com.openclaw.game;

public class NativeTouchBridge {

    static {
        System.loadLibrary("openclaw");
    }

    public static native void nativeTouchAction(
        String action,
        boolean pressed,
        float x,
        float y
    );
}
