package com.example.demo.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DirectorDAO {

    private final JdbcTemplate jdbc;

    public DirectorDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Lấy danh sách phòng ban với số liệu tháng hiện tại và tháng trước,
     * và insert vào DepartmentMonthlyReport nếu chưa có
     */
    public List<Map<String, Object>> getDepartmentReport(int month, int year) {
        // 1. Lấy dữ liệu current month từ Timekeeping + Employee
        String sqlCurrent = """
                SELECT d.department_id, d.name AS department_name,
                       IFNULL(SUM(CASE WHEN t.work_status='Nghỉ phép' THEN 1 ELSE 0 END),0) AS total_leave,
                       IFNULL(SUM(CASE WHEN t.work_status='Vắng mặt' THEN 1 ELSE 0 END),0) AS total_absent,
                       IFNULL(SUM(IFNULL(t.overtime,0)),0) AS total_overtime,
                       IFNULL(SUM(CASE WHEN t.check_in > '08:00:00' THEN 1 ELSE 0 END),0) AS total_late,
                       ROUND(IFNULL(AVG(
                           LEAST(100, GREATEST(0, 100 -
                               (CASE WHEN t.work_status='Nghỉ phép' THEN 1 ELSE 0 END) -
                               (CASE WHEN t.work_status='Vắng mặt' THEN 3 ELSE 0 END) -
                               (CASE WHEN t.check_in > '08:00:00' THEN 0.5 ELSE 0 END) +
                               (IFNULL(t.overtime,0)*0.5)
                           ))
                       ),0),2) AS current_score,
                       COUNT(DISTINCT e.employee_id) AS employee_count
                FROM Department d
                LEFT JOIN Employee e ON d.department_id = e.department_id
                LEFT JOIN Timekeeping t 
                  ON t.employee_id = e.employee_id 
                  AND MONTH(t.work_date)=? AND YEAR(t.work_date)=?
                WHERE d.department_id <> 'DP001'
                GROUP BY d.department_id, d.name
                """;

        List<Map<String, Object>> current = jdbc.queryForList(sqlCurrent, month, year);

        // 2. Lấy dữ liệu tháng trước từ DepartmentMonthlyReport
        int prevMonth = month == 1 ? 12 : month - 1;
        int prevYear = month == 1 ? year - 1 : year;

        String sqlPrev = """
                SELECT department_id, avg_score AS previous_score
                FROM DepartmentMonthlyReport
                WHERE month=? AND year=?
                """;

        List<Map<String, Object>> prev = jdbc.queryForList(sqlPrev, prevMonth, prevYear);

        Map<String, Double> prevMap = new HashMap<>();
        for (Map<String, Object> r : prev) {
            prevMap.put((String) r.get("department_id"),
                        r.get("previous_score") != null ? ((Number) r.get("previous_score")).doubleValue() : 0.0);
        }

        // 3. Gán previous_score + changePercent cho current
        for (Map<String, Object> d : current) {
            String deptId = (String) d.get("department_id");
            Double prevScore = prevMap.getOrDefault(deptId, 0.0);
            Double curScore = d.get("current_score") != null ? ((Number) d.get("current_score")).doubleValue() : 0.0;
            d.put("previous_score", prevScore);
            d.put("changePercent", prevScore == 0 ? null : (curScore - prevScore) / prevScore * 100);
        }

        // 4. Tính tổng lương theo phòng ban
        for (Map<String, Object> d : current) {
            String deptId = (String) d.get("department_id");

            Double totalPayroll = jdbc.queryForObject("""
                    SELECT IFNULL(SUM(p.total_salary),0)
                    FROM Payroll p
                    JOIN Employee e ON p.employee_id = e.employee_id
                    WHERE e.department_id = ? AND p.month = ? AND p.year = ?
                    """, Double.class, deptId, month, year);

            d.put("totalPayroll", totalPayroll);
            d.put("lateCount", d.get("total_late"));
        }

        // 5. INSERT vào DepartmentMonthlyReport nếu chưa tồn tại
        String insertSql = """
                INSERT INTO DepartmentMonthlyReport
                (report_id, department_id, month, year, total_payroll, avg_score, total_absent, total_leave, total_late, total_overtime)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String checkSql = "SELECT COUNT(*) FROM DepartmentMonthlyReport WHERE department_id=? AND month=? AND year=?";

        for (Map<String, Object> d : current) {
            String deptId = (String) d.get("department_id");

            Integer count = jdbc.queryForObject(checkSql, Integer.class, deptId, month, year);
            if (count == null || count == 0) { // chỉ insert nếu chưa tồn tại
                String reportId = String.format("REP-%04d%02d-%s", year, month, deptId);

                double avgScore = d.get("current_score") != null ? ((Number) d.get("current_score")).doubleValue() : 0.0;
                int totalAbsent = d.get("total_absent") != null ? ((Number) d.get("total_absent")).intValue() : 0;
                int totalLeave = d.get("total_leave") != null ? ((Number) d.get("total_leave")).intValue() : 0;
                int totalLate = d.get("total_late") != null ? ((Number) d.get("total_late")).intValue() : 0;
                double totalOT = d.get("total_overtime") != null ? ((Number) d.get("total_overtime")).doubleValue() : 0.0;

                jdbc.update(insertSql,
                        reportId,
                        deptId,
                        month,
                        year,
                        d.get("totalPayroll"),
                        avgScore,
                        totalAbsent,
                        totalLeave,
                        totalLate,
                        totalOT
                );
            }
        }

        // 6. Trả về luôn dữ liệu tính toán, không dùng DepartmentMonthlyReport cho dashboard
        return current;
    }
}
