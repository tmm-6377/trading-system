package com.trading.merchant.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettlementRecordTest {

    @Test
    void testCreate_Match() {
        SettlementRecord record = SettlementRecord.create(
            "merchant001",
            LocalDate.of(2026, 6, 8),
            Money.of(new BigDecimal("100.00")),
            Money.of(new BigDecimal("100.00"))
        );

        assertEquals("MATCH", record.getStatus());
    }

    @Test
    void testCreate_Mismatch() {
        SettlementRecord record = SettlementRecord.create(
            "merchant001",
            LocalDate.of(2026, 6, 8),
            Money.of(new BigDecimal("100.00")),
            Money.of(new BigDecimal("80.00"))
        );

        assertEquals("MISMATCH", record.getStatus());
    }

    @Test
    void testMatchRemark() {
        SettlementRecord record = SettlementRecord.create(
            "merchant001",
            LocalDate.of(2026, 6, 8),
            Money.of(new BigDecimal("100.00")),
            Money.of(new BigDecimal("100.00"))
        );

        assertEquals("结算正常", record.getRemark());
    }

    @Test
    void testMismatchRemark() {
        SettlementRecord record = SettlementRecord.create(
            "merchant001",
            LocalDate.of(2026, 6, 8),
            Money.of(new BigDecimal("100.00")),
            Money.of(new BigDecimal("99.00"))
        );

        assertEquals("结算差异，请人工核对", record.getRemark());
    }
}
