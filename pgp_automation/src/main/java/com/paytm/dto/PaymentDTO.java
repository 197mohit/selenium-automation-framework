package com.paytm.dto;



import java.time.Year;
import java.util.Random;

public class PaymentDTO {
    public static final String VISA_ENCRYPTED_CARD_DATA = "ROsdpkpEGF6+DaEhf9JU+GBLC+ApJAGsth/4KbXjqG9L2N3cqog4kBLE7CcbyRdqd2EoqoCXtDNA8LVJMt4lackeK9IWonhTXWlqKgurhIZGdQBURJfpXCDL2hhvNBTbZBjkhuhO35A1IzppkNSSQ7LH3uMVhQdu/x7u8k5DoeYWFkrjEFeXRy2erkiNT0vaYOnyMu7Qq3GHFF1yeCYdZO6TfKQLlEwOq0M6iIGq6rVrjy3Jm1z6XYXthUqgxexO/2ejNl6gRFpKcwGFMKLZCFKi9z89WCP79MBii2FHqm1RpXiASopqIpaNJNsG04l5B4Gtqk/murFOQSJY1cDlig==";
    public static final String VISA_COFT_CARD = "4639170013374311";
    public static final String MASTER_ENCRYPTED_CARD_DATA = "EKMa2lhPuGeYSSBU92vzv1/gNIL27oin5cWRYyzopThHF6CLDoYzYzLbprZfCb2mq568yUFJaTNfR+EUS//FDocFCQSJON/7EB7wqnG6lxYcrgMOKzKYKh0HR9fLCvuPA7VEsBPMlUq88315owdzcbZKckyDKUR0YDXaQb4/HKT2/GIfusaB6ZV1TgG2YZNWMmYvpcS8xfYYCC7TkzBmHLBKPTe4sF6igQytEhFCAJkBbXBK/gIh3zJDZOl15fuA3rmoWSF82i4MoSNuOzYmxrb9Zhn2c5/GuvCVwp/OdibP3d69hEmb54+ly9VIBvv9psW8ixEUgYbWykX41xyhOw==";
    public static final String MASTER_ICICI_DEBIT_CARD_NUMBER = "5166400031031058";
    public static final String ICICI_DEBIT_CARD_EMI="4799320857008816";
    public static final String ICICI_CORPORATE_DEBIT_CARD_NUMBER = "4731765131526259";
    public static final String ICICI_DEBIT_CARD_NUMBER = "5244519765781731";
    public static final String ICICI_DEBIT_CARD_NUMBER_EMI = "4572741654006328";
    public static final String MAESTRO_DEBIT_CARD_NUMBER = "6799990100000000019";
    public static final String AMEX_CARD_NUMBER = "379863297651006";
    public static final String VISA_COFT_CARD_NUMBER = "4761360075860436";
    public static final String HDFC_GUEST_CHECKOUT_ISSUER_CARD="4718650100010336";
    public static final String AXIS_GUEST_CHECKOUT_ISSUER_CARD="4700110100025677";
    public static final String ICICI_GUEST_CHECKOUT_ISSUER_CARD="4375512441465005";
    public static final String VISA_COFT_CARD_LAST_FOUR_NUMBER = "0436";
    public static final String RUPAY_CARD_NUMBER = "6073180505920479";
    public static final String DINERS_CARD_NUMBER = "30569309025904";
    public static final String DINERS_CC_CARD_NUMBER = "3612153445238802";
    public static final String DINERS_EMI_CARD_NUBMER="71103301877466";
    public static final String INVALID_CARD =  "607318050592047";
    public static final String INVALID_CARD2 =  "6789098765456789";
    public static final String LOW_SUCCESS_RATE_CARD_NUMBER =  "379863297651006";
    public static final String DEBIT_CARD_NUMBER = "4444333322221111";
    public static final String SBI_DEBIT_CARD = "4592000037105380";
    public static final String PNB_DEBIT_CARD = "5126520060428682";
    public static final String INTERNATIONAL_CARD =  "4150260989515663";
    public static final String INTERNATIONAL_CARD_1 = "4639170013374311";
    public static final String MAESTROCARD_1 = "6130336678087006";
    public static final String MAESTROCARD_2="4565455347600624";
    public static final String DINERS_PRIVILEGE_CONSUMER_HDFC_BIN = "522023";
    public static final String DINERS_BLACK_PREMIUM_HDFC_BIN = "418219";
    public static final String PLATINUM_TIMES_SIGNATURE_HDFC_BIN = "418235";
    public static final String REGALIA_SIGNATURE_ICICI_BIN = "430367";
    public static final String EDEN_PREMIUM_ICICI_BIN = "536012";
    public static final String DINERS_PRIVILEGE_CONSUMER_CARD = "5220239753805750";
    public static final String DINERS_BLACK_PREMIUM_HDFC_CARD = "4182199280328545";
    public static final String PLATINUM_TIMES_SIGNATURE_HDFC_CARD = "4182352069650866";
    public static final String REGALIA_SIGNATURE_ICICI_CARD = "4303675641289702";
    public static final String EDEN_PREMIUM_ICICI_CARD = "5360129895670701";

