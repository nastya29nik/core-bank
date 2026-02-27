package com.niknastacy.corebank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountProperties {
    @Value("${account.default-amount}")
    private BigDecimal amount;

    @Value("${account.transfer-commission}")
    private BigDecimal transferCommission;

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getTransferCommission() {
        return transferCommission;
    }
}
