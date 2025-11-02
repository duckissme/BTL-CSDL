package com.example.demo.dao;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TimekeepingDAO {

    private final JdbcTemplate jdbc;

    public TimekeepingDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Lấy danh sách chấm công
    public List<Map<String,Object>> getTimekeeping(String date, String search) {
        String sql = "SELECT t.employee_id, e.full_name, t.work_date AS date, " +
                     "t.check_in, t.check_out, t.overtime, t.work_status " +
                     "FROM timekeeping t " +
                     "JOIN employee e ON t.employee_id = e.employee_id " +
                     "WHERE t.work_date = ?";

        if (search != null && !search.isEmpty()) {
            sql += " AND e.full_name LIKE ?";
            return jdbc.queryForList(sql, date, "%" + search + "%");
        }

        return jdbc.queryForList(sql, date);
    }

    // Lưu checkin / checkout / workStatus – trả về JSON mới
    public Map<String,Object> saveTimekeeping(long employeeId, String date,
                                              String checkIn, String checkOut, String workStatus) {

        // Lấy dữ liệu hiện tại
        String selectSql = "SELECT check_in, check_out, work_status " +
                "FROM timekeeping WHERE employee_id=? AND work_date=?";
        Map<String,Object> current = jdbc.queryForMap(selectSql, employeeId, date);

        // Convert về String an toàn (vì MySQL TIME trả về java.sql.Time)
        String oldCheckIn  = current.get("check_in")  != null ? current.get("check_in").toString() : null;
        String oldCheckOut = current.get("check_out") != null ? current.get("check_out").toString() : null;
        String oldStatus   = current.get("work_status") != null ? current.get("work_status").toString() : null;

        // Giữ giá trị cũ nếu FE không truyền lên
        String finalCheckIn  = (checkIn  != null && !checkIn.isEmpty())  ? checkIn  : oldCheckIn;
        String finalCheckOut = (checkOut != null && !checkOut.isEmpty()) ? checkOut : oldCheckOut;
        String finalStatus   = (workStatus != null && !workStatus.isEmpty()) ? workStatus : oldStatus;

        // Tính overtime
        double overtime = calculateOvertime(finalCheckIn, finalCheckOut, finalStatus);

        // Cập nhật DB
        String updateSql =
                "UPDATE timekeeping SET check_in=?, check_out=?, work_status=?, overtime=? " +
                "WHERE employee_id=? AND work_date=?";

        jdbc.update(updateSql, finalCheckIn, finalCheckOut, finalStatus, overtime, employeeId, date);

        // Trả về JSON để FE cập nhật UI
        Map<String,Object> result = new HashMap<>();
        result.put("employeeId", employeeId);
        result.put("date", date);
        result.put("checkIn", finalCheckIn);
        result.put("checkOut", finalCheckOut);
        result.put("workStatus", finalStatus);
        result.put("overtime", overtime);

        return result;
    }

    // Tính overtime
    private double calculateOvertime(String checkIn, String checkOut, String workStatus) {
        if (!"Đi làm".equalsIgnoreCase(workStatus)) return 0;
        if (checkIn == null || checkOut == null) return 0;

        try {
            LocalTime in = LocalTime.parse(checkIn);
            LocalTime out = LocalTime.parse(checkOut);

            long minutes = Duration.between(in, out).toMinutes();
            double hours = minutes / 60.0;

            return hours > 8 ? hours - 8 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // Sinh bảng chấm công mặc định
    public void generateDailyTimekeeping(String date) {
        String sqlEmployees = "SELECT employee_id FROM employee";
        List<Long> employeeIds = jdbc.queryForList(sqlEmployees, Long.class);

        for (Long empId : employeeIds) {
            String checkSql = "SELECT COUNT(*) FROM timekeeping WHERE employee_id=? AND work_date=?";
            Integer count = jdbc.queryForObject(checkSql, Integer.class, empId, date);

            if (count == null || count == 0) {
                String insertSql =
                        "INSERT INTO timekeeping(employee_id, work_date, work_status, overtime) VALUES(?,?,?,?)";
                jdbc.update(insertSql, empId, date, "Đi làm", 0);
            }
        }
    }
}
