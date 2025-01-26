/**
 * @program: lease
 * @description: 设置定时任务，用于租期到期状态更新等
 * @author: LDY
 * @create: 2025-01-17 21:34
 **/
package com.atguigu.lease.web.admin.schedule;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.rmi.dgc.Lease;
import java.util.Date;

@Component
public class ScheduleTasks {

    @Autowired
    LeaseAgreementService service;
    @Scheduled(cron = "0 0 0 * * *")
    public void checkLeaseStatus(){
        LambdaUpdateWrapper<LeaseAgreement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.le(LeaseAgreement::getLeaseEndDate, new Date());
        wrapper.in(LeaseAgreement::getStatus, LeaseStatus.SIGNED, LeaseStatus.WITHDRAWING);
        wrapper.set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED);
        service.update(wrapper);
    }
}
