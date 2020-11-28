package com.wwc2.dvr.parse;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class JsonParse implements ResponseParse {

    @Override
    public <E> E paserObject(Object result, Class<E> clazz) {
        // TODO Auto-generated method stub
        return new Gson().fromJson(result.toString(), clazz);
    }

    @Override
    public <E> E paserCollection(Object result, Type typeToken) {
        // TODO Auto-generated method stub
        return new Gson().fromJson(result.toString(), typeToken);
        /**
         * new TypeToken<T>() {
         *         }.getType()
         */
    }

    @Override
    public String paserJson(Object object) {
        return new Gson().toJson(object);
    }

}
