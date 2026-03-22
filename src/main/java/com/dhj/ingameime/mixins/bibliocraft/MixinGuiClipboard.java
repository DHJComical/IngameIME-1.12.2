package com.dhj.ingameime.mixins.bibliocraft;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.control.BiblioCraftControl;
import com.dhj.ingameime.control.IControl;
import jds.bibliocraft.gui.GuiBiblioTextField;
import jds.bibliocraft.gui.GuiClipboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiClipboard.class, remap = false)
public class MixinGuiClipboard {

    @Shadow
    private GuiBiblioTextField textField0;
    @Shadow
    private GuiBiblioTextField textField1;
    @Shadow
    private GuiBiblioTextField textField2;
    @Shadow
    private GuiBiblioTextField textField3;
    @Shadow
    private GuiBiblioTextField textField4;
    @Shadow
    private GuiBiblioTextField textField5;
    @Shadow
    private GuiBiblioTextField textField6;
    @Shadow
    private GuiBiblioTextField textField7;
    @Shadow
    private GuiBiblioTextField textField8;
    @Shadow
    private GuiBiblioTextField textFieldTitle;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void onInitGui(CallbackInfo ci) {
        IngameIME_Forge.logDebugInfo("[BiblioCraft] GuiClipboard initialized");
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void onMouseClicked(int par1, int par2, int par3, CallbackInfo ci) {
        IngameIME_Forge.logDebugInfo("[BiblioCraft] mouseClicked called at ({}, {})", par1, par2);
        ingameIME_1_12_2$updateControlFocus();
    }

    @Inject(method = "keyTyped", at = @At("HEAD"))
    private void onKeyTyped(char par1, int par2, CallbackInfo ci) {
        IngameIME_Forge.logDebugInfo("[BiblioCraft] keyTyped called with char={}, key={}", par1, par2);
        ingameIME_1_12_2$updateActiveControl();
    }

    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    private void onGuiClosed(CallbackInfo ci) {
        IngameIME_Forge.logDebugInfo("[BiblioCraft] GuiClipboard closed");
        ClientProxy.INSTANCE.onControlFocus(null, false, false);
    }

    @Unique
    private void ingameIME_1_12_2$updateControlFocus() {
        GuiBiblioTextField[] fields = ingameIME_1_12_2$getTextFieldArray();
        IControl activeControl = null;

        for (int i = 0; i < fields.length; i++) {
            GuiBiblioTextField field = fields[i];
            if (field != null && ingameIME_1_12_2$isFieldEnabled(field) && ingameIME_1_12_2$isFieldFocused(field)) {
                IngameIME_Forge.logDebugInfo("[BiblioCraft] Found focused field: textField{}", i);
                activeControl = new BiblioCraftControl(field);
                break;
            }
        }

        if (activeControl != null) {
            IngameIME_Forge.logDebugInfo("[BiblioCraft] Setting control focus: {}", activeControl.getClass().getSimpleName());
            ClientProxy.INSTANCE.onControlFocus(activeControl, true, false);
        } else {
            IngameIME_Forge.logDebugInfo("[BiblioCraft] No focused field found");
        }
    }

    @Unique
    private void ingameIME_1_12_2$updateActiveControl() {
        GuiBiblioTextField[] fields = ingameIME_1_12_2$getTextFieldArray();

        for (GuiBiblioTextField field : fields) {
            if (field != null && ingameIME_1_12_2$isFieldEnabled(field) && ingameIME_1_12_2$isFieldFocused(field)) {
                IControl control = new BiblioCraftControl(field);
                ClientProxy.INSTANCE.onControlFocus(control, true, false);
                return;
            }
        }
    }

    @Unique
    private GuiBiblioTextField[] ingameIME_1_12_2$getTextFieldArray() {
        return new GuiBiblioTextField[]{
            textField0,
            textField1,
            textField2,
            textField3,
            textField4,
            textField5,
            textField6,
            textField7,
            textField8,
            textFieldTitle
        };
    }

    @Unique
    private boolean ingameIME_1_12_2$isFieldEnabled(GuiBiblioTextField field) {
        AccessorGuiBiblioTextField accessor = (AccessorGuiBiblioTextField) field;
        return accessor.isEnabled();
    }

    @Unique
    private boolean ingameIME_1_12_2$isFieldFocused(GuiBiblioTextField field) {
        AccessorGuiBiblioTextField accessor = (AccessorGuiBiblioTextField) field;
        return accessor.isFocused();
    }
}
