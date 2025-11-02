package com.example.demo.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RegisterDAO {

    private final JdbcTemplate jdbc;

    public RegisterDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Kiểm tra username đã tồn tại chưa
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM employee WHERE username = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    // Kiểm tra email đã tồn tại chưa
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM employee WHERE email = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    // Lấy position_id từ position_name
    public Integer getPositionIdByName(String positionName) {
        String sql = "SELECT position_id FROM Position WHERE position_name = ?";
        return jdbc.queryForObject(sql, Integer.class, positionName);
    }

    // Đăng ký nhân viên
    public int register(
            String username,
            String password,
            String fullName,
            String gender,
            String dateOfBirth,   // format: yyyy-MM-dd từ input type=date
            String positionName,
            String phone,
            String address,
            String email,
            String contractType
    ) {

        // Validate gender
        if (!"Nam".equalsIgnoreCase(gender)
                && !"Nữ".equalsIgnoreCase(gender)
                && !"Khác".equalsIgnoreCase(gender)) {
            gender = "Nam"; // default
        }

        // Convert position_name -> position_id
        Integer positionId = getPositionIdByName(positionName);
        if (positionId == null) {
            throw new RuntimeException("Không tìm thấy position_id cho: " + positionName);
        }

        String sql = """
            INSERT INTO employee(
                username, password, full_name, phone, address,
                date_of_birth, email, gender, contract_type, position_id
            )
            VALUES (?, ?, ?, ?, ?, STR_TO_DATE(?, '%Y-%m-%d'), ?, ?, ?, ?)
        """;

        return jdbc.update(
                sql,
                username,
                password,
                fullName,
                phone,
                address,
                dateOfBirth,   // yyyy-MM-dd → STR_TO_DATE
                email,
                gender,
                contractType,
                positionId
        );
    }
}
