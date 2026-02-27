package com.niknastacy.corebank.repository;

import com.niknastacy.corebank.entity.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {
    private final List<User> users;
    private final AtomicLong idGenerator = new AtomicLong(1);

    public UserRepository() {
        this.users = new ArrayList<>();
    }

    public synchronized List<User> findAll() {
        return List.copyOf(this.users);
    }

    public synchronized long addUser(User user) {
        long newId = idGenerator.getAndIncrement();
        user.setId(newId);
        this.users.add(user);
        return newId;
    }
}
