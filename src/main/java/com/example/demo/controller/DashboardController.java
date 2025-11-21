package com.example.demo.controller;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.AdminDAO;
import com.example.demo.dao.DirectorDAO;

@RestController
public class DashboardController {

    private final DirectorDAO directorDAO;
    private final AdminDAO adminDAO;

    public DashboardController(DirectorDAO directorDAO, AdminDAO adminDAO) {
        this.directorDAO = directorDAO;
        this.adminDAO = adminDAO;
    }

    /**
     * Lấy toàn bộ dữ liệu dashboard: summary + danh sách phòng ban
     * params:
     * - month: yyyy-MM (optional)
     * - q: tìm kiếm tên/id phòng ban (optional)
     */
    @GetMapping("/api/dashboard")
    public Map<String, Object> getDashboard(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "q", required = false) String q) {

        int m, y;
        try {
            YearMonth ym = (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month);
            m = ym.getMonthValue();
            y = ym.getYear();
        } catch (Exception e) {
            YearMonth ym = YearMonth.now();
            m = ym.getMonthValue();
            y = ym.getYear();
        }

        // 1. Lấy danh sách phòng ban với dữ liệu tháng hiện tại + tháng trước
        List<Map<String, Object>> departments = directorDAO.getDepartmentReport(m, y);

        // 2. Filter theo q nếu có
        if (q != null && !q.isBlank()) {
            String qLower = q.toLowerCase();
            departments.removeIf(d -> {
                String name = d.get("department_name") != null ? d.get("department_name").toString().toLowerCase() : "";
                String id = d.get("department_id") != null ? d.get("department_id").toString().toLowerCase() : "";
                return !name.contains(qLower) && !id.contains(qLower);
            });
        }

        // 3. Lấy summary theo tháng
        Map<String, Object> summaryData = adminDAO.getDashboardSummary(m, y);

        // 4. Gộp cả hai vào 1 Map trả về
        Map<String, Object> result = new HashMap<>();
        result.put("summary", summaryData);
        result.put("departments", departments);
        result.put("month", String.format("%04d-%02d", y, m));

        return result;
    }
}
