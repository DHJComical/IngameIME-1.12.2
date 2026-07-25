package com.dhj.ingameime.mixins.vanilla;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.Internal;
import com.dhj.ingameime.UnicodeTextHelper;
import com.dhj.ingameime.control.DirectTextFieldControl;
import com.dhj.ingameime.control.JEITextFieldControl;
import com.dhj.ingameime.control.VanillaTextFieldControl;
import journeymap.client.ui.fullscreen.Fullscreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {
    @Unique
    private static final String JOURNEYMAP_MOD_ID = "journeymap";
    @Unique
    private int ingameime$lastCursorPosition = -1;
    @Unique
    private boolean ingameime$lastLeadingSlash = false;
    @Unique
    private boolean ingameime$adjustingCursor = false;
    @Unique
    private boolean ingameime$adjustingSelection = false;

    @Inject(method = "setFocused(Z)V", at = @At("HEAD"))
    private void onSetFocus(boolean isFocusedIn, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;

        try {
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if (Loader.isModLoaded(JOURNEYMAP_MOD_ID) && currentScreen instanceof Fullscreen) {
                if (!ingameime$isJourneyMapSearchField(self, (Fullscreen) currentScreen)) {
                    return;
                }
            }

            if (Loader.isModLoaded(JEITextFieldControl.JEI_MOD_ID)
                && JEITextFieldControl.onFocusChange(self, isFocusedIn)) {
                return;
            }
            if (currentScreen != null && currentScreen.getClass().getName().startsWith("hunternif.mc.atlas")) {
                DirectTextFieldControl.onFocusChange(self, isFocusedIn);
                return;
            }
            VanillaTextFieldControl.onFocusChange(self, isFocusedIn);
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error(
                "IngameIME failed to handle focus change. This is a compatibility issue but the game was prevented from crashing.",
                t);
            System.err.println("IngameIME caught an error during focus change, preventing a crash: " + t.getMessage());
        }
    }

    @Inject(method = "textboxKeyTyped(CI)Z", at = @At("HEAD"), cancellable = true)
    private void ingameime$handleClusterDelete(char typedChar, int keyCode, CallbackInfoReturnable<Boolean> cir) {
        GuiTextField self = (GuiTextField) (Object) this;
        if (!self.isFocused()) {
            return;
        }

        AccessorGuiTextField accessor = (AccessorGuiTextField) self;
        if (!accessor.isEnabled()) {
            return;
        }
        if (GuiScreen.isCtrlKeyDown()) {
            return;
        }

        int cursor = self.getCursorPosition();
        int selection = self.getSelectionEnd();
        String text = self.getText();
        if (keyCode == Keyboard.KEY_BACK && cursor == selection) {
            int[] range = ingameime$previousClusterRange(text, cursor);
            if (ingameime$shouldDeleteCluster(range)) {
                ingameime$logClusterDelete("BACK", text, cursor, selection, range);
                self.setCursorPosition(range[1]);
                self.deleteFromCursor(-(range[1] - range[0]));
                cir.setReturnValue(true);
            }
        } else if (keyCode == Keyboard.KEY_DELETE && cursor == selection) {
            int[] range = ingameime$nextClusterRange(text, cursor);
            if (ingameime$shouldDeleteCluster(range)) {
                ingameime$logClusterDelete("DELETE", text, cursor, selection, range);
                self.setCursorPosition(range[0]);
                self.deleteFromCursor(range[1] - range[0]);
                cir.setReturnValue(true);
            }
        }
    }

    /**
     * Vanilla {@code writeText} truncates the inserted text at a UTF-16 char index
     * ({@code s1.substring(0, k)}), which splits a surrogate pair or emoji cluster when the
     * field is nearly full. The filtered text is pre-truncated here at a grapheme cluster
     * boundary that never exceeds the length vanilla would allow, so vanilla's own length
     * and cursor math (including {@code l = k}) keeps working on cluster-safe input.
     */
    @Redirect(
            method = "writeText(Ljava/lang/String;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ChatAllowedCharacters;filterAllowedCharacters(Ljava/lang/String;)Ljava/lang/String;"))
    private String ingameime$filterWriteTextClusterSafe(String textToWrite) {
        String filtered = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);

        GuiTextField self = (GuiTextField) (Object) this;
        int cursor = self.getCursorPosition();
        int selection = self.getSelectionEnd();
        int selectionStart = cursor < selection ? cursor : selection;
        int selectionEnd = cursor < selection ? selection : cursor;
        // Same inputs vanilla uses to compute its truncation length k in writeText.
        int available = self.getMaxStringLength() - self.getText().length() - (selectionStart - selectionEnd);

        if (available <= 0 || available >= filtered.length()) {
            return filtered;
        }

        int aligned = GraphemeClusters.floorClusterBoundary(filtered, available);
        if (aligned != available) {
            IngameIME_Forge.logDebugInfo(
                    "[IME ClusterWriteText] available={} aligned={} inserted='{}' text='{}'",
                    available,
                    aligned,
                    UnicodeTextHelper.debugEscaped(filtered.substring(0, aligned)),
                    UnicodeTextHelper.debugEscaped(self.getText())
            );
        }
        return filtered.substring(0, aligned);
    }

    @Inject(method = "setFocused(Z)V", at = @At("TAIL"))
    private void onSetFocusTail(boolean isFocusedIn, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;

        if (!isFocusedIn) {
            ingameime$lastCursorPosition = -1;
            ingameime$lastLeadingSlash = false;
            return;
        }

        ingameime$lastCursorPosition = self.getCursorPosition();
        ingameime$handleChatCommandMode(self);
    }

    @Inject(method = "setCursorPosition(I)V", at = @At("TAIL"))
    private void onCursorPositionChanged(int pos, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;
        if (!self.isFocused()) {
            return;
        }

        if (!ingameime$adjustingCursor) {
            int cursor = self.getCursorPosition();
            int direction = ingameime$lastCursorPosition >= 0 && cursor < ingameime$lastCursorPosition ? -1 : 1;
            int aligned = ingameime$alignClusterBoundary(self.getText(), cursor, direction);
            if (aligned != cursor) {
                IngameIME_Forge.logDebugInfo(
                        "[IME ClusterCursorAlign] cursor={} aligned={} direction={} text='{}'",
                        cursor,
                        aligned,
                        direction,
                        UnicodeTextHelper.debugEscaped(self.getText())
                );
                ingameime$adjustingCursor = true;
                self.setCursorPosition(aligned);
                ingameime$adjustingCursor = false;
                return;
            }
        }

        int cursor = self.getCursorPosition();
        if (cursor == ingameime$lastCursorPosition) {
            return;
        }

        ingameime$lastCursorPosition = cursor;
        ingameime$handleChatCommandMode(self);
    }

    @Inject(method = "setSelectionPos(I)V", at = @At("TAIL"))
    private void ingameime$onSelectionPosChanged(int position, CallbackInfo ci) {
        if (ingameime$adjustingSelection) {
            return;
        }

        GuiTextField self = (GuiTextField) (Object) this;
        if (!self.isFocused()) {
            return;
        }

        int selection = self.getSelectionEnd();
        int direction = selection < self.getCursorPosition() ? -1 : 1;
        int aligned = ingameime$alignClusterBoundary(self.getText(), selection, direction);
        if (aligned != selection) {
            IngameIME_Forge.logDebugInfo(
                    "[IME ClusterSelectionAlign] selection={} aligned={} direction={} text='{}'",
                    selection,
                    aligned,
                    direction,
                    UnicodeTextHelper.debugEscaped(self.getText())
            );
            ingameime$adjustingSelection = true;
            self.setSelectionPos(aligned);
            ingameime$adjustingSelection = false;
        }
    }

    @Unique
    private void ingameime$handleChatCommandMode(GuiTextField self) {
        if (!ingameime$isChatInputField(self)) {
            ingameime$lastLeadingSlash = false;
            return;
        }

        String text = self.getText();
        boolean leadingSlash = text != null && text.startsWith("/");

        if (leadingSlash) {
            if (self.getCursorPosition() == 1) {
                Internal.forceAlphaMode();
            }
        } else if (ingameime$lastLeadingSlash) {
            Internal.forceNativeMode();
        }

        ingameime$lastLeadingSlash = leadingSlash;
    }

    @Unique
    private boolean ingameime$isChatInputField(GuiTextField self) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (!(currentScreen instanceof GuiChat)) {
            return false;
        }
        return ((AccessorGuiChat) currentScreen).getInputField() == self;
    }

    @Unique
    private boolean ingameime$isJourneyMapSearchField(GuiTextField field, Fullscreen screen) {
        // JourneyMap is a compile-time dependency (modImplementation in
        // gradle/scripts/dependencies.gradle), but Fullscreen#searchTextX/searchTextZ are
        // package-private members of the journeymap.client.ui.fullscreen package with no
        // public getter, so they cannot be referenced directly and are read reflectively.
        try {
            Field fieldX = Fullscreen.class.getDeclaredField("searchTextX");
            fieldX.setAccessible(true);
            Object searchTextX = fieldX.get(screen);
            if (searchTextX != null && searchTextX == field) {
                return true;
            }

            Field fieldZ = Fullscreen.class.getDeclaredField("searchTextZ");
            fieldZ.setAccessible(true);
            Object searchTextZ = fieldZ.get(screen);
            if (searchTextZ != null && searchTextZ == field) {
                return true;
            }
        } catch (Exception e) {
            IngameIME_Forge.LOG.warn(
                "IngameIME failed to read JourneyMap fullscreen search fields; IME focus handling on the JourneyMap fullscreen map may not engage.",
                e);
        }
        return false;
    }

    @Unique
    private int[] ingameime$previousClusterRange(String text, int cursor) {
        if (text == null || cursor <= 0 || cursor > text.length()) {
            return null;
        }
        int[] previous = null;
        for (int start = 0; start < text.length(); ) {
            int end = GraphemeClusters.nextClusterEnd(text, start);
            if (cursor <= start) {
                return previous;
            }
            if (cursor <= end) {
                return new int[] {start, end};
            }
            previous = new int[] {start, end};
            start = end;
        }
        return previous;
    }

    @Unique
    private int[] ingameime$nextClusterRange(String text, int cursor) {
        if (text == null || cursor < 0 || cursor >= text.length()) {
            return null;
        }
        for (int start = 0; start < text.length(); ) {
            int end = GraphemeClusters.nextClusterEnd(text, start);
            if (cursor < end) {
                return new int[] {start, end};
            }
            start = end;
        }
        return null;
    }

    @Unique
    private int ingameime$alignClusterBoundary(String text, int position, int direction) {
        if (text == null || position <= 0 || position >= text.length()) {
            return position;
        }
        for (int start = 0; start < text.length(); ) {
            int end = GraphemeClusters.nextClusterEnd(text, start);
            if (position <= start) {
                return position;
            }
            if (position < end && end - start > 1) {
                return direction < 0 ? start : end;
            }
            start = end;
        }
        return position;
    }

    @Unique
    private boolean ingameime$shouldDeleteCluster(int[] range) {
        return range != null && range[1] - range[0] > 1;
    }

    @Unique
    private void ingameime$logClusterDelete(String key, String text, int cursor, int selection, int[] range) {
        String removed = text.substring(range[0], range[1]);
        IngameIME_Forge.logDebugInfo(
                "[IME ClusterDelete] key={} cursor={} selection={} range=[{},{}] removed='{}' removedUtf16=[{}] removedCp=[{}] textBefore='{}'",
                key,
                cursor,
                selection,
                range[0],
                range[1],
                UnicodeTextHelper.debugEscaped(removed),
                UnicodeTextHelper.debugUtf16(removed),
                UnicodeTextHelper.debugCodePoints(removed),
                UnicodeTextHelper.debugEscaped(text)
        );
    }

    /**
     * Grapheme cluster boundary logic shared by the GuiTextField cluster operations. Kept
     * free of Minecraft and mixin references so the logic can be unit-tested in isolation.
     */
    static final class GraphemeClusters {
        private GraphemeClusters() {
        }

        /**
         * End offset (exclusive) of the grapheme cluster that starts at {@code start}.
         */
        static int nextClusterEnd(String text, int start) {
            int i = start;
            int first = text.codePointAt(i);
            i += Character.charCount(first);

            if (isRegionalIndicator(first) && i < text.length()) {
                int next = text.codePointAt(i);
                if (isRegionalIndicator(next)) {
                    return i + Character.charCount(next);
                }
            }

            while (i < text.length()) {
                int next = text.codePointAt(i);
                if (next == 0x200D) {
                    i += Character.charCount(next);
                    if (i < text.length()) {
                        i += Character.charCount(text.codePointAt(i));
                    }
                } else if (isClusterContinuation(next)) {
                    i += Character.charCount(next);
                } else {
                    break;
                }
            }
            return i;
        }

        /**
         * Largest grapheme cluster boundary that is still {@code <= limit}. Rolls a UTF-16
         * char-based truncation length back to a cluster-safe cut point.
         */
        static int floorClusterBoundary(String text, int limit) {
            int boundary = 0;
            for (int start = 0; start < limit && start < text.length(); ) {
                int end = nextClusterEnd(text, start);
                if (end > limit) {
                    break;
                }
                boundary = end;
                start = end;
            }
            return boundary;
        }

        private static boolean isClusterContinuation(int codePoint) {
            if (codePoint == 0xFE0E
                    || codePoint == 0xFE0F
                    || (codePoint >= 0xE0100 && codePoint <= 0xE01EF)) {
                return true;
            }
            int type = Character.getType(codePoint);
            if (type == Character.NON_SPACING_MARK
                    || type == Character.COMBINING_SPACING_MARK
                    || type == Character.ENCLOSING_MARK) {
                return true;
            }
            return codePoint == 0x20E3
                    || (codePoint >= 0x1F3FB && codePoint <= 0x1F3FF)
                    || (codePoint >= 0xE0020 && codePoint <= 0xE007F);
        }

        private static boolean isRegionalIndicator(int codePoint) {
            return codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF;
        }
    }
}
