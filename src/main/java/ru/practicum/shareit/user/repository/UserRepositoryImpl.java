package ru.practicum.shareit.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> usersByEmail = new HashSet<>();
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
        return usersByEmail.contains(email);
    }

    @Override
    public void deleteEmailFromSet(String email) {
        usersByEmail.remove(email);
    }

    @Override
    public User create(User user) {
        user.setId(makeId());
        users.put(user.getId(), user);
        usersByEmail.add(user.getEmail());
        return user;
    }

    @Override
    public User read(long id) {
        return users.get(id);
    }


    @Override
    public void delete(long id) {
        usersByEmail.remove(users.get(id).getEmail());
        users.remove(id);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }
}
