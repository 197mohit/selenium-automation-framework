package com.paytm.utils.merchant.dto.cachecardtoken.response;

public class Body {



    private String token;
    private ResultInfo resultInfo;

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token=token;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void setResultInfo(ResultInfo resultInfo) {
        this.resultInfo = resultInfo;
    }
}
