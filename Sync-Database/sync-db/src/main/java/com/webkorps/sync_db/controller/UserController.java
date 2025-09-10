package com.webkorps.sync_db.controller;

import com.webkorps.sync_db.entity.User;
import com.webkorps.sync_db.service.UserFailoverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserFailoverService userFailoverService;

    @Autowired
    public UserController(UserFailoverService userFailoverService) {
        this.userFailoverService = userFailoverService;
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userFailoverService.getAllUsers();
        return ResponseEntity.ok(users);
    }

//    // Get user by ID
//    @GetMapping("/{id}")
//    public ResponseEntity<User> getUserById(@PathVariable Long id) {
//        Optional<User> user = userFailoverService.findUserById(id);
//        return user.map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }

    // Create new user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            User savedUser = userFailoverService.saveUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(null);
        }
    }

//    // Update user
//    @PutMapping("/{id}")
//    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
//        Optional<User> existingUser = userFailoverService.findUserById(id);
//        if (existingUser.isPresent()) {
//            User user = existingUser.get();
//            user.setName(updatedUser.getName());
//            user.setEmail(updatedUser.getEmail());
//            User savedUser = userFailoverService.saveUser(user);
//            return ResponseEntity.ok(savedUser);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

//    // Delete user
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
//        try {
//            userFailoverService.deleteUser(id);
//            return ResponseEntity.noContent().build();
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }

    // Manually trigger sync
    @PostMapping("/sync")
    public ResponseEntity<String> triggerSync() {
        userFailoverService.syncDatabases();
        return ResponseEntity.ok("Manual sync triggered successfully.");
    }
}
