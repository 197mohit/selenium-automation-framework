package com.paytm.dto.NativeDTO.InitTxn;

public class RiskFeeDetails {

    private FeeAmount feeAmount;
    private InitialAmount initialAmount;


    public RiskFeeDetails()
    {

    }

    public FeeAmount getFeeAmount()
    {
        return feeAmount;
    }

    public RiskFeeDetails setFeeAmount(String feeAmount)
    {
        this.feeAmount = new FeeAmount(feeAmount);
        return this;

    }

    public InitialAmount getInitialAmount()
    {
        return initialAmount;
    }

    public RiskFeeDetails setInitialAmount(String initialAmount)
    {
        this.initialAmount = new InitialAmount(initialAmount);
        return this;
    }

}
