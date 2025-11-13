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
            @RequestParam String employee_id,
            @RequestParam String full_name,
            @RequestParam String gender,
            @RequestParam String date_of_birth,
            @RequestParam String position_name,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String email,
            @RequestParam String employment_contract,
            @RequestParam(required = false) Double basic_salary
    ) {
        // Kiểm tra input bắt buộc
        if (employee_id.isEmpty() || full_name.isEmpty() || gender.isEmpty() || date_of_birth.isEmpty()
                || position_name.isEmpty() || phone.isEmpty() || address.isEmpty()
                || email.isEmpty() || employment_contract.isEmpty() || basic_salary == null) {
            return "Vui lòng nhập đầy đủ thông tin!";
        }

        // Kiểm tra employee_id tồn tại
        if (registerDAO.employeeIdExists(employee_id)) {
            return "Mã nhân viên đã tồn tại!";
        }

        // Kiểm tra email đã tồn tại
        if (registerDAO.emailExists(email)) {
            return "Email đã tồn tại!";
        }

        // Kiểm tra số điện thoại tồn tại
        if (registerDAO.phoneExists(phone)) {
            return "Số điện thoại đã tồn tại!";
        }

        // Nếu basic_salary null → mặc định 0
        if (basic_salary == null) {
            basic_salary = 0.0;
        }

        try {
            int rows = registerDAO.register(
                    employee_id,
                    full_name,
                    gender,
                    date_of_birth,
                    position_name,
                    phone,
                    address,
                    email,
                    employment_contract,
                    basic_salary
            );

            return (rows > 0)
                    ? "Đăng ký nhân viên thành công!"
                    : "Lỗi đăng ký!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi hệ thống!";
        }
    }
}
