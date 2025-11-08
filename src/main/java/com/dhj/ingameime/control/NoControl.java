package com.dhj.ingameime.control;

import com.dhj.ingameime.Internal;

import java.io.IOException;

public class NoControl implements IControl {

    public static final NoControl NO_CONTROL = new NoControl();

    private int x;
    private int y;

    @Override
    public Object getControlObject() {
        return null;
    }

    @Override
    public void writeText(String text) throws IOException {
        if (Internal.getActivated()) {
            AbstractControl.writeCurrentScreenText(text);
        }
    }

    @Override
    public boolean isVisible() {
        return Internal.getActivated();
    }

    @Override
    public int getCursorX() {
        return this.x;
    }

    @Override
    public int getCursorY() {
        return this.y;
    }

    public void setCursorX(int x) {
        this.x = x;
    }

    public void setCursorY(int y) {
        this.y= y;
    }
}
