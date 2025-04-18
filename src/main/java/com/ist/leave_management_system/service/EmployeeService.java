package com.ist.leave_management_system.service;

import com.ist.leave_management_system.model.Employee;
import java.util.Optional;

public interface EmployeeService {
    Optional<Employee> getEmployeeById(Long id);
    Optional<Employee> findByEmail(String email);
}
