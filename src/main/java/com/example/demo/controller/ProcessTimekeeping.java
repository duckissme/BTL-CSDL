package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.TimekeepingDAO;

@RestController
@RequestMapping("/api/timekeeping")
public class ProcessTimekeeping {

    private final TimekeepingDAO dao;

    public ProcessTimekeeping(TimekeepingDAO dao) {
        this.dao = dao;
    }

    /**
     * API lấy danh sách chấm công
     */
    @GetMapping
    public List<Map<String, Object>> getTimekeeping(
            @RequestParam String date,
            @RequestParam(required = false) String search) {
        return dao.getTimekeeping(date, search);
    }

    /**
     * API update checkin / checkout / status
     * → Trả về JSON với giá trị mới
     */
    @PostMapping("/update")
    public Map<String, Object> saveTimekeeping(
            @RequestParam long employeeId,
            @RequestParam String date,
            @RequestParam(required = false) String checkIn,
            @RequestParam(required = false) String checkOut,
            @RequestParam(required = false) String workStatus) {

        return dao.saveTimekeeping(employeeId, date, checkIn, checkOut, workStatus);
    }

    /**
     * API sinh bảng chấm công mặc định theo ngày
     */
    @PostMapping("/generate")
    public String generateDaily(@RequestParam String date) {
        try {
            dao.generateDailyTimekeeping(date);
            return "Đã sinh bảng chấm công cho ngày " + date;
        } catch (Exception e) {
            return "Lỗi: " + e.getMessage();
        }
    }
}
