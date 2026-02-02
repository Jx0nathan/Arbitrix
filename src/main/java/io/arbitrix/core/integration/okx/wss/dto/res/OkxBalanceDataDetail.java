package io.arbitrix.core.integration.okx.wss.dto.res;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OkxBalanceDataDetail {

    private String availBal;
    private String availEq;
    private String borrowFroz;
    private String cashBal;
    private String ccy;
    private String coinUsdPrice;
    private String crossLiab;
    private String disEq;
    private String eq;
    private String eqUsd;
    private String fixedBal;
    private String frozenBal;
    private String interest;
    private String isoEq;
    private String isoLiab;
    private String isoUpl;
    private String liab;
    private String maxLoan;
    private String mgnRatio;
    private String notionalLever;
    private String ordFrozen;
    private String spotInUseAmt;
    private String spotIsoBal;
    private String stgyEq;
    private String twap;
    private String uTime;
    private String upl;
    private String uplLiab;
}
