package com.dhj.ingameime.mixins.journeymap;

import journeymap.client.ui.fullscreen.FullscreenTextBoxButton;
import journeymap.client.ui.fullscreen.MapChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "journeymap.client.ui.fullscreen.Fullscreen", remap = false)
public interface AccessorJourneyMapFullscreen {
    @Accessor("searchTextX")
    FullscreenTextBoxButton ingameime$getSearchTextX();

    @Accessor("searchTextZ")
    FullscreenTextBoxButton ingameime$getSearchTextZ();

    @Accessor("chat")
    MapChat ingameime$getChat();
}
