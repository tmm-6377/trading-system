package com.trading.merchant.application;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 商家结算定时任务。
 *
 * <p>该任务用于按日触发商家结算流程，默认读取前一天的业务数据执行对账。
 * 当配置项 {@code business.settlement.enabled=true} 时任务生效；若未配置该项，也会默认开启。</p>
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "business.settlement.enabled", havingValue = "true", matchIfMissing = true)
public class SettlementScheduledJob {
    private final SettlementApplicationService settlementApplicationService;

    /**
     * 执行每日结算任务。
     *
     * <p>定时表达式默认值为 {@code 0 0 2 * * ?}，表示每天凌晨 2 点 0 分 0 秒触发一次。
     * 任务执行时会对前一自然日的数据进行结算，避免当天业务数据尚未完全入账造成统计偏差。</p>
     */
    @Scheduled(cron = "${business.settlement.cron:0 0 2 * * ?}")
    public void execute() {
        settlementApplicationService.executeSettlement(LocalDate.now().minusDays(1));
    }
}
