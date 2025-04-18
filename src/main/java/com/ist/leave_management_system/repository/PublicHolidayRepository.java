package com.ist.leave_management_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

import com.ist.leave_management_system.model.PublicHoliday;

@Repository
public interface PublicHolidayRepository extends JpaRepository<PublicHoliday, Long> {
    Optional<PublicHoliday> findByDate(LocalDate date);
}
