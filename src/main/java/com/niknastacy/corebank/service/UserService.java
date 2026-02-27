package com.niknastacy.corebank.service;

import com.niknastacy.corebank.entity.User;
import com.niknastacy.corebank.exception.BankOperationException;
import com.niknastacy.corebank.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountService accountService;

    public UserService(UserRepository userRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
    }

    public long createUser(String login) {
        if (userRepository.findAll().stream().anyMatch(u -> u.getLogin().equals(login))) {
            throw new BankOperationException("User with this login already exists");
        }

        User user = new User(login);
        long userId = userRepository.addUser(user);
        accountService.createAccount(userId);

        return userId;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
