package com.dhj.ingameime;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class JEICompat implements IModPlugin {

    private static IJeiRuntime jeiRuntime;

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

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JEICompat.jeiRuntime = runtime;
    }
}