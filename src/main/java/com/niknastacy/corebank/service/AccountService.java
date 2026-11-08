package com.niknastacy.corebank.service;

import com.niknastacy.corebank.config.AccountProperties;
import com.niknastacy.corebank.entity.Account;
import com.niknastacy.corebank.exception.AccountNotFoundException;
import com.niknastacy.corebank.exception.BankOperationException;
import com.niknastacy.corebank.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountProperties accountProperties;

    public AccountService(AccountRepository accountRepository, AccountProperties accountProperties) {
        this.accountRepository = accountRepository;
        this.accountProperties = accountProperties;
    }

    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.findAllByUserId(userId);
    }

    @Transactional
    public Long createAccount(Long userId) {
        Account account = new Account(userId, accountProperties.getAmount());
        return accountRepository.save(account).getId();
    }

    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));

        account.setMoneyAmount(account.getMoneyAmount().add(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));

        validateSufficientFunds(account, amount);
        account.setMoneyAmount(account.getMoneyAmount().subtract(amount));
        accountRepository.save(account);
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        if (fromId.equals(toId)) {
            throw new BankOperationException("Cannot transfer to the same account");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Long firstId = fromId < toId ? fromId : toId;
        Long secondId = fromId < toId ? toId : fromId;

        Account first = accountRepository.findByIdWithLock(firstId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + firstId + " not found"));
        Account second = accountRepository.findByIdWithLock(secondId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + secondId + " not found"));

        Account from = fromId.equals(first.getId()) ? first : second;
        Account to = toId.equals(first.getId()) ? first : second;

        validateSufficientFunds(from, amount);

        BigDecimal amountToTransfer = amount;
        if (!from.getUserId().equals(to.getUserId())) {
            BigDecimal commission = amount.multiply(accountProperties.getTransferCommission());
            amountToTransfer = amount.subtract(commission);
        }

        from.setMoneyAmount(from.getMoneyAmount().subtract(amount));
        to.setMoneyAmount(to.getMoneyAmount().add(amountToTransfer));

        accountRepository.save(from);
        accountRepository.save(to);
    }

    private void validateSufficientFunds(Account account, BigDecimal amount) {
        if (account.getMoneyAmount().compareTo(amount) < 0) {
            throw new BankOperationException("Insufficient funds on balance");
        }
    }
}