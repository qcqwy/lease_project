package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.LeaseAgreementService;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.vo.agreement.AgreementDetailVo;
import com.atguigu.lease.web.app.vo.agreement.AgreementItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {
    @Autowired
    LeaseAgreementMapper leaseAgreementMapper;
    @Autowired
    ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    RoomInfoMapper roomInfoMapper;
    @Autowired
    GraphInfoMapper graphInfoMapper;
    @Autowired
    PaymentTypeMapper paymentTypeMapper;
    @Autowired
    LeaseTermMapper leaseTermMapper;
    @Override
    public List<AgreementItemVo> listItemByPhone(String phone) {
        return leaseAgreementMapper.listItemByPhone(phone);

    }

    @Override
    public AgreementDetailVo getDetailById(Long id) {
        //1.查询租约信息
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        if(leaseAgreement == null){
            return null;
        }
        //2.查询公寓信息
        Long apartmentId = leaseAgreement.getApartmentId();
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(apartmentId);
        //3.查询房间信息
        Long roomId = leaseAgreement.getRoomId();
        RoomInfo roomInfo = roomInfoMapper.selectById(roomId);
        //4.查询公寓图片信息
        List<GraphVo> graphApartmentVos = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, apartmentId);
        List<GraphVo> graphRoomVos = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, roomId);
        //5.查询支付方式
        PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getPaymentTypeId());
        //6.查询租期
        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());


        AgreementDetailVo agreementDetailVo = new AgreementDetailVo();
        BeanUtils.copyProperties(leaseAgreement, agreementDetailVo);
        agreementDetailVo.setApartmentName(apartmentInfo.getName());
        agreementDetailVo.setApartmentGraphVoList(graphApartmentVos);
        agreementDetailVo.setRoomNumber(roomInfo.getRoomNumber());
        agreementDetailVo.setRoomGraphVoList(graphRoomVos);
        agreementDetailVo.setPaymentTypeName(paymentType.getName());
        agreementDetailVo.setLeaseTermMonthCount(leaseTerm.getMonthCount());
        agreementDetailVo.setLeaseTermUnit(leaseTerm.getUnit());

        return agreementDetailVo;

    }
}




