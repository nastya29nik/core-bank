package com.niknastacy.corebank.repository;

import com.niknastacy.corebank.entity.Account;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class AccountRepository {
    private final List<Account> accounts;
    private final AtomicLong idGenerator = new AtomicLong(1);

    public AccountRepository() {
        this.accounts = new ArrayList<>();
    }

    public synchronized List<Account> getAccountsByUserId(long userId) {
        return this.accounts.stream()
                .filter(acc -> acc.getUserId() == userId)
                .toList();
    }

    public synchronized Optional<Account> getAccountById(long accountId) {
        return this.accounts.stream()
                .filter(acc -> acc.getId() == accountId)
                .findFirst();
    }

    public synchronized long saveAccount(Account account) {
        long newId = idGenerator.getAndIncrement();
        account.setId(newId);
        this.accounts.add(account);
        return newId;
    }

    public synchronized void deleteAccount(long accountId) {
        this.accounts.removeIf(acc -> acc.getId() == accountId);
    }
}
