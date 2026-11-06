package com.openclaw.game;

/** JNI bridge: native side posts SDL_USEREVENT; game thread consumes it. */
public final class NativeTouchBridge implements TouchOverlayView.ActionSink {
    static { System.loadLibrary("openclaw"); }
    private static native void nativeTouchAction(String action, boolean pressed, float x, float y);
    @Override public void onAction(String action, boolean pressed, float x, float y) {
        nativeTouchAction(action, pressed, x, y);
    }
}
