package com.dhj.ingameime.mixins.journeymap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "journeymap.client.ui.fullscreen.Fullscreen", remap = false)
public interface AccessorJourneyMapFullscreen {
    @Accessor("searchTextX")
    Object getSearchTextX();

    @Accessor("searchTextZ")
    Object getSearchTextZ();
}
