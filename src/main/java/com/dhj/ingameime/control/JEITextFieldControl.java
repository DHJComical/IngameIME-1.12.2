package com.dhj.ingameime.control;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.UnicodeTextHelper;
import com.dhj.ingameime.mixins.vanilla.AccessorGuiTextField;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.input.GuiTextFieldFilter;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;
import java.awt.*;

public class JEITextFieldControl extends VanillaTextFieldControl<GuiTextFieldFilter> {

    public static final String JEI_MOD_ID = "jei";

    protected JEITextFieldControl(GuiTextFieldFilter control) {
        super(control);
    }

    @Override
    public void writeText(String text) {
        text = UnicodeTextHelper.repairUtf8Mojibake(text);
        if (text == null || text.isEmpty()) {
            return;
        }
        // FIXME: It just works!
        this.controlObject.writeText(text);
        int cursorPos = this.controlObject.getCursorPosition();
        Plugin.setJEIFilterText(this.controlObject.getText());
        this.controlObject.setCursorPosition(cursorPos);
    }

    @Nonnull
    @Override
    public Point getCursorPos() {
        com.dhj.ingameime.mixins.vanilla.AccessorGuiTextField accessor = (AccessorGuiTextField) this.controlObject;
        return AbstractControl.getCursorPos(
                accessor.getFont(), this.controlObject.getText(),
                this.controlObject.x, this.controlObject.y, this.controlObject.width, this.controlObject.height,
                accessor.getLineScrollOffset(), this.controlObject.getCursorPosition(), this.controlObject.getSelectionEnd(),
                true
        );
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

    @JEIPlugin
    public static class Plugin implements IModPlugin {
        private static IJeiRuntime jeiRuntime;

        // Necessary to stop JEI erroring
        public Plugin() {
        }

        public static void setJEIFilterText(String text) {
            if (jeiRuntime != null) {
                jeiRuntime.getIngredientFilter().setFilterText(text);
            }
        }

        @Override
        public void onRuntimeAvailable(@Nonnull IJeiRuntime runtime) {
            jeiRuntime = runtime;
        }
    }
}
