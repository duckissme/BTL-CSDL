package com.example.demo.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.PayrollDAO;

@RestController
@RequestMapping("/api/payroll")
public class ProcessPayroll {

    private final PayrollDAO payrollDAO;

    public ProcessPayroll(PayrollDAO payrollDAO) {
        this.payrollDAO = payrollDAO;
    }

    @GetMapping
    public Map<String, Object> getPayroll(
            @RequestParam String employeeId,
            @RequestParam int month,
            @RequestParam int year
    ) {
        // Trả dữ liệu lương dạng JSON
        return payrollDAO.calculateAndSavePayroll(employeeId, month, year);
    }
}
