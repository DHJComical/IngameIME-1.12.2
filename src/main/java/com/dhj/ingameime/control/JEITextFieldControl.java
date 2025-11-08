package com.dhj.ingameime.control;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.JEICompat;
import com.dhj.ingameime.mixins.vanilla.AccessorGuiTextField;
import mezz.jei.input.GuiTextFieldFilter;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.Optional;

public class JEITextFieldControl extends VanillaTextFieldControl<GuiTextFieldFilter> {

    public static final String JEI_MOD_ID = "jei";

    protected JEITextFieldControl(GuiTextFieldFilter control) {
        super(control);
    }

    @Override
    public void writeText(String text) {
        // FIXME: It just works!
        this.controlObject.writeText(text);
        int cursorPos = this.controlObject.getCursorPosition();
        JEICompat.setJEIFilterText(this.controlObject.getText());
        this.controlObject.setCursorPosition(cursorPos);
    }

    @Override
    public int getCursorX() {
        AccessorGuiTextField accessor = (AccessorGuiTextField) this.controlObject;
        return AbstractControl.getCursorX(accessor.getFont(), this.controlObject.getText(),
                this.controlObject.x, this.controlObject.getWidth(),
                accessor.getLineScrollOffset(), this.controlObject.getCursorPosition(),
                true);
    }

    @Override
    public int getCursorY() {
        return AbstractControl.getCursorY(this.controlObject.y, this.controlObject.height, true);
    }

    /**
     * Try to set the GuiTextField object focus.
     *
     * @param object The field to be set
     * @return Success or not
     */
    @Optional.Method(modid = JEI_MOD_ID)
    public static boolean onFocusChange(GuiTextField object, boolean focused) {
        if (object instanceof GuiTextFieldFilter) {
            ClientProxy.INSTANCE.onControlFocus(new JEITextFieldControl((GuiTextFieldFilter) object), focused, true);
            return true;
        }
        return false;
    }
}
