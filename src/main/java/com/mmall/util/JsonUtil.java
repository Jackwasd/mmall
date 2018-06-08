package com.mmall.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;

@Slf4j
public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        //对象的所有字段全部列入,如果是NOT_NULL就是空字段不会出现在JSON字符串中了,
        // NON_DEFAULT的话就是说如果值和字段的默认值相等，该字段也不会出现
        // NOT_EMPTY和NOT_NULL相似，但是空字符串，空集合这种都是EMPTY的，但是不是NULL的，也就是说empty更严格一些
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);
        //取消默认转化timestamps形式，如果是true的话，就会显示时间戳，也就是1970年1月1号到现在的毫秒数
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        //忽略空bean转json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        //所有的日期格式都统一为以下样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));
        //忽略在json字符串中存在，但是在JAVA对象中不存在对应属性的情况。防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * 把一个对象转化成字符串
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String objToString(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Parse object to String error", e);
            return null;
        }
    }

    /**
     * 返回封装好的格式化的json字符串
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> String objToStringPretty(T obj){
        if(obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            log.warn("Parse object to String error", e);
            return null;
        }
    }

    /**
     * Json字符串转化为对象(反序列化),注意这个方法是只能使用单个类，集合的话不适用
     * @param str
     * @param clazz 这个表示要转化的对象的类的信息
     * @param <T>
     * @return
     */
    public static <T> T stringToObj(String str, Class<T> clazz){
        if(StringUtils.isEmpty(str) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T)str : objectMapper.readValue(str, clazz);
        } catch (IOException e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    /**
     * 反序列化为集合类的,通用的，可以覆盖上面的一种方法，只使用这一种
     * @param str
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T>  T stringToObj(String str, TypeReference<T> typeReference){
        if(StringUtils.isEmpty(str) || typeReference == null){
            return null;
        }
        try {
            return (T)(typeReference.getType().equals(String.class) ? str : objectMapper.readValue(str, typeReference));
        } catch (IOException e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }

    /**
     * 和上一个的方法差不多
     * @param str
     * @param collectionClass   传的是集合类，比方List.class. Set.class
     * @param elementClasses    可变长的参数  集合里面的元素的类
     * @param <T>
     * @return
     */
    public static <T>  T stringToObj(String str, Class<?> collectionClass, Class<?>...elementClasses){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
        try {
            return objectMapper.readValue(str, javaType);
        } catch (IOException e) {
            log.warn("Parse String to Object error", e);
            return null;
        }
    }
}
