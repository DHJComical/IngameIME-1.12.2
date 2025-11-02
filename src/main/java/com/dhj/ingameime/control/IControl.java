package com.dhj.ingameime.control;

import java.io.IOException;

/**
 * Universal interface for all types of text fields.
 */
public interface IControl {
    boolean isControlObject(Object o);
    Object getControlObject();
    void writeText(String text) throws IOException;

    int getCursorX();
    int getCursorY();
}
