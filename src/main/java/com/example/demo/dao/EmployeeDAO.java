package com.example.demo.dao;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EmployeeDAO {

    private final JdbcTemplate jdbc;

    public EmployeeDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // =============================
    // Lấy tất cả nhân viên (có search)
    // =============================
    public List<Map<String, Object>> getAllEmployees(String search) {
        String sql = "SELECT e.employee_id, e.full_name, e.date_of_birth, e.gender, e.email, e.phone, "
                   + "e.work_status, e.employment_contract, e.basic_salary, "
                   + "p.position_name "
                   + "FROM Employee e "
                   + "LEFT JOIN Position p ON e.position_id = p.position_id";

        if (search != null && !search.isEmpty()) {
            sql += " WHERE e.full_name LIKE ?";
            return jdbc.queryForList(sql, "%" + search + "%");
        }

        return jdbc.queryForList(sql);
    }

    // =============================
    // Xóa nhân viên
    // =============================
    public int deleteEmployee(String employeeId) {
        String sql = "DELETE FROM Employee WHERE employee_id=?";
        return jdbc.update(sql, employeeId);
    }

    // =============================
    // Lấy tất cả chức vụ
    // =============================
    public List<Map<String, Object>> getAllPositions() {
        return jdbc.queryForList("SELECT position_id, position_name FROM Position");
    }

    // =============================
    // Cập nhật nhân viên (KHÔNG cập nhật mã NV)
    // =============================
    public int updateEmployee(String employeeId,
                              String fullName,
                              String positionName,
                              String dateOfBirth,
                              String gender,
                              String email,
                              String phone,
                              String workStatus,
                              String employmentContract,
                              Double basicSalary) {

        // Lấy position_id từ position_name
        String sqlPos = "SELECT position_id FROM Position WHERE position_name=?";
        Integer positionId = jdbc.queryForObject(sqlPos, Integer.class, positionName);

        if (positionId == null) {
            throw new RuntimeException("Không tìm thấy chức vụ: " + positionName);
        }

        // Không cập nhật employee_code
        String sql = "UPDATE Employee SET "
                   + "full_name=?, date_of_birth=?, gender=?, email=?, phone=?, "
                   + "work_status=?, position_id=?, employment_contract=?, basic_salary=? "
                   + "WHERE employee_id=?";

        return jdbc.update(sql,
                fullName.trim(),
                dateOfBirth,
                gender,
                email.trim(),
                phone.trim(),
                workStatus,
                positionId,
                employmentContract,
                basicSalary,
                employeeId
        );
    }
}
