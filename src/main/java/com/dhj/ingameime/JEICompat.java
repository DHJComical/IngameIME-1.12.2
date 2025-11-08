package com.dhj.ingameime;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;

import javax.annotation.Nonnull;

@JEIPlugin
public class JEICompat implements IModPlugin {

    private static IJeiRuntime jeiRuntime;

    public static void setJEIFilterText(String text) {
        if (jeiRuntime != null) {
            jeiRuntime.getIngredientFilter().setFilterText(text);
        }
    }

    @Override
    public void onRuntimeAvailable(@Nonnull IJeiRuntime runtime) {
        JEICompat.jeiRuntime = runtime;
    }
}