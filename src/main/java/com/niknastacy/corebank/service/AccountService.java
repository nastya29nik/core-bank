package com.niknastacy.corebank.service;

import com.niknastacy.corebank.config.AccountProperties;
import com.niknastacy.corebank.entity.Account;
import com.niknastacy.corebank.exception.AccountNotFoundException;
import com.niknastacy.corebank.exception.BankOperationException;
import com.niknastacy.corebank.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountProperties accountProperties;

    public AccountService(AccountRepository accountRepository,
                          AccountProperties accountProperties) {
        this.accountRepository = accountRepository;
        this.accountProperties = accountProperties;
    }

    public List<Account> getUserAccounts(long userId) {
        return accountRepository.getAccountsByUserId(userId);
    }

    public long createAccount(long userId) {
        Account account = new Account();
        account.setUserId(userId);
        account.setMoneyAmount(accountProperties.getAmount());
        return accountRepository.saveAccount(account);
    }

    public void deposit(long accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        Account account = accountRepository.getAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));

        synchronized (account) {
            account.setMoneyAmount(account.getMoneyAmount().add(amount));
        }
    }

    public void withdraw(long accountId, BigDecimal amount) {
        Account account = accountRepository.getAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));

        synchronized (account) {
            validateSufficientFunds(account, amount);
            BigDecimal newBalance = account.getMoneyAmount().subtract(amount);
            account.setMoneyAmount(newBalance);
        }
    }

    public void transfer(long fromId, long toId, BigDecimal amount) {
        if (fromId == toId) {
            throw new BankOperationException("Cannot transfer to the same account");
        }

        Account from = accountRepository.getAccountById(fromId)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));
        Account to = accountRepository.getAccountById(toId)
                .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        Account firstLock = from.getId() < to.getId() ? from : to;
        Account secondLock = from.getId() < to.getId() ? to : from;

        synchronized (firstLock) {
            synchronized (secondLock) {
                validateSufficientFunds(from, amount);
                BigDecimal amountToTransfer = amount;

                if (from.getUserId() != to.getUserId()) {
                    BigDecimal commissionAmount = amount.multiply(accountProperties.getTransferCommission());
                    amountToTransfer = amount.subtract(commissionAmount);
                }

                from.setMoneyAmount(from.getMoneyAmount().subtract(amount));
                to.setMoneyAmount(to.getMoneyAmount().add(amountToTransfer));
            }
        }
    }

    public void closeAccount(long accountId) {
        Account accountToClose = accountRepository.getAccountById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account " + accountId + " not found"));

        List<Account> userAccounts = accountRepository.getAccountsByUserId(accountToClose.getUserId());

        if (userAccounts.size() <= 1) {
            throw new BankOperationException("Cannot close the only account of the user");
        }

        Account targetAccount = userAccounts.stream()
                .filter(acc -> acc.getId() != accountId)
                .findFirst()
                .orElseThrow(() -> new BankOperationException("Target account for funds transfer not found"));

        Account firstLock = accountToClose.getId() < targetAccount.getId() ? accountToClose : targetAccount;
        Account secondLock = accountToClose.getId() < targetAccount.getId() ? targetAccount : accountToClose;

        synchronized (firstLock) {
            synchronized (secondLock) {
                BigDecimal remainingBalance = accountToClose.getMoneyAmount();
                targetAccount.setMoneyAmount(targetAccount.getMoneyAmount().add(remainingBalance));
                accountToClose.setMoneyAmount(BigDecimal.ZERO);
                accountRepository.deleteAccount(accountId);
            }
        }
    }

    private static void validateSufficientFunds(Account account, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (account.getMoneyAmount().compareTo(amount) < 0) {
            throw new BankOperationException("Insufficient funds on balance");
        }
    }
}
