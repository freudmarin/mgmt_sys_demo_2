package com.marindulja.mgmt_sys_demo_2.services;

import com.marindulja.mgmt_sys_demo_2.models.Repair;
import com.marindulja.mgmt_sys_demo_2.models.RepairStatus;
import com.marindulja.mgmt_sys_demo_2.repositories.IRepairRepository;
import com.marindulja.mgmt_sys_demo_2.repositories.ITechnicianRepository;
import com.marindulja.mgmt_sys_demo_2.specifications.CustomRepairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TechnicianService implements ITechnicianService {

    private final IRepairRepository repairRepository;
    private final ITechnicianRepository technicianRepository;
    private final CustomRepairRepository spec;
    @Override
    public List<Repair> viewPendingRepairs() {
        return repairRepository.findAll(spec.byStatus2(RepairStatus.PENDING));
    }

    @Override
    public List<Repair> viewAcceptedRepairs() {
        // get the technician from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long techId = Long.valueOf(user.getUsername());
        return repairRepository.findAll(spec.byStatusAndTechnicianId(RepairStatus.IN_PROGRESS, techId));
    }

    // shkruaj unit test
    @Override
    public void acceptRepair(Long repairId) {
        Repair repair = repairRepository.findById(repairId).get();
        repair.setStatus(RepairStatus.IN_PROGRESS);
        // get the technician from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long techId = Long.valueOf(user.getUsername());
        repair.setTechnician(technicianRepository.findById(techId).get());
        repairRepository.save(repair);
    }

    @Override
    public void rejectRepair(Long repairId, String reason) {
        Repair repair = repairRepository.findById(repairId).get();
        repair.setStatus(RepairStatus.CANCELED);
        repair.setRepairNotes(reason);
        repairRepository.save(repair);
    }

    @Override
    public void completeRepair(Long repairId, double price) {
        Repair repair = repairRepository.findById(repairId).get();
        repair.setStatus(RepairStatus.COMPLETED);
        if (new Date().after(repair.getWarrantyExpireDate())) {
            repair.setPrice(price);
        }
        repairRepository.save(repair);
    }
}
