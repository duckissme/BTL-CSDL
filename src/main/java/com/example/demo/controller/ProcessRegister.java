package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.RegisterDAO;

@RestController
@RequestMapping("/api")
public class ProcessRegister {

    private final RegisterDAO registerDAO;

    public ProcessRegister(RegisterDAO registerDAO) {
        this.registerDAO = registerDAO;
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String full_name,
            @RequestParam String gender,
            @RequestParam String date_of_birth,
            @RequestParam String position_name,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String email,
            @RequestParam String contract_type
    ) {

        // Kiểm tra username đã tồn tại
        if (registerDAO.usernameExists(username)) {
            return "Tên đăng nhập đã tồn tại!";
        }

        // Kiểm tra email đã tồn tại
        if (registerDAO.emailExists(email)) {
            return "Email đã tồn tại!";
        }

        try {
            int rows = registerDAO.register(
                    username,
                    password,
                    full_name,
                    gender,
                    date_of_birth,   // yyyy-MM-dd từ input date
                    position_name,
                    phone,
                    address,
                    email,
                    contract_type
            );

            return (rows > 0)
                    ? "Đăng ký nhân viên thành công!"
                    : "Lỗi đăng ký!";

        } catch (Exception e) {
            return "Lỗi hệ thống: " + e.getMessage();
        }
    }
}
