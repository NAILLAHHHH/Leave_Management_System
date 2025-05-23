package com.ist.leave_management_system.service.impl;

import com.ist.leave_management_system.model.Employee;
import com.ist.leave_management_system.model.UserRole;
import com.ist.leave_management_system.repository.EmployeeRepository;
import com.ist.leave_management_system.repository.UserRoleRepository;
import com.ist.leave_management_system.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    @Override
    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    @Override
    public List<Employee> findAdminEmployees() {
        UserRole adminRole = userRoleRepository.findByRoleName("ADMIN");
        return employeeRepository.findByRole(adminRole);
    }
} 