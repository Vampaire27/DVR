package com.wwc2.dvr.parse;

import java.lang.reflect.Type;

public class ResultPaser {
    private static ResponseParse parse;

    static {
        parse = new JsonParse();
    }
    public static <E> E paserObject(Object result, Class<E> clazz) {
        return parse.paserObject(result, clazz);
    }

    public static <E> E paserCollection(Object result, Type typeToken) {

        return parse.paserCollection(result, typeToken);
    }

    public static String JsonParse(Object object) {

        return parse.paserJson(object);
    }

}
