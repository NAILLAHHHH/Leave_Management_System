package com.ist.leave_management_system.service;

import com.ist.leave_management_system.model.LeaveType;
import java.util.List;

public interface LeaveTypeService {
    LeaveType createLeaveType(LeaveType leaveType);
    List<LeaveType> getAllLeaveTypes();
    LeaveType getLeaveTypeById(Long id);
    LeaveType updateLeaveType(Long id, LeaveType leaveType);
    void deleteLeaveType(Long id);
}
