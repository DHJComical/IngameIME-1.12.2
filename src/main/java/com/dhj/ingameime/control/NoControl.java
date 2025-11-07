package com.dhj.ingameime.control;

public class NoControl implements IControl {

    public static final NoControl NO_CONTROL = new NoControl();

    @Override
    public Object getControlObject() {
        return null;
    }

    @Override
    public void writeText(String text) {
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public int getCursorX() {
        return 0;
    }

    @Override
    public int getCursorY() {
        return 0;
    }
}
