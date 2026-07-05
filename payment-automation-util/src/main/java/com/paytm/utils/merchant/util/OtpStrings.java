package com.paytm.utils.merchant.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.ServerUtil;
import com.paytm.utils.merchant.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public enum OtpStrings {

    //LOGIN_OTP("cd /paytm/logs; tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send | grep --line-buffered \"as your login OTP\" | awk -F 'content\": \"Use | as your login OTP.' '{if(NF > 1) print $2}' | tail -1"),
    LOGIN_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send | grep --line-buffered \"OTP is confidential\" | awk -F'\"content\": \"OTP is confidential. Sharing it with anyone will give them full access to your Paytm Wallet. The OTP for login is  | . Paytm never calls to verify your OTP.' '{if(NF > 1) print $2}' | tail -1"),
    Login_Signup_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send | grep --line-buffered \"Paytm never calls to verify your OTP.\" | awk -F '\"content\": \"OTP is confidential. Sharing it with anyone will give them full access to your Paytm Wallet. The OTP for login is  | . Paytm never calls to verify your OTP.' '{if(NF > 1) print $2}' | tail -1"),
    KYC_verification_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send  | grep --line-buffered  \"isyourPaytmCode.PleaseenterthiscodetoconfirmyouracceptanceoftheTerms&Conditionsavailableonouragent\" | awk -F'\"content\":\"|isyourPaytmCode.PleaseenterthiscodetoconfirmyouracceptanceoftheTerms&Conditionsavailableonouragent' '{if(NF > 1) print $2}' | tail -1"),
    Offline_payment_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send | grep --line-buffered  \"is your One Time Password to complete payment using Paytm Wallet on our partner merchant.\" | awk -F 'content\\\": \\\"| is your One Time Password' '{if(NF > 1) print $2}' | tail -1"),
    Signup_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send | grep --line-buffered  isyourverificationcodetoverifyyour | awk -F'\"content\":\"|isyourverificationcodetoverifyyour' '{if(NF > 1) print $2}' | tail -1"),
    Current_phone_change_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send  | grep --line-buffered  isyourOTPforMobileNo. | awk -F'\"content\":\"|isyourOTPforMobileNo.' '{if(NF > 1) print $2}' | tail -1"),
    P2P_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send  | grep --line-buffered  isyourOneTimePassword.DonotsharethisOTPwithanyone. | awk -F'\"content\":\"|isyourOneTimePassword.DonotsharethisOTPwithanyone.' '{if(NF > 1) print $2}' | tail -1"),
    OTP_on_phone_post_Phone_merge("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send  | grep --line-buffered \"isyourOTP(OneTimePassword)tochangeyouremailIDto\" | awk -F'\"content\":\"|isyourOTP' '{if(NF > 1) print $2}' | tail -1"),
    DEVICE_P2P_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send  | grep --line-buffered isyourOTPtoverifyyourdeviceonPaytm | awk -F'\"content\":\"|isyourOTPtoverifyyourdeviceonPaytm' '{if(NF > 1) print $2}' | tail -1"),
    Google_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send  | awk -F'\"content\":\"Use|asyourloginOTP.YourOTPisconfidential.PaytmnevercallsyouaskingforOTP.SharingitwithanyonegivesthemfullaccesstoyourPaytmWallet' '{if(NF > 1) print $2}' | tail -1"),
    LOGIN_MerchantUber_OTP("cd /paytm/logs; tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send | grep --line-buffered \"Your Uber_Login OTP\"  | awk -F 'content\\\": \\\"Your Uber_Login OTP is |' '{if(NF > 1) print $2}' | awk -F \".\" '{print$1}' | tail -1"),
    SetUserDefinedLimit_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send  | grep --line-buffered hasbeensenttoyourmobile,pleaseenterthesameheretologin |awk -F 'hasbeensenttoyourmobile,pleaseenterthesameheretologin.|\",\"priority' '{if(NF > 1) print $2}' | tail -1"),
    SignUp_MerchantUber_OTP("cd /paytm/logs; tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send | grep --line-buffered YourUbersignupOTPis | awk -F 'content\":\"YourUbersignupOTPis|\",\"priority' '{if(NF > 1) print $2}' | tail -1"),
    LINK_OTP("tail -1000 /paytm/logs/pgproxy-notification.log | grep --line-buffered ValueToSubstitue | grep --line-buffered alipayplus.communication.sms.send |grep  --line-buffered \"Beware of fraudulent calls.\". | awk -F 'linkpayment, OTP is | .\",' '{if(NF > 1) print $2}' | tail -1 ");

    private String key;

    OtpStrings(String key) {
        this.key = key;
    }

    public String getOtp(String phone) {
        String otp = null;
        int retryCount = 3;
        while (retryCount > 0) {
            try {
                Thread.sleep(4000);
                ServerUtil serverUtil = new ServerUtil();
                Session session = serverUtil.getSession(Constants.NOTIFICATION_CONNECTION_URL);
                ChannelExec channel = (ChannelExec) session.openChannel("exec");
                phone = phone.replace("+91", "");
                String newKey = this.key.replace("ValueToSubstitue", phone);
                System.out.println("Command is " + newKey);
                channel.setCommand(newKey);
                Reporter.report.info(newKey);
                channel.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
                otp = reader.readLine();
                channel.disconnect();
                session.disconnect();
                if (!(otp == null))
                    return otp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            retryCount--;
        }
        return null;
    }
}
