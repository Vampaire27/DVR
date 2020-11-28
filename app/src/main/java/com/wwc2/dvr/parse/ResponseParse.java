package com.wwc2.dvr.parse;

import java.lang.reflect.Type;

public interface ResponseParse {
    /**
     * @author FH
     * @Description: TODO 解析对象
     * @throws:throws
     */

    public <E> E paserObject(Object result, Class<E> clazz);

    /**
     * @author FH
     * @Description: TODO 解析集合
     * @throws:throws
     */

    public <E> E paserCollection(Object result, Type typeToken);

    /**
     * @param object
     * @return json
     * @Description: TODO 封装json对象
     */
    public String paserJson(Object object);

}
