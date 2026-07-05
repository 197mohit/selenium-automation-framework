package com.paytm.utils.merchant.user.wallet

import com.paytm.framework.conditions.Condition
import com.paytm.utils.merchant.util.exception.walletException.WalletException
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import lombok.experimental.PackagePrivate

import static com.paytm.utils.merchant.Constants.WALLET_HOST
import static io.restassured.RestAssured.given

trait Wallet implements Comparable<Wallet> {

    @PackagePrivate
    final RequestSpecification reqSpec = new RequestSpecBuilder()
            .addFilters(Arrays.asList(new RequestLoggingFilter(), new ResponseLoggingFilter()))
            .setBaseUri(WALLET_HOST)
            .setContentType(ContentType.JSON)
            .build()

    abstract String getName()

    double getBalance() {
        String balance = given()
                .spec(reqSpec)
                .headers(["ssotoken": user.tokens.find { it.name == 'sso' }.id])
                .body(
                [
                        request: [
                                is_detailinfo: "yes"
                        ]
                ]
        )
                .post("/service/checkUserBalance")
                .path("response.subWalletDetailsList.find { it.subWalletName == '${this.name.toUpperCase()}' }.balance")
        if (balance == null) throw new WalletException("unable to fetch $this balance")
        return balance as double
    }

    void setBalance(double amt) {
        double current = this.balance
        double diff = amt - current
        if (diff == 0D) return
        else if (diff >= 1D) this + diff
        else if (diff > 0D && diff < 1D) {
            this + (1D + diff)
            this - 1D
        } else this - diff.abs()
    };

    abstract void plus(double amt);

    abstract void minus(double amt);

    void add(double amt) { this + amt }

    void remove(double amt) { this - amt }

    void clear() { this.balance = 0D }

    void addAll() { this.limits.add.breach() }

    boolean isEmpty() {
        this.size() == 0D
    }

    double size() {
        this.getBalance()
    }

    abstract WalletLimits getLimits();

    @Override
    int compareTo(Wallet wallet) {
        double diff = this.balance - wallet.balance
        return diff == 0 ? 0 : diff / diff.abs() as int
    }

    boolean equals(Object obj) {
        obj instanceof Wallet && obj.user == this.user && obj.name == this.name
    }
}