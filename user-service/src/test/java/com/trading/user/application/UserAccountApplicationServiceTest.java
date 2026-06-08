package com.trading.user.application;

import com.trading.common.exception.BusinessException;
import com.trading.common.exception.NotFoundException;
import com.trading.user.application.dto.AccountDTO;
import com.trading.user.application.dto.RechargeCommand;
import com.trading.user.domain.model.Money;
import com.trading.user.domain.model.UserAccount;
import com.trading.user.domain.repository.AccountTransactionRepository;
import com.trading.user.domain.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountApplicationServiceTest {
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private AccountTransactionRepository accountTransactionRepository;
    @InjectMocks
    private UserAccountApplicationService userAccountApplicationService;

    @Test
    void testRecharge_Success() {
        // Given
        UserAccount account = UserAccount.create("user001");
        when(userAccountRepository.findByUserId("user001")).thenReturn(Optional.of(account));
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AccountDTO result = userAccountApplicationService.recharge(new RechargeCommand("user001", new BigDecimal("100.00")));

        // Then
        assertEquals("user001", result.getUserId());
        assertEquals(new BigDecimal("100.00"), result.getBalance());
        verify(userAccountRepository).save(any(UserAccount.class));
        verify(accountTransactionRepository).save(any());
    }

    @Test
    void testRecharge_NewUser() {
        // Given
        when(userAccountRepository.findByUserId("user002")).thenReturn(Optional.empty());
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AccountDTO result = userAccountApplicationService.recharge(new RechargeCommand("user002", new BigDecimal("88.80")));

        // Then
        assertEquals("user002", result.getUserId());
        assertEquals(new BigDecimal("88.80"), result.getBalance());
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void testRecharge_InvalidAmount() {
        // Given
        when(userAccountRepository.findByUserId("user003")).thenReturn(Optional.empty());

        // When
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userAccountApplicationService.recharge(new RechargeCommand("user003", new BigDecimal("-1.00"))));

        // Then
        assertEquals("INVALID_AMOUNT", exception.getCode());
        verify(userAccountRepository, never()).save(any(UserAccount.class));
        verify(accountTransactionRepository, never()).save(any());
    }

    @Test
    void testGetAccount_Success() {
        // Given
        UserAccount account = UserAccount.builder()
            .userId("user001")
            .balance(Money.of(new BigDecimal("66.60")))
            .version(1)
            .build();
        when(userAccountRepository.findByUserId("user001")).thenReturn(Optional.of(account));

        // When
        AccountDTO result = userAccountApplicationService.getAccount("user001");

        // Then
        assertEquals("user001", result.getUserId());
        assertEquals(new BigDecimal("66.60"), result.getBalance());
    }

    @Test
    void testGetAccount_NotFound() {
        // Given
        when(userAccountRepository.findByUserId("missing-user")).thenReturn(Optional.empty());

        // When
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> userAccountApplicationService.getAccount("missing-user"));

        // Then
        assertEquals("NOT_FOUND", exception.getCode());
    }

    @Test
    void testGetAccount_MultipleAccess() {
        // Given
        UserAccount account = UserAccount.builder()
            .userId("user001")
            .balance(Money.of(new BigDecimal("50.00")))
            .version(1)
            .build();
        when(userAccountRepository.findByUserId("user001")).thenReturn(Optional.of(account));

        // When
        AccountDTO first = userAccountApplicationService.getAccount("user001");
        AccountDTO second = userAccountApplicationService.getAccount("user001");

        // Then
        assertEquals(new BigDecimal("50.00"), first.getBalance());
        assertEquals(new BigDecimal("50.00"), second.getBalance());
        verify(userAccountRepository, times(2)).findByUserId("user001");
    }
}
