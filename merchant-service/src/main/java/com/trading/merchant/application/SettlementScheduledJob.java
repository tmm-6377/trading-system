package com.trading.merchant.application;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "business.settlement.enabled", havingValue = "true", matchIfMissing = true)
public class SettlementScheduledJob {
    private final SettlementApplicationService settlementApplicationService;

    @Scheduled(cron = "${business.settlement.cron:0 0 2 * * ?}")
    public void execute() {
        settlementApplicationService.executeSettlement(LocalDate.now().minusDays(1));
    }
}
