package jp.gr.java_conf.uzresk.samples.spark.emr.utils;

import java.util.ResourceBundle;

public class Resource {

    private static final ResourceBundle RESOURCE;
    static {
        RESOURCE = ResourceBundle.getBundle("run");
    }

    public static String getString(String key) {
        return RESOURCE.getString(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(RESOURCE.getString(key));
    }
}