    public static final String INTERNATIONAL_ICICI_CREDIT_CARD = "5555525280834775";
    //"4854980000514895", "4854980604790867", "5459649100225134" ,"4718650100010336"- CC with Banks as ICICI instead of HDFC
    public static final String INTERNATIONAL_AXIS_CREDIT_CARD = "4000000000001091";
    public static final String INTERNATIONAL_IAXI_CREDIT_CARD = "4000000000002503";
    public static final String MCC_INTERNATIONAL_CREDIT_CARD = "4005520000000129";

    public static final String BAJAJFN_CARDLESS_CARD = "2030400550155313";
    public static final String BAJAJFN_CARDLESS_CARD2 = "2030400333666222";

    public static final String EXP_MONTH = "10";
    public static final String EXP_YEAR = "2035";

    public static final String COFT_DECLINE_VISA_CARD = "4988438843884305";
    public static final String COFT_DECLINE_VISA_EXP_MONTH = "11";
    public static final String COFT_DECLINE_VISA_EXP_YEAR = "2023";
    public static final String COFT_VISA_CVV = "123";

    public static final String COFT_TRCONFIG_ISSUE_VISA_CARD = "4001590000000001";
    public static final String COFT_NOTALLOWED_VISA_CARD = "4293189100000008";
    public static final String COFT_INVALIDREQUEST_VISA = "4484600000000004";
    public static final String COFT_CARD_NOT_ALLOWED_VISA = "4166676667666746";
    public static final String COFT_FAILEDNETWORK_VISA ="4646464646464644";
    public static final String COFT_INVALIDPARAMETER_VISA = "4000620000000007";
    public static final String COFT_VISA_MONTH_EXPIRY = "11";
    public static final String COFT_VISA_YEAR_EXPIRY = "2030";
    public static final String LITE_RISKEXTENDEDINFO = "scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|isRooted:false|displayName:qa11SS41915387889728|mode:qrBackEnd|wifi:|mode:qrBackEnd|userLBSLatitude:12.9290586|userLBSLongitude:77.5399188|CHANNEL_ID:QRCODE|terminalType:APP|deviceId:8deddae0d3c66352|appVersion:10.28.0|versionCode:721184|osType:Android|phoneModel:Pixel+4a|IMEI:null|deviceManufacturer:Google|deviceLanguage:en|timeZone:GMT+05:30|routerMac:null|clientIp:192.168.1.3|productCode:null|isOfflineMerchant:true|isOnlineMerchant:false|operationOrigin:consumer app|paymentFlow:|operationType:PAYMENT|requestType:QR dynamic|fuzzyDeviceId:8deddae0d3c66352|screenResolution:1080x2340|isGalleryScan:false";
    public static final String LITE_CREDITBLOCK = "{\\\"accRefId\\\":\\\"236077\\\",\\\"accountType\\\":\\\"SAVINGS\\\",\\\"bank\\\":\\\"MYPSP2\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABF0008032\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX4872\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"Venkat\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"7259493013@paytm\\\",\\\"primary\\\":true}}";
    public static final String LITE_CREDITBLOCKQR = "{\"CredsAllowedDLength\":\"2048\",\"CredsAllowedDType\":\"ALPH\",\"CredsAllowedSubType\":\"SIGNATURE\",\"CredsAllowedType\":\"ARQC\"}";
    public static final String CC_MPIN = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
    public static final String CC_RISKEXTENDEDINFO = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
    public static final String CC_CREDITBLOCK = "{\\\\accRefId\\\\:\\\\238692\\\\,\\\\accountType\\\\:\\\\CREDIT\\\\,\\\\bank\\\\:\\\\MyBene\\\\,\\\\bankLogoUrl\\\\:\\\\https://static.paytmbank.com/upi/images/bank-logo/000000.png\\\\,\\\\credsAllowed\\\\:[{\\\\CredsAllowedDLength\\\\:\\\\6\\\\,\\\\CredsAllowedDType\\\\:\\\\Numeric\\\\,\\\\CredsAllowedSubType\\\\:\\\\SMS\\\\,\\\\CredsAllowedType\\\\:\\\\OTP\\\\,\\\\dLength\\\\:\\\\6\\\\},{\\\\CredsAllowedDLength\\\\:\\\\6\\\\,\\\\CredsAllowedDType\\\\:\\\\Numeric\\\\,\\\\CredsAllowedSubType\\\\:\\\\MPIN\\\\,\\\\CredsAllowedType\\\\:\\\\PIN\\\\,\\\\dLength\\\\:\\\\6\\\\},{\\\\CredsAllowedDLength\\\\:\\\\6\\\\,\\\\CredsAllowedDType\\\\:\\\\Numeric\\\\,\\\\CredsAllowedSubType\\\\:\\\\ATMPIN\\\\,\\\\CredsAllowedType\\\\:\\\\PIN\\\\,\\\\dLength\\\\:\\\\6\\\\}],\\\\ifsc\\\\:\\\\AABE0877543\\\\,\\\\maskedAccountNumber\\\\:\\\\857675XXXXX99\\\\,\\\\mpinSet\\\\:\\\\Y\\\\,\\\\name\\\\:\\\\ABC\\\\,\\\\pgBankCode\\\\:\\\\CON3\\\\,\\\\txnAllowed\\\\:\\\\ALL\\\\,\\\\vpaDetail\\\\:{\\\\defaultCreditAccRefId\\\\:\\\\242393\\\\,\\\\defaultDebitAccRefId\\\\:\\\\242393\\\\,\\\\name\\\\:\\\\7259493013@paytm\\\\,\\\\primary\\\\:true}}";
    public static final String EMI_CC = "4761360075860592";
    public static final String AlternateID_VISA_CARD ="4895380115392363";
    public static final String AlternateID_VISA_CARD_CVV="545";
    public static final String AlternateID_VISA_CARD_ExpiryMonth="12";
    public static final String AlternateID_VISA_CARD_ExpiryYear="2029";

