package com.dhj.ingameime;

/**
 * Converts LWJGL2 X11 key event fields to the values expected by IBus.
 */
public final class LinuxKeyEventConverter {
    private static final int X11_KEYCODE_OFFSET = 8;
    private static final int X11_KEY_PRESS = 2;
    private static final int X11_KEY_RELEASE = 3;

    private LinuxKeyEventConverter() {}

    /** Validates that an X11 keysym can cross the current JNI jint boundary. */
    public static int toJniKeyval(long keyval) {
        if (keyval < 0 || keyval > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("X11 keysym is outside the supported JNI jint range: " + keyval);
        }
        return (int) keyval;
    }

    /** Converts the X11 keycode range to the evdev-based keycodes used by IBus. */
    public static int toIbusKeycode(int x11Keycode) {
        if (x11Keycode < X11_KEYCODE_OFFSET) {
            throw new IllegalArgumentException("X11 hardware keycode must be at least 8: " + x11Keycode);
        }
        return x11Keycode - X11_KEYCODE_OFFSET;
    }

    /** Rejects modifier states that cannot be represented by the Rust u32 API. */
    public static void validateState(int state) {
        if (state < 0) {
            throw new IllegalArgumentException("X11 modifier state cannot be negative: " + state);
        }
    }

    /** Maps the X11 KeyPress and KeyRelease event types to the JNI release flag. */
    public static boolean isRelease(int eventType) {
        if (eventType == X11_KEY_PRESS) {
            return false;
        }
        if (eventType == X11_KEY_RELEASE) {
            return true;
        }
        throw new IllegalArgumentException("Unexpected X11 key event type: " + eventType);
    }

    /**
     * Decides whether IBus handling should prevent LWJGL2 from processing the event.
     * Releases must always reach LWJGL2 so its internal key state is cleared.
     */
    public static boolean shouldCancelLwjglHandler(boolean release, boolean handled) {
        return handled && !release;
    }
}
