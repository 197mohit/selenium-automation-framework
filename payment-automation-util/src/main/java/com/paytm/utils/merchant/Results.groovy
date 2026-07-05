package com.paytm.utils.merchant

class Results implements Iterable<Result> {

    private final static String MID_PASSED_IN_QUERY_PARAMS_AND_REQUEST_BODY_DOES_NOT_MATCH = 'mid passed in query params and request body does not match'
    private final static String SOMETHING_WENT_WRONG = 'Something went wrong'
    private final static String NO_PROMO_CODE_ACTIVE = 'No promo code currently active on the merchant'
    private final static String CUST_ID_CANT_BE_BLANK_FOR_TOKEN_TYPE_CHECKSUM = "CustId can't be blank for tokenType CHECKSUM"
    private final static String SYSTEM_ERROR_INVALID_PARAM = "System Error, invalid param"
    private final static String WE_COULD_NOT_VERIFY_VPA = "Sorry! We could not verify the VPA."
    private final static String WE_COULD_NOT_VERIFY_UPI_ID_TRY_AGAIN_LATER = "Sorry! We could not verify the VPA. Please try again.";
    private final static String UNSUCCESSFUL_PAYMENT_REQUEST_TRY_AGAIN_LATER = "Your payment request was unsuccessful. Please try again later."
    private final static String UNSUCCESSFUL_PAYMENT_REQUEST_TRY_AGAIN = "Invalid VPA, Try Again"
    private final static String INVALID_UPI_ADDRESS = "Invalid UPI address"
    private final static String INVALID_UPI_ID = "Invalid VPA, Try Again"

    final Result success = this['0000']
    final Result invalidReqParams = this['1001']
    final Result invalidSSOToken = this['403']
    final Result sysErr = this['00000900']
    final Result qrCodeNotRecognised = this['QR_1018']
    final Result orderIdInQueryParamNotMatchingWithOrderIdInRequest = this['2014']
    final Result mIdInQueryParamNotMatchingWithmIdInRequest = this['2013']
    final Result sessionIsExpired = this['1006']
    final Result mIdAndOrderIdMandatoryInQueryParams = this.find {
        it.msg == 'Mid and OrderId are mandatory in query parameter'
    }
    final Result mIdMandatoryInQueryParams = this.find { it.msg == 'Mid is mandatory in query parameter' }
    final Result orderIdMandatoryInQueryParams = this.find { it.msg == 'OrderId is mandatory in query parameter' }
    final Result missingMandatoryElement = this.find { it.msg == 'Missing mandatory element' }
    final Result midAndReferenceIdMandatoryInQueryParams = this.find { it.msg == 'mid and referenceId are mandatory in query parameter' }
    final Result tokenValidationFailed = this['1002']
    final Result invalidBinNo = this['1003']
    final Result ssoTokenIsInvalid = this['2004']
    final Result invalidChecksum = this['2005']
    final Result invalidMid = this['2006']
    final Result emiNotConfiguredOnMerchant = this['3004']
    final Result operationNotSupported = this['2012']
    final Result midPassedInQueryParamsAndRequestBodyDoesNotMatch = this.find { it.msg == MID_PASSED_IN_QUERY_PARAMS_AND_REQUEST_BODY_DOES_NOT_MATCH }
    final Result somethingWentWrong = this.find { it.msg == SOMETHING_WENT_WRONG }
    final Result noPromoCodeActive = this.find { it.msg == NO_PROMO_CODE_ACTIVE }
    final Result custIdCantBeBlankForTokenTypeChecksum = this.find { it.msg == CUST_ID_CANT_BE_BLANK_FOR_TOKEN_TYPE_CHECKSUM }
    final Result systemErrorInvalidParam = this.find { it.msg == SYSTEM_ERROR_INVALID_PARAM }
    final Result couldNotVerifyVpa = this.find { it.msg == WE_COULD_NOT_VERIFY_VPA }
    final Result unsuccessfulPaymentRequestTryAgainLater = this.find { it.msg == UNSUCCESSFUL_PAYMENT_REQUEST_TRY_AGAIN_LATER }
    final Result unsuccessfulPaymentRequestTryAgain = this.find { it.msg == UNSUCCESSFUL_PAYMENT_REQUEST_TRY_AGAIN }
    final Result invalidUpiAddress = this.find { it.msg == INVALID_UPI_ADDRESS }
    final Result invalidUpiId = this.find { it.msg == INVALID_UPI_ID }
    final Result couldNotVerifyUpiIdTryAgainLater = this.find {it.msg == WE_COULD_NOT_VERIFY_UPI_ID_TRY_AGAIN_LATER }

    Result getAt(String code) {
        this.find { it.code == code }
    }

    @Override
    Iterator<Result> iterator() {
        new Iterator<Result>() {
            private List<Result> list = [
                    ['0', 'F', SYSTEM_ERROR_INVALID_PARAM],
                    ['0000', 'S', 'Success'],
                    ['0001', 'F', UNSUCCESSFUL_PAYMENT_REQUEST_TRY_AGAIN_LATER],
                    ['0001', 'F', UNSUCCESSFUL_PAYMENT_REQUEST_TRY_AGAIN],
                    ['0001', 'F', INVALID_UPI_ADDRESS],
                    ['0001', 'F', INVALID_UPI_ID],
                    ['1001', 'F', 'Request prameters are not valid'],
                    ['1002', 'F', 'Token validation failed'],
                    ['1003', 'F', "Bin number is not valid"],
                    ['403', 'F', 'Invalid SSO Token'],
                    ['00000900', 'U', 'System error'],
                    ['QR_1018', 'F', 'This QR Code cannot be recognised.'],
                    ['335', 'F', 'Invalid merchant Id.'],
                    ['330', 'F', 'Paytm checksum mismatch.'],
                    ['501', 'F', WE_COULD_NOT_VERIFY_VPA],
                    ['501', 'F', WE_COULD_NOT_VERIFY_UPI_ID_TRY_AGAIN_LATER],
                    ['601', 'F', 'Invalid A/c Number or IFSC Code.'],
                    ['602', 'F', 'Invalid VPA.'],
                    ['2014', 'F', "OrderId in the query param doesn't match with the OrderId send in the request"],
                    ['2012', 'F', "Operation is not supported"],
                    ['2013', 'F', "Mid in the query param doesn't match with the Mid send in the request"],
                    ['1006', 'F', "Your Session has expired."],
                    ['1007', 'F', "Mid and OrderId are mandatory in query parameter"],
                    ['1007', 'F', "Mid is mandatory in query parameter"],
                    ['1007', 'F', "OrderId is mandatory in query parameter"],
                    ['1007', 'F', "Missing mandatory element"],
                    ['1007', 'F', "mid and referenceId are mandatory in query parameter"],
                    ['1007', 'F', MID_PASSED_IN_QUERY_PARAMS_AND_REQUEST_BODY_DOES_NOT_MATCH],
                    ['1012', 'F', NO_PROMO_CODE_ACTIVE],
                    ['2004', 'F', "SSO Token is invalid"],
                    ['2005', 'F', "Checksum provided is invalid"],
                    ['2006', 'F', "Mid is invalid"],
                    ['3004', 'F', "EMI not configured on merchant"],
                    ['9999', 'F', SOMETHING_WENT_WRONG],
                    ['9999', 'F', CUST_ID_CANT_BE_BLANK_FOR_TOKEN_TYPE_CHECKSUM],
            ].collect { new Result(it[0], it[1], it[2]) }
            private int index = 0

            @Override
            boolean hasNext() {
                list.size() > index
            }

            @Override
            Result next() {
                list[index++]
            }
        }
    }
}
