package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.ApartmentInfoMapper;
import com.atguigu.lease.web.app.mapper.FacilityInfoMapper;
import com.atguigu.lease.web.app.mapper.GraphInfoMapper;
import com.atguigu.lease.web.app.mapper.LabelInfoMapper;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    LabelInfoMapper labelInfoMapper;
    @Autowired
    GraphInfoMapper graphInfoMapper;
    @Autowired
    FacilityInfoMapper facilityInfoMapper;
    @Override
    public ApartmentItemVo selectApartmentItemVoById(Long id) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, id);
        BigDecimal minRent = apartmentInfoMapper.selectMinRentByApartmentId(id);
        ApartmentItemVo res = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo, res);
        res.setGraphVoList(graphVoList);
        res.setLabelInfoList(labelInfoList);
        res.setMinRent(minRent);
        return res;
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if(apartmentInfo == null){
            return null;
        }
        List<GraphVo> graphVos = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, id);
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByApartmentId(id);
        BigDecimal minRent = apartmentInfoMapper.selectMinRentByApartmentId(id);
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVos);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setMinRent(minRent);
        return apartmentDetailVo;
    }
}




