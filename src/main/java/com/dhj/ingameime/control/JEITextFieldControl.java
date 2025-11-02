package com.dhj.ingameime.control;

import com.dhj.ingameime.IMStates;
import com.dhj.ingameime.JEICompat;
import com.dhj.ingameime.mixins.vanilla.AccessorGuiTextField;
import mezz.jei.input.GuiTextFieldFilter;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import static com.dhj.ingameime.IngameIME_Forge.LOG;

public class JEITextFieldControl extends VanillaTextFieldControl<GuiTextFieldFilter> {

    private static final String JEI_MOD_ID = "jei";

    protected JEITextFieldControl(GuiTextFieldFilter control) {
        super(control);
    }

    @Override
    public void writeText(String text) {
        LOG.info("JEI text field detected, using JEI API to set text.");
        // Hacky. Fix it.
        this.controlObject.writeText(text);
        JEICompat.setJEIFilterText(this.controlObject.getText());
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
     * @param object The field to be set
     * @return Success or not
     */
    public static boolean onFocus(GuiTextField object) {
        if (!Loader.isModLoaded(JEI_MOD_ID)) return false;
        return onFocusOptional(object);
    }

    @Optional.Method(modid = JEI_MOD_ID)
    private static boolean onFocusOptional(GuiTextField object) {
        if (object instanceof GuiTextFieldFilter) {
            IMStates.setOverlayControl(new JEITextFieldControl((GuiTextFieldFilter) object));
            return true;
        }
        return false;
    }
}