    public static final String AlternateID_RUPAY_CARD ="6080410000000001";
    public static final String COFT_RUPAY_TOKEN ="6080410004000195";
    public static final String COFT_VISA_TOKEN ="4718650100000195";
    public static final String AlternateID_RUPAY_CARD_CVV="123";
    public static final String AlternateID_RUPAY_CARD_ExpiryMonth="12";
    public static final String AlternateID_RUPAY_CARD_ExpiryYear="2028";

    public static final String ISSUER_TOKENIZATION_VISA_CARD = "4761360075860519";
    public static final String Tokenization_Year = String.valueOf(Year.now().getValue()+1);

    String[] list = {"4718650100010336", "4761360075860428", "4761360075860436", "4761360075860444", "4761360075860451",
            "4761360075860469", "4761360075860477", "4761360075860485", "4761360075860493", "4761360075860501", "4761360075860519",
            "4761360075860527", "4761360075860535", "4761360075860543", "4761360075860550", "4761360075860568", "4761360075860576",
            "4761360075860584", "4761360075860592", "4761360075860600", "4761360075860618", "4761360075860626", "4761360075860634",
            "4761360075860642", "4761360075860659", "4761360075860667", "4761360075860675", "4761360075860683", "4761360075860691",
            "4761360075860709", "4761360075860717", "4761360075860725", "4761360075860733", "4761360075860741"}; //4893771000362085

    Random r = new Random();//4799479867216601
    private String creditCardNumber = list[r.nextInt(list.length)];

    //TODO replace this debit card no. with valid number as this no. is of credit card
    private String debitCardNumber = "4444333322221111";
    private String payMethodType="CREDIT_CARD";
    private String expMonth = "12";
    private String expYear = "2035";
    private String cvvNumber = "618";
    private String securePass3D = "indu@123";
    private String MMID = "1111111";
    private String emiCard = "4718650100010336";
    public static final String OTP = "888888";
    public static final String FAILED_OTP = "808080";
    private String bankName = "HDFC";
    private String vpa = "9999661503@paytm";
    public int savedVpaIndex = 1;
    private String hdfcVPA="arsh.test2@paytm";
    public static final String bankOtp = "123456";
    private String savedCardId;
    private int month = 6;
    public static final String AMEX_CREDIT_CARD_NUMBER_A ="379863297651006";
    public static final String AMEX_CREDIT_CARD_NUMBER_B="370295061673669";

