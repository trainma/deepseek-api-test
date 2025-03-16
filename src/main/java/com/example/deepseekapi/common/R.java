package com.example.deepseekapi.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果类
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    /**
     * 成功返回结果
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 成功返回结果
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return 响应结果
     */
    public static <T> R<T> ok(T data) {
        return ok(data, "操作成功");
    }

    /**
     * 成功返回结果
     *
     * @param data    返回数据
     * @param message 返回消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> R<T> ok(T data, String message) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setData(data);
        r.setMessage(message);
        r.setSuccess(true);
        return r;
    }

    /**
     * 失败返回结果
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> R<T> fail() {
        return fail("操作失败");
    }

    /**
     * 失败返回结果
     *
     * @param message 返回消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> R<T> fail(String message) {
        return fail(null, message, 500);
    }

    /**
     * 失败返回结果
     *
     * @param data    返回数据
     * @param message 返回消息
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> R<T> fail(T data, String message) {
        return fail(data, message, 500);
    }

    /**
     * 失败返回结果
     *
     * @param data    返回数据
     * @param message 返回消息
     * @param code    状态码
     * @param <T>     数据类型
     * @return 响应结果
     */
    public static <T> R<T> fail(T data, String message, Integer code) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMessage(message);
        r.setSuccess(false);
        return r;
    }

    /**
     * 未授权返回结果
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> R<T> unauthorized() {
        return fail(null, "未授权", 401);
    }

    /**
     * 禁止访问返回结果
     *
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> R<T> forbidden() {
        return fail(null, "禁止访问", 403);
    }
}
