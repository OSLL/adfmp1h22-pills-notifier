package com.example.pillnotifier.model;

import java.util.HashMap;

public class DataHolder {
    private static final HashMap<String, String> data = new HashMap<>();
    public static String getData(String key) {
        return data.get(key);
    }
    public static void setData(String key, String value) {
        data.put(key, value);
    }
    public static void removeData(String key) {
        data.remove(key);
    }
}
