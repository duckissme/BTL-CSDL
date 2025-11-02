package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import com.example.demo.dao.LoginDAO;

@RestController
@RequestMapping("/api")
public class ProcessLogin {

    private final LoginDAO loginDAO;

    public ProcessLogin(LoginDAO loginDAO) {
        this.loginDAO = loginDAO;
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password
    ) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return "Username hoặc mật khẩu không được để trống!";
        }

        boolean ok = loginDAO.login(username, password);

        if (ok) {
            return "Đăng nhập thành công!";
        } else {
            return "Sai tên đăng nhập hoặc mật khẩu!";
        }
    }
}

