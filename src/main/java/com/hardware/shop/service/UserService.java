package com.hardware.shop.service;

import com.hardware.shop.dao.UserDAO;
import com.hardware.shop.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public boolean register(User user, String plainPassword) throws SQLException {

        validateUser(user);
        validatePassword(plainPassword);

        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

        user.setPasswordHash(hashedPassword);

        return userDAO.save(user);
    }

    public User login(String username, String plainPassword) throws SQLException {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }

        validatePassword(plainPassword);

        User user = userDAO.findByUsername(username);

        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("User account is inactive.");
        }

        boolean passwordMatches = BCrypt.checkpw(plainPassword, user.getPasswordHash());

        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        return user;
    }

    public User findById(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }

        return userDAO.findById(id);
    }

    private void validateUser(User user) {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (user.getUsername() == null || user.getUsername().isBlank()) {

            throw new IllegalArgumentException("Username is required.");
        }

        if (user.getFullName() == null || user.getFullName().isBlank()) {

            throw new IllegalArgumentException("Full name is required.");
        }

        if (user.getStatus() == null || user.getStatus().isBlank()) {

            user.setStatus("ACTIVE");
        }
    }

    private void validatePassword(String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must contain at least 6 characters.");
        }
    }
}