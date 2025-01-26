package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.ApartmentInfoMapper;
import com.atguigu.lease.web.app.mapper.GraphInfoMapper;
import com.atguigu.lease.web.app.mapper.LabelInfoMapper;
import com.atguigu.lease.web.app.mapper.ViewAppointmentMapper;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.ViewAppointmentService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {
    @Autowired
    ViewAppointmentMapper viewAppointmentMapper;
    @Autowired
    ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    GraphInfoMapper graphInfoMapper;
    @Autowired
    LabelInfoMapper labelInfoMapper;
    @Autowired
    ApartmentInfoService apartmentInfoService;
    @Override
    public List<AppointmentItemVo> listItem(Long id) {
        List<AppointmentItemVo> res = viewAppointmentMapper.listItem(id);
        System.out.println("公寓图片"+res.get(0).getGraphVoList());
        return res;
    }

    @Override
    public AppointmentDetailVo getDetailById(Long id) {
        ViewAppointment viewAppointment = viewAppointmentMapper.selectById(id);
        if(viewAppointment == null){
            return null;
        }

        ApartmentItemVo apartmentItemVo = apartmentInfoService.selectApartmentItemVoById(viewAppointment.getApartmentId());

        AppointmentDetailVo detailVo = new AppointmentDetailVo();
        BeanUtils.copyProperties(viewAppointment, detailVo);
        detailVo.setApartmentItemVo(apartmentItemVo);
        return detailVo;
    }
}