    public static final String ICICI_CREDIT_CARD_NUMBER="4375512441465005";
    public static final String MASTER_CREDIT_CARD = "5452260891234678";
    private String passcode = "1234";
    public static final String CREDIT_CARD_FOR_FAILED_TXN = "4718650100030131";
    public static final String DEBIT_CARD_FOR_FAILED_TXN = "4444333322201111";
    public static final String BAJAJ_FINSERV_CREDIT_CARD_NUMBER = "2030400200341578";
    public static final String BAJAJ_FINSERV_CREDIT_CARD_NUMBER1 = "2030400291909002";
    public static final String MASTERCARD_CC_BILL_PAYMENT = "5450650000000295";
    public static final String VISA_CC_BILL_PAYMENT = "4012888888881881";
    public static final String INVALID_MASTER_CARD_CC_BILL_PAYMENT = "2223520010000012";
    public static final String ICICI_CC_CARD = "4375512441465005";
    public static final String PROMO_CC_CARD_ICICI="4386280025337100";
    public static final String PROMO_CC_CARD_HDFC="4718650100010336";
    public static final String DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME = "4799475263852080";
    public static final String ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME = "4018060581050735";
    public static final String INTERNATIONAL_ICMC_CREDIT_CARD = "4099000000001960";

    public static final String Failedvpa = "fail@mypsp";
    private String promoCode = "";
    private String promoDesc = "";
    public static final String DC_FAILED_TXN = "4799475263852080";
    public static final String PREPAID_CARD= "4766413897814514"; //On PGPDB as well as P+ side
    public static final String CORPORATE_PREPAID_CARD= "5419190102188516";
    public static final String CORPORATE_INTERNATIONAL_PREPAID_CARD= "5105105105105100";
    public static final String CORPORATE_INDIAN_DC = "5346800022235225";
    public static final String CORPORATE_INDIAN_CC = "4166464311356935";
    public static final String DC="5455330760000018"; //|882|052026
    public static final String promoCC = "4718650100010336";
    private String mandateAuthMode ="Net Banking";
    private String savedBankMandateAccount ="PPBL";
    public static final String VISA_CREDIT_CARD_NUMBER = "4375512441465005";

    public static final String VISA_HDFC_EMI_CREDIT_CARD_NUMBER="4718650100010336";

    public static final String BLOCKED_BIN_NO="998877";
    public static final String INVALID_9DIGIT_BIN_NO="404276890";
    
    public static final String PASS_VPA = "pass@paytm";
    public static final String subsBinNumberCC = "524373";
    public static final String subsBinNumberDC = "512967";
    public static final String CARDTOKEN = "5506900490971255";
    public static final String Tavv = "AHzk1bo/KGvjAAIYHTetAAADFA==";
    public static final String CARDSUFFIX = "0008";
    public static final String PAR = "50017XX7IDPG2J0XSJDRR6IOYSW1F";
    public static final String TOKENMONTHEXPIRY = "072029";
    public static final String BOTHEXPIRY = "031996";
    public static final String TOKENEXPIRY = "082029";
    public static final String COFTTHEIARISKINTBIN = "550690";
    public static final String COFTTHEIARISKINTCIN = "20211116435000fa05ac0f8504e12ac743a5dc40ff9e5";
    public static final String COBRANDED_CC="4558869980270074";
    public static final String NONCOBRANDED_CC="4639174896320839";
    public static final String COBRANDED_DC="4092627830820230";
    public static final String NONCOBRANDED_DC="4444333322221111";
    public static final String ISSUER_ICICI_CC="4776581291547696";
    public static final String ISSUER_HDFC_CC="5019390442207976";
    public String getSubsBinNumberCC(){ return subsBinNumberCC;}
    public String getSubsBinNumberDC(){ return subsBinNumberDC;}

    public String getDC(){return DC;}

    public static String getPromoCC() {
        return promoCC;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public String getInternationalCreditCardNumber() {
        return INTERNATIONAL_AXIS_CREDIT_CARD;
    }


    public PaymentDTO setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
        return this;
    }

    public String getDebitCardNumber() {
        return debitCardNumber;
    }

    public PaymentDTO setDebitCardNumber(String debitCardNumber) {
        this.debitCardNumber = debitCardNumber;
        return this;
    }

