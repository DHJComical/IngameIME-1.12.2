package com.dhj.ingameime;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.IJeiRuntime;

@JEIPlugin
public class JEICompat implements IModPlugin {

    private static IJeiRuntime jeiRuntime;

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JEICompat.jeiRuntime = runtime;
    }

    public static String getJEIFilterText() {
        if (jeiRuntime != null) {
            return jeiRuntime.getIngredientFilter().getFilterText();
        }
        return "";
    }

    public static void setJEIFilterText(String text) {
        if (jeiRuntime != null) {
            jeiRuntime.getIngredientFilter().setFilterText(text);
        }
    }
}