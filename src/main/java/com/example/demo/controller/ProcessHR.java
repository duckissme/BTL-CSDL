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

    private final EmployeeDAO employeeDAO;

    public ProcessHR(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    // 1️⃣ Lấy danh sách nhân viên (có tìm kiếm)
    @GetMapping("/employees")
    public List<Map<String,Object>> getEmployees(@RequestParam(required = false) String search){
        return employeeDAO.getAllEmployees(search);
    }

    // 2️⃣ Xóa nhân viên
    @DeleteMapping("/employees/{id}")
    public String deleteEmployee(@PathVariable("id") long employeeId){
        int rows = employeeDAO.deleteEmployee(employeeId);
        return rows > 0 ? "Đã xóa nhân viên với ID: " + employeeId 
                         : "Không tìm thấy nhân viên với ID: " + employeeId;
    }

    // 3️⃣ Cập nhật nhân viên bằng @RequestParam (THÊM contractType)
    @PutMapping("/employees/{id}")
    public String updateEmployee(@PathVariable("id") long employeeId,
                                 @RequestParam String fullName,
                                 @RequestParam String positionName,
                                 @RequestParam String dateOfBirth,
                                 @RequestParam String gender,
                                 @RequestParam String email,
                                 @RequestParam String phone,
                                 @RequestParam String workStatus,
                                 @RequestParam String contractType) {
        try {
            int rows = employeeDAO.updateEmployeeByPositionName(
                employeeId, fullName, positionName,
                dateOfBirth, gender, email, phone,
                workStatus, contractType
            );

            return rows > 0 ? "Đã cập nhật nhân viên ID " + employeeId
                             : "Không tìm thấy nhân viên ID " + employeeId;

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi server: " + e.getMessage();
        }
    }

    // 4️⃣ Lấy danh sách position
    @GetMapping("/positions")
    public List<Map<String,Object>> getPositions() {
        return employeeDAO.getAllPositions();
    }
}
