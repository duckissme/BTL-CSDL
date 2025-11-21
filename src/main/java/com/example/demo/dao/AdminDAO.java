package com.example.demo.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDAO {

    private final JdbcTemplate jdbc;

    public AdminDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Trả về tổng quan dashboard cho 1 tháng
     */
    public Map<String, Object> getDashboardSummary(int month, int year) {
        Map<String, Object> summary = new HashMap<>();

        String monthWhere = " WHERE MONTH(work_date)=? AND YEAR(work_date)=? ";

        Integer totalEmployees = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Employee", Integer.class);

        Double totalPayroll = jdbc.queryForObject(
                "SELECT SUM(total_salary) FROM Payroll WHERE month=? AND year=?",
                Double.class, month, year);

        Double totalOvertime = jdbc.queryForObject(
                "SELECT SUM(overtime) FROM Timekeeping " + monthWhere,
                Double.class, month, year);

        Integer presentCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Timekeeping " + monthWhere + " AND work_status='Đi làm'",
                Integer.class, month, year);

        Integer leaveCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Timekeeping " + monthWhere + " AND work_status='Nghỉ phép'",
                Integer.class, month, year);

        Integer absentCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Timekeeping " + monthWhere + " AND work_status='Vắng mặt'",
                Integer.class, month, year);
        Integer totalLate = jdbc.queryForObject(
                "SELECT COUNT(*) FROM Timekeeping " + monthWhere + " AND check_in > '08:00:00'",
                Integer.class, month, year);

        summary.put("totalEmployees", totalEmployees);
        summary.put("totalPayroll", totalPayroll != null ? totalPayroll : 0);
        summary.put("totalOvertime", totalOvertime != null ? totalOvertime : 0);
        summary.put("presentCount", presentCount != null ? presentCount : 0);
        summary.put("leaveCount", leaveCount != null ? leaveCount : 0);
        summary.put("absentCount", absentCount != null ? absentCount : 0);
        summary.put("totalLate", totalLate != null ? totalLate : 0);

        return summary;
    }
}
