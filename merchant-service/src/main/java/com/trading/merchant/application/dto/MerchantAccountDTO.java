package com.trading.merchant.application.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MerchantAccountDTO {
    String merchantId;
    BigDecimal balance;
}
