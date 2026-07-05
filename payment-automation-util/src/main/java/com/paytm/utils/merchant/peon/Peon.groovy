package com.paytm.utils.merchant.peon

import io.restassured.response.Response

interface Peon {

    String getName()

    String getOrderId()

    Peon get()

    Map map()

    Response response()
}