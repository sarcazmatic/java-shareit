package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private long id = 0;

    private long makeId() {
        return ++id;
    }

    @Override
    public boolean checkExistId(long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean checkExistEmail(String email) {
        Optional<User> checkEmail = users.values().
                stream().
                filter(u -> u.getEmail().equals(email)).
                findFirst();

        return checkEmail.isPresent();
    }

    @Override
    public void deleteEmailFromSet(String email) {
        Optional<User> findUserEmail = users.values().
                stream().
                filter(user -> email.equals(user.getEmail())).
                findFirst();

        findUserEmail.ifPresent(user -> users.remove(user.getId()));
    }

    @Override
    public User create(User user) {
        user.setId(makeId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User read(long id) {
        return users.get(id);
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }
}
