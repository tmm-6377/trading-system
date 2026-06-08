package com.trading.merchant.domain.model;

import com.trading.common.exception.BusinessException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SKU {
    private final String value;

    private SKU(String value) {
        this.value = value;
    }

    public static SKU of(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("INVALID_SKU", "SKU不能为空");
        }
        return new SKU(value);
    }
}