    public String getPaymentType() {
        return payMethodType;
    }

    public PaymentDTO setPaymentType(String payMethodType) {
        this.payMethodType = payMethodType;
        return this;
    }


    public static String getAmexCardNumber() {
        return AMEX_CARD_NUMBER;
    }

    public String getExpMonth() {
        return expMonth;
    }

    public PaymentDTO setExpMonth(String expMonth) {
        this.expMonth = expMonth;
        return this;
    }

    public String getExpYear() {
        return expYear;
    }

    public PaymentDTO setExpYear(String expYear) {
        this.expYear = expYear;
        return this;
    }

    public String getCvvNumber() {
        return cvvNumber;
    }

    public PaymentDTO setCvvNumber(String cvvNumber) {
        this.cvvNumber = cvvNumber;
        return this;
    }

    public String getSecurePass3D() {
        return securePass3D;
    }

    public PaymentDTO setSecurePass3D(String securePass3D) {
        this.securePass3D = securePass3D;
        return this;
    }

    public String getMMID() {
        return MMID;
    }

    public PaymentDTO setMMID(String MMID) {
        this.MMID = MMID;
        return this;
    }

    public String getBankName() {
        return bankName;
    }

    public PaymentDTO setBankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public String getVpa() {
        return vpa;
    }

    public PaymentDTO setVpa(String vpa) {
        this.vpa =vpa;
        return this;
    }

    public int getMonth() {
        return month;
    }

    public PaymentDTO setMonth(int month) {
        this.month = month;
        return this;
    }

    public String getSavedCardId() {
        return savedCardId;
    }

    public PaymentDTO setSavedCardId(String savedCardId) {
        this.savedCardId = savedCardId;
        return this;
    }

    public String getPasscode() {
        return passcode;
    }

    public PaymentDTO setPasscode(String passcode) {
       this.passcode = passcode;
        return this;
    }

    public String getPromoCode()
    {return promoCode;}

    public PaymentDTO setPromoCode(String promoCode)
    {
     this.promoCode = promoCode;
     return this;
    }


    public String getPromoDesc()
    {return promoDesc;}

    public PaymentDTO setPromoDesc(String promoDesc)
    {
        this.promoDesc = promoDesc;
        return this;
    }

    public String getMandateAuthMode()
    {return mandateAuthMode;}

    public PaymentDTO setMandateAuthMode(String AuthMode)
    {
        this.mandateAuthMode = AuthMode;
        return this;
    }


    public String getSavedBankMandateAccount()
    {return savedBankMandateAccount;}

    public PaymentDTO setSavedBankMandateAccount(String savedBankMandateAccount)
    {
        this.savedBankMandateAccount = savedBankMandateAccount;
        return this;
    }

    public String getEmiCard() {
        return emiCard;
    }

    public PaymentDTO setEmiCard(String emiCard) {
        this.emiCard = emiCard;
        return this;
    }

    @Override
    public String toString() {
        return "PaymentDTO{" +
                "creditCardNumber='" + creditCardNumber + '\'' +
                ", debitCardNumber='" + debitCardNumber + '\'' +
                ", expMonth='" + expMonth + '\'' +
                ", expYear='" + expYear + '\'' +
                ", cvvNumber='" + cvvNumber + '\'' +
                ", securePass3D='" + securePass3D + '\'' +
                ", MMID='" + MMID + '\'' +
                ", OTP='" + OTP + '\'' +
                ", bankName='" + bankName + '\'' +
                ", vpa='" + vpa + '\'' +
                ", savedCardId='" + savedCardId + '\'' +
                ", month=" + month +
                ", passcode=" + passcode +
                '}';
    }

    public static final String isNativeOtpBlockedTRUE = "9999945672263449";
    public static final String isNativeOtpBlockedFALSE = "4012009999900029";
    public static final String PREPAID_CARD_HOTFIX="4870527017700692";

    public String getNativeOtpBinNumber(){ return isNativeOtpBlockedFALSE;}
    public String getNativeOtpBlockedBinNumber(){ return isNativeOtpBlockedTRUE;}

    public static final String[] cardSortingList = new String[]{"8894849440560615","8897746666453300","8894866853592836","9987665658410483","9837665679244509"};
    public static final String CCB_SUCCESS = "4537609697058354";
    public static final String CCB_FAILURE1 = "4306798152826857";
    public static final String CCB_FAILURE2 = "4816035010235762";
}
