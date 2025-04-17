package com.ist.leave_management_system;

import com.ist.leave_management_system.model.UserRole;
import com.ist.leave_management_system.repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRoleRepository userRoleRepository;

    public DataSeeder(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRoleRepository.count() == 0) {
            userRoleRepository.save(UserRole.builder().roleName("ADMIN").description("Full access").build());
            userRoleRepository.save(UserRole.builder().roleName("MANAGER").description("Approve leave requests").build());
            userRoleRepository.save(UserRole.builder().roleName("STAFF").description("Regular staff access").build());
            System.out.println("✅ Default roles seeded.");
        }
    }
}
