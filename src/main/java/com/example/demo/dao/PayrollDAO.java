package com.example.demo.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PayrollDAO {

    private final JdbcTemplate jdbcTemplate;

    public PayrollDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Tính toán lương trực tiếp trong SQL (có khấu trừ thuế và bảo hiểm)
     * và lưu vào bảng Payroll, đồng thời trả dữ liệu hiển thị ra HTML phiếu lương.
     */
    public Map<String, Object> calculateAndSavePayroll(String employeeId, int month, int year) {

        // Giả định: thuế 10%, bảo hiểm 5%
        double taxRate = 0.10;
        double insuranceRate = 0.05;
        double totalDeductionRate = taxRate + insuranceRate;

        // SQL: lấy thông tin nhân viên + chấm công + tính lương
        String sql = """
            SELECT 
                e.employee_id,
                e.full_name,
                e.gender,
                e.date_of_birth,
                e.phone,
                e.email,
                e.employment_contract,
                e.basic_salary AS basic_salary,
                d.name AS department,
                p.position_name AS position,

                -- Chấm công
                COALESCE(SUM(CASE WHEN tk.work_status = 'Đi làm' THEN 1 ELSE 0 END), 0) AS work_days,
                COALESCE(SUM(CASE WHEN tk.work_status = 'Nghỉ phép' THEN 1 ELSE 0 END), 0) AS leave_days,
                COALESCE(SUM(CASE WHEN tk.work_status = 'Vắng mặt' THEN 1 ELSE 0 END), 0) AS absent_days,
                COALESCE(SUM(tk.overtime), 0) AS total_overtime,

                -- Khoản mặc định
                1000000 AS allowance,

                -- Tổng lương trước khấu trừ (gross)
                ((COALESCE(SUM(CASE WHEN tk.work_status = 'Đi làm' THEN 1 ELSE 0 END),0)*(e.basic_salary/22)
                  + COALESCE(SUM(tk.overtime),0)*(e.basic_salary/176*1.5)
                  + 1000000
                  - (COALESCE(SUM(CASE WHEN tk.work_status = 'Nghỉ phép' THEN 1 ELSE 0 END),0)*(e.basic_salary/22*0.5)
                     + COALESCE(SUM(CASE WHEN tk.work_status = 'Vắng mặt' THEN 1 ELSE 0 END),0)*(e.basic_salary/22))
                 ) * p.salary_coefficient
                ) AS gross_salary,

                -- Khấu trừ (thuế + bảo hiểm)
                ((COALESCE(SUM(CASE WHEN tk.work_status = 'Đi làm' THEN 1 ELSE 0 END),0)*(e.basic_salary/22)
                  + COALESCE(SUM(tk.overtime),0)*(e.basic_salary/176*1.5)
                  + 1000000
                  - (COALESCE(SUM(CASE WHEN tk.work_status = 'Nghỉ phép' THEN 1 ELSE 0 END),0)*(e.basic_salary/22*0.5)
                     + COALESCE(SUM(CASE WHEN tk.work_status = 'Vắng mặt' THEN 1 ELSE 0 END),0)*(e.basic_salary/22))
                 ) * p.salary_coefficient
                 ) * ? AS deduction,

                -- Lương thực lĩnh (net)
                ((COALESCE(SUM(CASE WHEN tk.work_status = 'Đi làm' THEN 1 ELSE 0 END),0)*(e.basic_salary/22)
                  + COALESCE(SUM(tk.overtime),0)*(e.basic_salary/176*1.5)
                  + 1000000
                  - (COALESCE(SUM(CASE WHEN tk.work_status = 'Nghỉ phép' THEN 1 ELSE 0 END),0)*(e.basic_salary/22*0.5)
                     + COALESCE(SUM(CASE WHEN tk.work_status = 'Vắng mặt' THEN 1 ELSE 0 END),0)*(e.basic_salary/22))
                 ) * p.salary_coefficient
                 ) * (1 - ?) AS net_salary

            FROM Employee e
            LEFT JOIN Department d ON e.department_id = d.department_id
            LEFT JOIN Position p ON e.position_id = p.position_id
            LEFT JOIN Timekeeping tk 
                   ON e.employee_id = tk.employee_id 
                  AND MONTH(tk.work_date) = ? 
                  AND YEAR(tk.work_date) = ?
            WHERE e.employee_id = ?
            GROUP BY e.employee_id, e.full_name, e.gender, e.date_of_birth, e.phone, e.email,
                     e.employment_contract, e.basic_salary, d.name, p.position_name, p.salary_coefficient
        """;

        Map<String, Object> result;
        try {
            result = jdbcTemplate.queryForMap(
                sql,
                totalDeductionRate,  // deduction
                totalDeductionRate,  // net_salary = gross*(1 - deduction)
                month,
                year,
                employeeId
            );
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Nhân viên không tồn tại hoặc chưa có chấm công cho tháng/năm này");
        }

        // Lấy dữ liệu từ SQL kết quả (tránh NPE)
        double basicSalary   = result.get("basic_salary")   != null ? ((Number) result.get("basic_salary")).doubleValue() : 0;
        double workDays      = result.get("work_days")      != null ? ((Number) result.get("work_days")).doubleValue() : 0;
        double leaveDays     = result.get("leave_days")     != null ? ((Number) result.get("leave_days")).doubleValue() : 0;
        double absentDays    = result.get("absent_days")    != null ? ((Number) result.get("absent_days")).doubleValue() : 0;
        double totalOvertime = result.get("total_overtime") != null ? ((Number) result.get("total_overtime")).doubleValue() : 0;
        double allowance     = result.get("allowance")      != null ? ((Number) result.get("allowance")).doubleValue() : 0;
        double grossSalary   = result.get("gross_salary")   != null ? ((Number) result.get("gross_salary")).doubleValue() : 0;
        double deduction     = result.get("deduction")      != null ? ((Number) result.get("deduction")).doubleValue() : 0;
        double netSalary     = result.get("net_salary")     != null ? ((Number) result.get("net_salary")).doubleValue() : 0;

        // Tính tiền tăng ca riêng
        double overtimePay = totalOvertime * (basicSalary / 176 * 1.5);

        // Tạo ID phiếu lương (VD: NV001202411)
        String payrollId = employeeId + String.format("%04d%02d", year, month);

        // Lưu dữ liệu chính vào bảng Payroll
        String insertSql = """
            INSERT INTO Payroll (payroll_id, employee_id, month, year, allowance, overtime_pay, deduction, total_salary)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                allowance = VALUES(allowance),
                overtime_pay = VALUES(overtime_pay),
                deduction = VALUES(deduction),
                total_salary = VALUES(total_salary)
        """;

        jdbcTemplate.update(
            insertSql,
            payrollId,
            employeeId,
            month,
            year,
            allowance,
            overtimePay,
            deduction,
            netSalary
        );

        // Tạo Map trả về cho HTML
        Map<String, Object> payrollResult = new HashMap<>();
        payrollResult.putAll(result);
        payrollResult.put("payroll_id", payrollId);
        payrollResult.put("overtime_pay", overtimePay);
        payrollResult.put("deduction", deduction);
        payrollResult.put("net_salary", netSalary);
        payrollResult.put("gross_salary", grossSalary);

        // Thêm các giá trị cần hiển thị lên HTML phiếu lương
        payrollResult.put("work_days", workDays);
        payrollResult.put("leave_days", leaveDays);
        payrollResult.put("absent_days", absentDays);

        // Thêm tháng và năm để hiển thị trên HTML
        payrollResult.put("month", month);
        payrollResult.put("year", year);

        return payrollResult;
    }
}
