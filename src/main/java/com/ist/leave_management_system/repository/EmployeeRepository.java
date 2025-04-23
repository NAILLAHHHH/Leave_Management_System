package com.ist.leave_management_system.repository;

import com.ist.leave_management_system.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ist.leave_management_system.model.UserRole;
import java.util.List;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmail(String email);  // To find user by email
    
    boolean existsByEmail(String email); // To check if email already exists

    List<Employee> findByRole(UserRole role);
}
