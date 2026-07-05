package com.paytm.utils.merchant

import groovy.transform.MapConstructor
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.parsing.Parser
import io.restassured.specification.ResponseSpecification

import static org.hamcrest.Matchers.equalTo

class Result {

    String code
    String status
    String msg

    Result(String code, String status, String msg) {
        this.code = code
        this.status = status
        this.msg = msg
    }

    ResponseSpecification asType(Class<?> aClass) {
        if (aClass == ResponseSpecification.class) {
            return new ResponseSpecBuilder()
                    .setDefaultParser(Parser.JSON)
                    .rootPath('body.resultInfo')
                    .expectBody('resultStatus', equalTo(status))
                    .expectBody('resultCode', equalTo(code))
                    .expectBody('resultMsg', equalTo(msg))
                    .build()
        } else throw new ClassCastException()
    }
}
