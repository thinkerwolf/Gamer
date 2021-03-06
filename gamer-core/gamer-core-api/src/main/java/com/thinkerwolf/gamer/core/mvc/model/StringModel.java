package com.thinkerwolf.gamer.core.mvc.model;

import java.nio.charset.Charset;

public class StringModel implements Model<String> {

    public static final String NAME = "string";

    private String data;

    private Charset charset;

    public StringModel(String data, Charset charset) {
        this.data = data;
        this.charset = charset;
    }

    public StringModel(String data) {
        this.data = data;
        this.charset = Charset.forName("UTF-8");
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public byte[] getBytes() {
        return data.getBytes(charset);
    }
}
