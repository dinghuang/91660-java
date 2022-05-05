package com.example.demo.ticket;

import io.restassured.response.Response;

/**
 * @author dinghuang123@gmail.com
 * @since 2022/4/20
 */
@FunctionalInterface
public interface LoginFunction {
    Response execute();
}
