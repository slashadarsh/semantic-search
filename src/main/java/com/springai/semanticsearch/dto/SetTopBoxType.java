package com.springai.semanticsearch.dto;

import java.util.HashMap;
import java.util.Map;

public enum SetTopBoxType {
    SYNAMEDIA("Synamedia"),
    IRDETO("Irdeto");
  //  ANDROID("Android");

    private final String value;

    SetTopBoxType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, SetTopBoxType> map = new HashMap<>();

    static {
        for (SetTopBoxType type : SetTopBoxType.values()) {
            map.put(type.getValue(), type);
        }
    }

    public static Map<String, SetTopBoxType> getTypeMap() {
        return map;
    }
}
