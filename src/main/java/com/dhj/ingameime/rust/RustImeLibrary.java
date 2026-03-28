package com.dhj.ingameime.rust;

/**
 * Rust-based IngameIME native library interface.
 * This provides a clean Java API for the Rust IME core.
 * <p>
 * Note: Native library is loaded by Internal class.
 */
public class RustImeLibrary {

    // No static block - library is loaded by Internal.tryLoadLibrary()

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
