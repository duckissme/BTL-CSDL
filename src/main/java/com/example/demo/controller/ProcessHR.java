package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.EmployeeDAO;

@RestController
@RequestMapping("/api/hr")
public class ProcessHR {

    private final EmployeeDAO dao;

    public ProcessHR(EmployeeDAO dao) {
        this.dao = dao;
    }

    // =============================
    // Lấy tất cả nhân viên (có tìm kiếm)
    // =============================
    @GetMapping("/employees")
    public List<Map<String, Object>> getEmployees(@RequestParam(required = false) String search) {
        return dao.getAllEmployees(search);
    }

    // =============================
    // Xóa nhân viên
    // =============================
    @DeleteMapping("/employees/{id}")
    public String deleteEmployee(@PathVariable("id") String id) {
        int rows = dao.deleteEmployee(id);
        return rows > 0 ? "Xóa thành công" : "Xóa thất bại";
    }

    // =============================
    // Cập nhật nhân viên (KHÔNG cập nhật mã nhân viên)
    // =============================
    @PutMapping("/employees/{id}")
    public String updateEmployee(
            @PathVariable("id") String id,
            @RequestParam String fullName,
            @RequestParam String positionName,
            @RequestParam String dateOfBirth,
            @RequestParam String gender,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String workStatus,
            @RequestParam String employmentContract,
            @RequestParam Double basicSalary) {

        dao.updateEmployee(id, fullName, positionName, dateOfBirth, gender, email,
                phone, workStatus, employmentContract, basicSalary);
        return "Cập nhật thành công";
    }

    // =============================
    // Lấy danh sách chức vụ
    // =============================
    @GetMapping("/positions")
    public List<Map<String, Object>> getPositions() {
        return dao.getAllPositions();
    }
}
