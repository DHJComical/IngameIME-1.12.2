package com.dhj.ingameime.theme.api;

import com.dhj.ingameime.theme.HexIntegerTypeAdapter;
import com.google.gson.annotations.JsonAdapter;

public class ThemeDecoration {
    public String textureFile = "";
    
    @JsonAdapter(HexIntegerTypeAdapter.class)
    public int anchorPoint = 0;
    
    @JsonAdapter(HexIntegerTypeAdapter.class)
    public int imageAnchor = 0;
    
    @JsonAdapter(HexIntegerTypeAdapter.class)
    public int offsetX = 0;
    
    @JsonAdapter(HexIntegerTypeAdapter.class)
    public int offsetY = 0;
    
    public float scale = 1.0f;
    public float alpha = 1.0f;
    public String target = "all";
    
    @JsonAdapter(HexIntegerTypeAdapter.class)
    public int width = -1;
    
    @JsonAdapter(HexIntegerTypeAdapter.class)
    public int height = -1;
}