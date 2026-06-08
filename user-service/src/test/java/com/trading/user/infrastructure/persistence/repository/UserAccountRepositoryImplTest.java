package com.trading.user.infrastructure.persistence.repository;

import com.trading.common.exception.BusinessException;
import com.trading.user.domain.model.Money;
import com.trading.user.domain.model.UserAccount;
import com.trading.user.infrastructure.persistence.entity.UserAccountPO;
import com.trading.user.infrastructure.persistence.mapper.UserAccountMapper;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountRepositoryImplTest {
    @Mock
    private UserAccountMapper userAccountMapper;
    @InjectMocks
    private UserAccountRepositoryImpl userAccountRepository;

    @Test
    void testFindByUserId_Found() {
        // Given
        UserAccountPO po = new UserAccountPO();
        po.setUserId("user001");
        po.setBalance(new BigDecimal("100.00"));
        po.setVersion(2);
        when(userAccountMapper.selectById("user001")).thenReturn(po);

        // When
        Optional<UserAccount> result = userAccountRepository.findByUserId("user001");

        // Then
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("100.00"), result.orElseThrow().getBalance().getAmount());
    }

    @Test
    void testFindByUserId_NotFound() {
        // Given
        when(userAccountMapper.selectById("missing-user")).thenReturn(null);

        // When
        Optional<UserAccount> result = userAccountRepository.findByUserId("missing-user");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave_NewAccount() {
        // Given
        UserAccount account = UserAccount.create("user001");
        account.recharge(Money.of(new BigDecimal("50.00")));
        when(userAccountMapper.selectById("user001")).thenReturn(null);

        // When
        UserAccount result = userAccountRepository.save(account);

        // Then
        ArgumentCaptor<UserAccountPO> captor = ArgumentCaptor.forClass(UserAccountPO.class);
        verify(userAccountMapper).insert(captor.capture());
        assertEquals("user001", captor.getValue().getUserId());
        assertEquals(new BigDecimal("50.00"), result.getBalance().getAmount());
    }

    @Test
    void testSave_ExistingAccount_Success() {
        // Given
        UserAccount account = UserAccount.builder()
            .userId("user001")
            .balance(Money.of(new BigDecimal("80.00")))
            .version(1)
            .build();
        UserAccountPO existing = new UserAccountPO();
        existing.setUserId("user001");
        existing.setBalance(new BigDecimal("60.00"));
        existing.setVersion(1);
        UserAccountPO updated = new UserAccountPO();
        updated.setUserId("user001");
        updated.setBalance(new BigDecimal("80.00"));
        updated.setVersion(2);
        when(userAccountMapper.selectById("user001")).thenReturn(existing, updated);
        when(userAccountMapper.updateById(any(UserAccountPO.class))).thenReturn(1);

        // When
        UserAccount result = userAccountRepository.save(account);

        // Then
        assertEquals(new BigDecimal("80.00"), result.getBalance().getAmount());
        assertEquals(2, result.getVersion());
        verify(userAccountMapper).updateById(any(UserAccountPO.class));
    }

    @Test
    void testSave_OptimisticLockException() {
        // Given
        UserAccount account = UserAccount.builder()
            .userId("user001")
            .balance(Money.of(new BigDecimal("80.00")))
            .version(1)
            .build();
        UserAccountPO existing = new UserAccountPO();
        existing.setUserId("user001");
        existing.setBalance(new BigDecimal("60.00"));
        existing.setVersion(1);
        when(userAccountMapper.selectById("user001")).thenReturn(existing);
        when(userAccountMapper.updateById(any(UserAccountPO.class))).thenReturn(0);

        // When
        BusinessException exception = assertThrows(BusinessException.class,
            () -> userAccountRepository.save(account));

        // Then
        assertEquals("CONCURRENT_MODIFICATION", exception.getCode());
    }
}
