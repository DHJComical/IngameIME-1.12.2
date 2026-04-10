package com.dhj.ingameime.rust;

/**
 * Rust-based IngameIME native library interface.
 * This provides a clean Java API for the Rust IME core.
 * <p>
 * Note: Native library is loaded by Internal class.
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

    /**
     * Create a new input context for Win32 (default uiLess=false).
     */

    public static long createInputContextWin32(long hWnd, int api, boolean uiLessWindows) {
        return rust_ime_library_create_input_context_win32(hWnd, api, uiLessWindows);
    }

    public static long createInputContext(long windowHandle, int api, boolean uiLess) {
        return rust_ime_library_create_input_context_win32(windowHandle, api, uiLess);
    }

    /**
     * Destroy an input context.
     */
    public static native void rust_ime_library_destroy_input_context(long contextPtr);

    public static void destroyInputContext(long inputCtx) {
        rust_ime_library_destroy_input_context(inputCtx);
    }

    /**
     * Set whether the input method is activated.
     */
    public static native void rust_ime_library_set_input_context_activated(long contextPtr, boolean activated);

    public static void setInputContextActivated(long contextPtr, boolean activated){
        rust_ime_library_set_input_context_activated(contextPtr, activated);
    }

    /**
     * Get whether the input method is activated.
     */
    public static native boolean rust_ime_library_is_input_context_activated(long contextPtr);

    public static boolean isInputContextActivated (long inputCtx) {
        return rust_ime_library_is_input_context_activated(inputCtx);
    }

    /**
     * Get the current input mode.
     * @return 0=Alpha, 1=Native, 2=Unsupported
     */
    public static native int rust_ime_library_get_input_mode(long contextPtr, int mode);

    public static int getInputMode(long inputCtx, int mode) {
        return rust_ime_library_get_input_mode(inputCtx,  mode);
    }

    /**
     * Force switch IME to English/alphanumeric mode.
     */
    public static native void rust_ime_library_force_alpha_mode(long contextPtr);

    public static void forceAlphaMode(long contextPtr) {
        rust_ime_library_force_alpha_mode(contextPtr);
    }

    /**
     * Force switch IME to native mode.
     */
    public static native void rust_ime_library_force_native_mode(long contextPtr);

    public static void forceNativeMode(long contextPtr) {
        rust_ime_library_force_native_mode(contextPtr);
    }

    /**
     * Set the preedit rectangle for candidate window positioning.
     */
    public static native void rust_ime_library_set_pre_edit_rect(long contextPtr, int x, int y, int width, int height);

    public static void setPreEditRect(long contextPtr, int x, int y, int width, int height) {
        rust_ime_library_set_pre_edit_rect(contextPtr, x, y, width, height);
    }

    /**
     * Get the library version string.
     * @return Version string in format "major.minor.patch"
     */
    public static native String rust_ime_library_get_version();
    
    public static String getVersion() {
        return rust_ime_library_get_version();
    }

    /**
     * Set the maximum number of candidates to display per page.
     * @param contextPtr Input context pointer
     * @param maxCandidates Maximum number of candidates (default: 9)
     */
    public static native void rust_ime_library_set_max_candidates(long contextPtr, int maxCandidates);

    public static void setMaxCandidates(long contextPtr, int maxCandidates) {
        rust_ime_library_set_max_candidates(contextPtr, maxCandidates);
    }

    /**
     * Get the maximum number of candidates to display per page.
     * @param contextPtr Input context pointer
     * @return Maximum number of candidates
     */
    public static native int rust_ime_library_get_max_candidates(long contextPtr);

    public static int getMaxCandidates(long contextPtr) {
        return rust_ime_library_get_max_candidates(contextPtr);
    }

    /**
     * Enable or disable debug logging in Rust backend.
     * @param enabled true to enable debug logging
     */
    public static native void rust_ime_library_set_debug_logging(boolean enabled);

    public static void setDebugLogging(boolean enabled) {
        rust_ime_library_set_debug_logging(enabled);
    }

    /**
     * Initialize Rust logger with Java's Log4j logger.
     * This forwards Rust logs to Java's logger.
     */
    public static native void rust_ime_library_init_logger(Object logger);

    public static void initLogger(org.apache.logging.log4j.Logger logger) {
        // Wrap logger in a simple object that Rust can call methods on
        rust_ime_library_init_logger(new LoggerWrapper(logger));
    }

    /**
     * Wrapper to expose Log4j logger methods to JNI
     */
    @SuppressWarnings("unused")
    public static class LoggerWrapper {
        private final org.apache.logging.log4j.Logger logger;

        public LoggerWrapper(org.apache.logging.log4j.Logger logger) {
            this.logger = logger;
        }

        public void info(String msg) { logger.info(msg); }
        public void debug(String msg) { logger.debug(msg); }
        public void warn(String msg) { logger.warn(msg); }
        public void error(String msg) { logger.error(msg); }
    }

    // Callback interfaces

    /**
     * Callback for commit text events.
     */
    public interface CommitCallback {
        void onCommit(String text);
    }

    /**
     * Callback for preedit events.
     * @param state 0=Begin, 1=Update, 2=End
     * @param content Preedit content (null for Begin/End)
     * @param cursor Cursor position in preedit
     */
    public interface PreEditCallback {
        void onPreEdit(int state, String content, int cursor);
    }

    /**
     * Callback for candidate list events.
     * @param state 0=Begin, 1=Update, 2=End
     * @param candidates Array of candidate strings
     * @param selected Selected candidate index
     */
    public interface CandidateListCallback {
        void onCandidateList(int state, String[] candidates, int selected);
    }

    /**
     * Callback for input mode changes.
     * @param mode 0=Alpha, 1=Native, 2=Unsupported
     */
    public interface InputModeCallback {
        void onInputModeChanged(int mode);
    }

    // Callback registration

    /**
     * Register a commit callback.
     */
    public static native void rust_ime_library_set_commit_callback(long contextPtr, CommitCallback callback);

    public static void setCommitCallback(long inputCtx, CommitCallback commitCallback) {
        rust_ime_library_set_commit_callback(inputCtx, commitCallback);
    }

    /**
     * Register a preedit callback.
     */
    public static native void rust_ime_library_set_pre_edit_callback(long contextPtr, PreEditCallback callback);

    public static void setPreEditCallback (long inputCtx, PreEditCallback preEditCallback) {
        rust_ime_library_set_pre_edit_callback(inputCtx, preEditCallback);
    }

    /**
     * Register a candidate list callback.
     */
    public static native void rust_ime_library_set_candidate_list_callback(long contextPtr, CandidateListCallback callback);

    public static void setCandidateListCallback(long inputCtx, CandidateListCallback candidateListCallback) {
        rust_ime_library_set_candidate_list_callback(inputCtx, candidateListCallback);
    }

    /**
     * Register an input mode callback.
     */
    public static native void rust_ime_library_set_input_mode_callback(long contextPtr, InputModeCallback callback);

    public static void setInputModeCallback(long inputCtx, InputModeCallback inputModeCallback) {
        rust_ime_library_set_input_mode_callback(inputCtx, inputModeCallback);
    }

}
