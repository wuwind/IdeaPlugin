package com.wuhf.findviews.beans;

public class ResIdBean {
    private String name;
    private String id;
    private String fieldName;

    public ResIdBean(String name, String id) {
        this.name = name;
        this.id = id;
        String[] split = id.split("_");
        if (split.length <= 1) {
            setFieldName(id);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(split[0]);
        for (int i = 1; i < split.length; i++) {
            String s = split[i].toUpperCase();
            char c = s.charAt(0);
            stringBuilder.append(c);
            stringBuilder.append(split[i], 1, s.length());
        }
        setFieldName(stringBuilder.toString());
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
