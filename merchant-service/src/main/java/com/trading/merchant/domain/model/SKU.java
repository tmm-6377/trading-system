package com.trading.merchant.domain.model;

import com.trading.common.exception.BusinessException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 商品 SKU 值对象。
 *
 * <p>SKU 用于唯一标识商家下的具体商品规格。作为值对象，它通过不可变的字符串值承载业务标识，
 * 并在创建时完成非空与空白校验，防止非法 SKU 流入领域模型。</p>
 */
@Getter
@EqualsAndHashCode
public class SKU {
    private final String value;

    private SKU(String value) {
        this.value = value;
    }

    /**
     * 创建 SKU 值对象。
     *
     * <p>当传入值为 {@code null} 或仅包含空白字符时，方法会抛出业务异常，保证 SKU 始终具备可识别的业务含义。</p>
     *
     * @param value SKU 字符串值
     * @return SKU 值对象
     * @throws BusinessException 当 SKU 为空时抛出
     */
    public static SKU of(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("INVALID_SKU", "SKU不能为空");
        }
        return new SKU(value);
    }
}
