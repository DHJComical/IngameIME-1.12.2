package com.dhj.ingameime.theme;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * 通用整数类型适配器，支持十六进制格式（0x 前缀）和十进制格式
 */
public class HexIntegerTypeAdapter extends TypeAdapter<Integer> {

    @Override
    public void write(JsonWriter out, Integer value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        // 默认写入为十进制数字
        out.value(value);
    }

    @Override
    public Integer read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return 0;
        }

        String value;
        // 处理数字类型
        if (in.peek() == JsonToken.NUMBER) {
            return in.nextInt();
        } else {
            // 处理字符串类型
            value = in.nextString();
            try {
                value = value.trim();
                // 移除十六进制前缀
                if (value.toLowerCase().startsWith("0x")) {
                    return Integer.parseInt(value.substring(2), 16);
                } else if (value.startsWith("#")) {
                    return Integer.parseInt(value.substring(1), 16);
                } else {
                    return Integer.parseInt(value);
                }
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}
