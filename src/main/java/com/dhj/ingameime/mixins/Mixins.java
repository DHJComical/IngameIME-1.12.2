package com.dhj.ingameime.mixins;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

import javax.annotation.Nonnull;

public enum Mixins implements IMixins {

    // Read the java doc of IMixins and MixinBuilder for further information

    // You should declare all of your mixins early and late in this same enum

    EXAMPLE(new MixinBuilder()
        .setPhase(Phase.EARLY)
        .addClientMixins("MixinMinecraft")
        .addClientMixins("MixinGuiTextField")
        .addClientMixins("AccessorGuiChat")
        .addClientMixins("AccessorGuiTextField")
        .addClientMixins("AccessorGuiScreen")
        .addClientMixins("MixinGuiSlot")
        .addClientMixins("MixinGuiTextFieldAccess")
    ),

    BETTERQUESTING(new MixinBuilder()
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetMods.BETTERQUESTING)
        .addClientMixins("betterquesting.MixinPanelTextField")
        .addClientMixins("betterquesting.AccessorPanelTextField")
    ),

    BIBLIOCRAFT(new MixinBuilder()
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetMods.BIBLIOCRAFT)
        .addClientMixins("bibliocraft.MixinGuiClipboard")
        .addClientMixins("bibliocraft.AccessorGuiBiblioTextField")
    ),

    NOTES(new MixinBuilder()
        .setPhase(Phase.LATE)
        .addRequiredMod(TargetMods.NOTES)
        .addClientMixins("notes.MixinGuiNoteTitleField")
        .addClientMixins("notes.AccessorGuiNoteTitleField")
        .addClientMixins("notes.MixinGuiNoteTextField")
        .addClientMixins("notes.AccessorGuiNoteTextField")
    );

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
