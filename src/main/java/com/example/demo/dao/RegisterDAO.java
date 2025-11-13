package com.example.demo.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RegisterDAO {

    private final JdbcTemplate jdbc;

    public RegisterDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Kiểm tra email đã tồn tại
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM employee WHERE email = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    // Kiểm tra phone đã tồn tại
    public boolean phoneExists(String phone) {
        String sql = "SELECT COUNT(*) FROM employee WHERE phone = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, phone);
        return count != null && count > 0;
    }

    // Kiểm tra employee_id đã tồn tại
    public boolean employeeIdExists(String employeeId) {
        String sql = "SELECT COUNT(*) FROM employee WHERE employee_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, employeeId);
        return count != null && count > 0;
    }

    // Lấy position_id từ position_name
    public Integer getPositionIdByName(String positionName) {
        String sql = "SELECT position_id FROM Position WHERE position_name = ?";
        return jdbc.queryForObject(sql, Integer.class, positionName);
    }

    // Lấy department_id từ position_id
    public String getDepartmentIdByPositionId(Integer positionId) {
        String sql = "SELECT department_id FROM Position WHERE position_id = ?";
        return jdbc.queryForObject(sql, String.class, positionId);
    }

    // Đăng ký nhân viên
    public int register(
            String employeeId,
            String fullName,
            String gender,
            String dateOfBirth,
            String positionName,
            String phone,
            String address,
            String email,
            String employmentContract,
            Double basicSalary
    ) {
        // Validate gender
        if (!"Nam".equalsIgnoreCase(gender)
                && !"Nữ".equalsIgnoreCase(gender)
                && !"Khác".equalsIgnoreCase(gender)) {
            gender = "Nam";
        }

        // Lấy position_id
        Integer positionId = getPositionIdByName(positionName);
        if (positionId == null) {
            throw new RuntimeException("Không tìm thấy position_id cho: " + positionName);
        }

        // Tự động tìm department_id theo position
        String departmentId = getDepartmentIdByPositionId(positionId);
        if (departmentId == null) {
            throw new RuntimeException("Không tìm thấy department_id cho chức vụ có ID: " + positionId);
        }

        String sql = """
            INSERT INTO employee(
                employee_id, full_name, phone, address,
                date_of_birth, email, gender,
                employment_contract, position_id, department_id,
                basic_salary
            )
            VALUES (?, ?, ?, ?, STR_TO_DATE(?, '%Y-%m-%d'), ?, ?, ?, ?, ?, ?)
        """;

        return jdbc.update(
                sql,
                employeeId,
                fullName,
                phone,
                address,
                dateOfBirth,
                email,
                gender,
                employmentContract,
                positionId,
                departmentId,
                basicSalary
        );
    }
}
