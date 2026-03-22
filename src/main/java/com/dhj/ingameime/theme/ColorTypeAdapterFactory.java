package com.dhj.ingameime.theme;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * TypeAdapterFactory that serializes int colors as hexadecimal strings
 */
public class ColorTypeAdapterFactory implements TypeAdapterFactory {
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // Only handle Integer/int types
        if (!type.getRawType().equals(Integer.class) && !type.getRawType().equals(int.class)) {
            return null;
        }
        
        return (TypeAdapter<T>) new ColorTypeAdapter();
    }
    
    private static class ColorTypeAdapter extends TypeAdapter<Integer> {
        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }
            // Write as hexadecimal string with 0x prefix
            out.value(String.format("0x%08X", value.intValue()));
        }

        @Override
        public Integer read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return 0;
            }

            String hex = in.nextString();
            try {
                // Support multiple formats: 0xRRGGBBAA, #RRGGBBAA, RRGGBBAA
                hex = hex.trim();
                if (hex.startsWith("0x") || hex.startsWith("0X")) {
                    hex = hex.substring(2);
                } else if (hex.startsWith("#")) {
                    hex = hex.substring(1);
                }
                return (int) Long.parseLong(hex, 16);
            } catch (NumberFormatException e) {
                // Try parsing as integer directly
                try {
                    return Integer.parseInt(hex);
                } catch (NumberFormatException e2) {
                    return 0xFF000000; // Default to opaque black
                }
            }
        }
    }
}
