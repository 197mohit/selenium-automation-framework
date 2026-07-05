package com.paytm.utils.merchant.ff4j

import io.restassured.http.ContentType

import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.isIn

class FF4JFeature implements AutoCloseable {

    final String uri
    private final String username
    private final String password
    private String lockToken

    FF4JFeature(String uri, String username, String password) {
        this.uri = uri
        this.username = username
        this.password = password
    }

    String getId() {
        URLDecoder.decode(this.uri.split('/')[-1], 'UTF-8')
    }

    boolean isEnabled() {
        given()
                .header('Authorization', "Basic " + "$username:$password".getBytes().encodeBase64().toString())//TODO refactor it to use rest-assured authentication
                .get(this.uri)
                .then()
                .statusCode(200)
                .extract()
                .path('enabled')
    }

    boolean setEnabled(boolean enabled) {
        given()
                .header('Authorization', "Basic " + "$username:$password".getBytes().encodeBase64().toString())//TODO refactor it to use rest-assured authentication
                .header('Lock-Token', this.lockToken ?: '')
                .contentType(ContentType.JSON)
                .body([enabled: enabled])
                .patch(this.uri)
                .statusCode() == 204
    }

    String getExpression() {
        given()
                .header('Authorization', "Basic " + "$username:$password".getBytes().encodeBase64().toString())//TODO refactor it to use rest-assured authentication
                .get(this.uri)
                .then()
                .statusCode(200)
                .extract()
                .path('expression')
    }

    boolean setExpression(String expression) {
        given()
                .header('Authorization', "Basic " + "$username:$password".getBytes().encodeBase64().toString())//TODO refactor it to use rest-assured authentication
                .header('Lock-Token', this.lockToken)
                .body([expression: expression])
                .patch(this.uri)
                .statusCode() == 204
    }

    boolean lock() {
        def res = given()
                .request('LOCK', this.uri)
                .then()
                .statusCode(isIn(200, 423))
                .extract()
        if (res.statusCode() == 200) {
            this.lockToken = res.header('Lock-Token')
            return true
        } else if (res.statusCode() == 423) return false
    }

    boolean unlock() {
        given()
                .header('Lock-Token', this.lockToken ?: '')
                .request('UNLOCK', this.uri)
                .then()
                .statusCode(isIn(204))
        this.lockToken = null
        return true
    }

    @Override
    void close() throws Exception {
        if (!this.unlock()) throw new RuntimeException('Unknown exception occurred while trying unlocking the resource.')
    }
}
