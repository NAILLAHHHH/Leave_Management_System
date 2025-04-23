package com.ist.leave_management_system.service;

import com.ist.leave_management_system.model.Employee;
import java.util.Optional;
import java.util.List;

public interface EmployeeService {
    Optional<Employee> getEmployeeById(Long id);
    Optional<Employee> findByEmail(String email);
    List<Employee> findAdminEmployees();
}
