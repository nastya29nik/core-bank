package com.niknastacy.corebank.cli;

import com.niknastacy.corebank.service.AccountService;
import com.niknastacy.corebank.service.UserService;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

@Component
public class ConsoleListener {

    private final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private final UserService userService;
    private final AccountService accountService;

    public ConsoleListener(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    public void run() {
        System.out.println("MiniBank started. Type EXIT to stop.");
        boolean isRunning = true;

        while (isRunning) {
            try {
                System.out.print("\nEnter command: ");
                String command = br.readLine();

                if (command == null) {
                    System.out.println("Bye-Bye!");
                    isRunning = false;
                    continue;
                }

                switch (command.toUpperCase()) {
                    case "USER_CREATE" -> methodUserCreate();
                    case "SHOW_ALL_USERS" -> methodShowAllUsers();
                    case "ACCOUNT_CREATE" -> methodAccountCreate();
                    case "ACCOUNT_DEPOSIT" -> methodAccountDeposit();
                    case "ACCOUNT_WITHDRAW" -> methodAccountWithdraw();
                    case "ACCOUNT_TRANSFER" -> methodAccountTransfer();
                    case "ACCOUNT_CLOSE" -> methodAccountClose();
                    case "EXIT" -> {
                        System.out.println("Bye-Bye!");
                        isRunning = false;
                    }
                    default -> System.out.println("Unknown command. Available: USER_CREATE, SHOW_ALL_USERS, ACCOUNT_CREATE, ACCOUNT_DEPOSIT, ACCOUNT_WITHDRAW, ACCOUNT_TRANSFER, ACCOUNT_CLOSE, EXIT");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void methodUserCreate() throws IOException {
        System.out.println("Enter login:");
        String login = br.readLine();
        long idUser = userService.createUser(login);
        System.out.println("User created successfully, your id is " + idUser);
    }

    private void methodShowAllUsers() {
        userService.getUsers().forEach(user -> {
            System.out.println(user);
            accountService.getUserAccounts(user.getId()).forEach(System.out::println);
        });
    }

    private void methodAccountCreate() throws IOException {
        long userId = readLong("Enter user ID:");
        long accId = accountService.createAccount(userId);
        System.out.println("Account created successfully, your acc id is " + accId);
    }

    private void methodAccountDeposit() throws IOException {
        long accountId = readLong("Enter account ID:");
        BigDecimal amount = readBigDecimal();
        accountService.deposit(accountId, amount);
        System.out.println("Deposit successful");
    }

    private void methodAccountWithdraw() throws IOException {
        long accountId = readLong("Enter account ID:");
        BigDecimal amount = readBigDecimal();
        accountService.withdraw(accountId, amount);
        System.out.println("Withdraw successful");
    }

    private void methodAccountTransfer() throws IOException {
        long fromId = readLong("Enter source account ID:");
        long toId = readLong("Enter target account ID:");
        BigDecimal amount = readBigDecimal();
        accountService.transfer(fromId, toId, amount);
        System.out.println("Transfer successful");
    }

    private void methodAccountClose() throws IOException {
        long accountId = readLong("Enter account ID to close:");
        accountService.closeAccount(accountId);
        System.out.println("Account closed successfully");
    }

    private long readLong(String message) throws IOException {
        while (true) {
            try {
                System.out.println(message);
                return Long.parseLong(br.readLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Enter a valid integer number (ID).");
            }
        }
    }

    private BigDecimal readBigDecimal() {
        while (true) {
            try {
                System.out.println("Enter amount:");
                return new BigDecimal(br.readLine());
            } catch (Exception e) {
                System.out.println("Invalid input. Enter a decimal value.");
            }
        }
    }
}