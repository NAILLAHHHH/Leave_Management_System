package com.ist.leave_management_system.repository;

import com.ist.leave_management_system.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmail(String email);  // To find user by email
    
    boolean existsByEmail(String email); // To check if email already exists
}
