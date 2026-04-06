package com.dhj.ingameime.rust;

import com.dhj.ingameime.IngameIME_Forge;

/**
 * Rust-based IngameIME native library interface.
 */
public class RustImeLibrary {
    public static final String JNI_BIND_CLASS_PROPERTY = "ingameime.jni.bind_class";
    public static final String JNI_BIND_CLASS_NAME = RustImeLibrary.class.getName();

    // No static block - library is loaded by Internal.tryLoadLibrary()

    public static String getJniBindClassName() {
        return JNI_BIND_CLASS_NAME;
    }

    /**
     * Create a new input context for Win32.
     * @param hwnd Window handle
     * @param api API type (0=TSF, 1=IMM32)
     * @param uiLess Whether to hide candidate window
     * @return Context pointer, or 0 if failed
     */
    public static native long rust_ime_library_create_input_context_win32(long hwnd, int api, boolean uiLess);

    public static long createInputContextWin32(long hWnd, int api, boolean uiLessWindows) {
        return rust_ime_library_create_input_context_win32(hWnd, api, uiLessWindows);
    }

    public static native void rust_ime_library_destroy_input_context(long contextPtr);

    public static void destroyInputContext(long inputCtx) {
        rust_ime_library_destroy_input_context(inputCtx);
    }

    public static native void rust_ime_library_set_input_context_activated(long contextPtr, boolean activated);

    public static void setInputContextActivated(long contextPtr, boolean activated) {
        rust_ime_library_set_input_context_activated(contextPtr, activated);
    }

    public static native boolean rust_ime_library_is_input_context_activated(long contextPtr);

    public static boolean isInputContextActivated(long inputCtx) {
        return rust_ime_library_is_input_context_activated(inputCtx);
    }

    public static native int rust_ime_library_get_input_mode(long contextPtr, int mode);

    public static int getInputMode(long inputCtx, int mode) {
        return rust_ime_library_get_input_mode(inputCtx, mode);
    }

    public static native void rust_ime_library_force_alpha_mode(long contextPtr);

    public static void forceAlphaMode(long contextPtr) {
        rust_ime_library_force_alpha_mode(contextPtr);
    }

    public static native void rust_ime_library_force_native_mode(long contextPtr);

    public static void forceNativeMode(long contextPtr) {
        rust_ime_library_force_native_mode(contextPtr);
    }

    public static native void rust_ime_library_set_pre_edit_rect(long contextPtr, int x, int y, int width, int height);

    public static void setPreEditRect(long contextPtr, int x, int y, int width, int height) {
        rust_ime_library_set_pre_edit_rect(contextPtr, x, y, width, height);
    }

    public static native String rust_ime_library_get_version();

    public static String getVersion() {
        return rust_ime_library_get_version();
    }

    public static native void rust_ime_library_set_max_candidates(long contextPtr, int maxCandidates);

    public static void setMaxCandidates(long contextPtr, int maxCandidates) {
        rust_ime_library_set_max_candidates(contextPtr, maxCandidates);
    }

    public static native int rust_ime_library_get_max_candidates(long contextPtr);

    public static int getMaxCandidates(long contextPtr) {
        return rust_ime_library_get_max_candidates(contextPtr);
    }

    public static native void rust_ime_library_set_debug_logging(boolean enabled);

    public static void setDebugLogging(boolean enabled) {
        rust_ime_library_set_debug_logging(enabled);
    }

    public static native void rust_ime_library_init_logger(Object logger);

    public static void initLogger() {
        rust_ime_library_init_logger(new LoggerWrapper());
    }

    @SuppressWarnings("unused")
    public static class LoggerWrapper {
        public void info(String msg) {
            IngameIME_Forge.logDebugInfo("[Rust] {}", msg);
        }

        public void debug(String msg) {
            IngameIME_Forge.logDebugInfo("[Rust] {}", msg);
        }

        public void warn(String msg) {
            IngameIME_Forge.LOG.warn("[Rust] {}", msg);
        }

        public void error(String msg) {
            IngameIME_Forge.LOG.error("[Rust] {}", msg);
        }
    }

    public interface CommitCallback {
        void onCommit(String text);
    }

    public interface PreEditCallback {
        void onPreEdit(int state, String content, int cursor);
    }

    public interface CandidateListCallback {
        void onCandidateList(int state, String[] candidates, int selected);
    }

    public interface InputModeCallback {
        void onInputModeChanged(int mode);
    }

    public static native void rust_ime_library_set_commit_callback(long contextPtr, CommitCallback callback);

    public static void setCommitCallback(long inputCtx, CommitCallback commitCallback) {
        rust_ime_library_set_commit_callback(inputCtx, commitCallback);
    }

    public static native void rust_ime_library_set_pre_edit_callback(long contextPtr, PreEditCallback callback);

    public static void setPreEditCallback(long inputCtx, PreEditCallback preEditCallback) {
        rust_ime_library_set_pre_edit_callback(inputCtx, preEditCallback);
    }

    public static native void rust_ime_library_set_candidate_list_callback(long contextPtr, CandidateListCallback callback);

    public static void setCandidateListCallback(long inputCtx, CandidateListCallback candidateListCallback) {
        rust_ime_library_set_candidate_list_callback(inputCtx, candidateListCallback);
    }

    public static native void rust_ime_library_set_input_mode_callback(long contextPtr, InputModeCallback callback);

    public static void setInputModeCallback(long inputCtx, InputModeCallback inputModeCallback) {
        rust_ime_library_set_input_mode_callback(inputCtx, inputModeCallback);
    }
}
