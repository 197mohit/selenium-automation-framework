package com.paytm.utils.merchant.ff4j

import static com.paytm.utils.merchant.Constants.MOCK_HOST
import static io.restassured.RestAssured.given

class FF4JFeatures implements Iterable<FF4JFeature> {

    private final String username
    private final String password

    FF4JFeatures(String username, String password) {
        this.username = username
        this.password = password
    }

    @Override
    Iterator<FF4JFeature> iterator() {
        Closure<Iterator> closure = {
            given()
                    .baseUri(MOCK_HOST)
                    .basePath('/mockbank/envs/automation/ff4j/features')
                    .header('Authorization', "Basic " + "$username:$password".getBytes().encodeBase64().toString())//TODO refactor it to use rest-assured authentication
                    .get()
                    .path('members')
                    .collect { new FF4JFeature(it as String, this.username, this.password) }
                    .iterator()
        }
        closure()
    }

    FF4JFeature getAt(String feature) {
        this.toList().find { it.id == feature }
    }

}
