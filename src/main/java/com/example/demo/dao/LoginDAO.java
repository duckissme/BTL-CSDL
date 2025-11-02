package com.example.demo.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginDAO {

    private final JdbcTemplate jdbc;

    public LoginDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean login(String username, String password) {
        String sql = "SELECT COUNT(*) FROM employee WHERE username = ? AND password = ?";

        Integer count = jdbc.queryForObject(sql, Integer.class, username, password);

        return count != null && count == 1;
    }
}

