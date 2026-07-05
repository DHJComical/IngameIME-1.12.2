package com.dhj.ingameime.theme;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ColorTypeAdapter extends TypeAdapter<Number> {

    @Override
    public void write(JsonWriter out, Number value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(String.format("0x%08X", value.intValue()));
    }

    @Override
    public Number read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return 0;
        }

        String hex;
        // 处理数字类型（包括十进制负数和正数）
        if (in.peek() == JsonToken.NUMBER) {
            long longValue = in.nextLong();
            return (int) longValue;
        } else {
            // 处理字符串类型（十六进制格式）
            hex = in.nextString();
            try {
                // 移除所有可能的空白字符和引号
                hex = hex.trim().replace("\"", "").replace("'", "");
                // 移除十六进制前缀
                if (hex.toLowerCase().startsWith("0x")) {
                    hex = hex.substring(2);
                } else if (hex.startsWith("#")) {
                    hex = hex.substring(1);
                }
                // 处理可能的前导 0x（如果还有残留）
                hex = hex.replace("0x", "").replace("0X", "");
                return (int) Long.parseLong(hex, 16);
            } catch (NumberFormatException e) {
                return 0xFF000000;
            }
        }
    }
}
