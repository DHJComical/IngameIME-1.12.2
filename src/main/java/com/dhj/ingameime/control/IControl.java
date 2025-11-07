package com.dhj.ingameime.control;

import java.io.IOException;

/**
 * Universal interface for all types of text fields.
 */
public interface IControl {
    Object getControlObject();

    void writeText(String text) throws IOException;
    boolean isVisible();

    int getCursorX();
    int getCursorY();
}
