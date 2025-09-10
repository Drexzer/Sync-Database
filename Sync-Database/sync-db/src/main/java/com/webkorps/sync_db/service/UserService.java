package com.webkorps.sync_db.service;

import com.webkorps.sync_db.entity.User;
import com.webkorps.sync_db.repository.link.LinkUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private LinkUserRepository linkUserRepository;

    public User saveUser(User user) {
        return linkUserRepository.save(user);
    }

    public List<User> getAllUsers() {
        return linkUserRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return linkUserRepository.findById(id);
    }

    public void deleteUser(Long id) {
        linkUserRepository.deleteById(id);
    }
}
