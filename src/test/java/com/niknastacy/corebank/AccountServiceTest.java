package com.niknastacy.corebank;

import com.niknastacy.corebank.entity.Account;
import com.niknastacy.corebank.repository.AccountRepository;
import com.niknastacy.corebank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void testConcurrentTransfersWithoutDeadlock() throws InterruptedException {
        Account acc1 = accountRepository.save(new Account(1L, new BigDecimal("1000.00")));
        Account acc2 = accountRepository.save(new Account(1L, new BigDecimal("1000.00")));

        int numberOfThreads = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final boolean direction = (i % 2 == 0);
            executor.submit(() -> {
                try {
                    if (direction) {
                        accountService.transfer(acc1.getId(), acc2.getId(), new BigDecimal("10.00"));
                    } else {
                        accountService.transfer(acc2.getId(), acc1.getId(), new BigDecimal("10.00"));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Account updated1 = accountRepository.findById(acc1.getId()).get();
        Account updated2 = accountRepository.findById(acc2.getId()).get();

        BigDecimal total = updated1.getMoneyAmount().add(updated2.getMoneyAmount());
        assertEquals(new BigDecimal("2000.00"), total);
        System.out.println("Тест пройден идеально! Сумма сошлась: " + total);
    }
}