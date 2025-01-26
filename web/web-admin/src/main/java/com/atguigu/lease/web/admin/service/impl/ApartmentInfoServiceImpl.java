package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.controller.apartment.RegionInfoController;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import kotlin.jvm.internal.Lambda;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.databind.type.LogicalType.Collection;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {
    @Autowired
    ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    GraphInfoMapper graphInfoMapper;
    @Autowired
    LabelInfoMapper labelInfoMapper;
    @Autowired
    FacilityInfoMapper facilityInfoMapper;
    @Autowired
    FeeValueMapper feeValueMapper;
    @Autowired
    ProvinceInfoService provinceInfoService;
    @Autowired
    CityInfoService cityInfoService;
    @Autowired
    DistrictInfoService districtInfoService;
    @Autowired
    RoomInfoService roomInfoService;
    @Autowired
    GraphInfoService graphInfoService;
    @Autowired
    ApartmentFacilityService facilityService;
    @Autowired
    ApartmentLabelService labelService;
    @Autowired
    ApartmentFeeValueService feeValueService;

    @Override
    public boolean saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        //saveOrUpdate在对数据进行保存或更新后，会给原来的对象进行数据回显，添加上id
        //所以判断是保存或更新要先进行id是否为null判断，再进行保存或更新
        //获取地区名称
        LambdaQueryWrapper<DistrictInfo> districtInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Long districtId = apartmentSubmitVo.getDistrictId();
        districtInfoLambdaQueryWrapper.eq(DistrictInfo::getId, districtId);
        String districtName = districtInfoService.getOne(districtInfoLambdaQueryWrapper).getName();
        apartmentSubmitVo.setDistrictName(districtName);
        //获取城市名称
        LambdaQueryWrapper<CityInfo> cityInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Long cityId = apartmentSubmitVo.getCityId();
        cityInfoLambdaQueryWrapper.eq(CityInfo::getId, cityId);
        String cityName = cityInfoService.getOne(cityInfoLambdaQueryWrapper).getName();
        apartmentSubmitVo.setCityName(cityName);
        //获取省名称
        LambdaQueryWrapper<ProvinceInfo>  provinceInfoLambdaQueryWrapper= new LambdaQueryWrapper<>();
        Long provinceId = apartmentSubmitVo.getProvinceId();
        provinceInfoLambdaQueryWrapper.eq(ProvinceInfo::getId, provinceId);
        String provinceName = provinceInfoService.getOne(provinceInfoLambdaQueryWrapper).getName();
        apartmentSubmitVo.setProvinceName(provinceName);

        boolean isUpdate = apartmentSubmitVo.getId() != null;
        boolean b = super.saveOrUpdate(apartmentSubmitVo);
        //若是保存公寓信息，则需要删除原有的公寓与配套、标签、图片、杂费的信息
        if(isUpdate){
            //1.删除图片列表
            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId, apartmentSubmitVo.getId());
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoService.remove(graphInfoLambdaQueryWrapper);
            //2.删除配套列表
            LambdaQueryWrapper<ApartmentFacility> FacilityWrapper = new LambdaQueryWrapper<>();
            FacilityWrapper.eq(ApartmentFacility::getApartmentId, apartmentSubmitVo.getId());
            facilityService.remove(FacilityWrapper);
            //3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> LabelWrapper = new LambdaQueryWrapper<>();
            LabelWrapper.eq(ApartmentLabel::getApartmentId, apartmentSubmitVo.getId());
            labelService.remove(LabelWrapper);
            //4.删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> FeeWrapper = new LambdaQueryWrapper<>();
            FeeWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentSubmitVo.getId());
            feeValueService.remove(FeeWrapper);
        }
        //新增列表
        //1.新增图片列表
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            List<GraphInfo> graphList = new ArrayList<>();
            for(GraphVo graph: graphVoList){
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setName(graph.getName());
                graphInfo.setUrl(graph.getUrl());
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setItemType(ItemType.APARTMENT);
                graphList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphList);
        }
        //2.新增配套列表
        List<Long> facilityIds = apartmentSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(apartmentSubmitVo.getFacilityInfoIds())){
            List<ApartmentFacility> facilityList = new ArrayList<>();
            for(Long id : facilityIds){
                ApartmentFacility apartmentFacility = ApartmentFacility.builder().build();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(id);
                facilityList.add(apartmentFacility);
            }
            facilityService.saveBatch(facilityList);
        }
        //3.新增标签列表
        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        if(!CollectionUtils.isEmpty(labelIds)){
            List<ApartmentLabel> labelInfoList = new ArrayList<>();
            for(Long id: labelIds){
                ApartmentLabel apartmentLabel = ApartmentLabel.builder().build();
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                apartmentLabel.setLabelId(id);
                labelInfoList.add(apartmentLabel);
            }
            labelService.saveBatch(labelInfoList);
        }
        //4.新增杂费列表
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if(!CollectionUtils.isEmpty(feeValueIds)){
            List<ApartmentFeeValue> feeValueList = new ArrayList<>();
            for(Long id: feeValueIds){
                ApartmentFeeValue feeValue = ApartmentFeeValue.builder().build();
                feeValue.setApartmentId(apartmentSubmitVo.getId());
                feeValue.setFeeValueId(id);
                feeValueList.add(feeValue);
            }
            feeValueService.saveBatch(feeValueList);
        }
        return b;

    }

    @Override
    public IPage<ApartmentItemVo> pageApartmentItemByQuery(IPage<ApartmentItemVo> iPage, ApartmentQueryVo queryVo) {
        IPage<ApartmentItemVo> list = apartmentInfoMapper.pageApartmentItemByQuery(iPage, queryVo);
        return list;
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //1.查询公寓信息,若公寓存在，则接着查询，否则直接return
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if(apartmentInfo != null){
            //2.查询图片列表
            List<GraphVo> graphVoList =  graphInfoMapper.selectByItemTypeAndId(ItemType.APARTMENT ,id);
            //3.查询标签列表
            List<LabelInfo> labelInfoList = labelInfoMapper.selectByApartmentId(id);
            //4.查询配套列表
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectByApartmentId(id);
            //5.查询杂费列表
            List<FeeValueVo> feeValueVoList = feeValueMapper.selectByApartmentId(id);
            //6.封装所有信息到ApartmentDetailVo对象里
            ApartmentDetailVo vo = new ApartmentDetailVo();
            BeanUtils.copyProperties(apartmentInfo, vo);
            vo.setFacilityInfoList(facilityInfoList);
            vo.setLabelInfoList(labelInfoList);
            vo.setGraphVoList(graphVoList);
            vo.setFeeValueVoList(feeValueVoList);
            return vo;
        }
        return null;
    }
    @Override
    public void removeApartmentById(Long id) {
        //0.判断该公寓下方是否还存在房间
        LambdaQueryWrapper<RoomInfo> roomInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoLambdaQueryWrapper.eq(RoomInfo::getApartmentId, id);
        long count = roomInfoService.count(roomInfoLambdaQueryWrapper);
        if(count > 0){
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }
        else{
            boolean b = super.removeById(id);

            //1.删除图片列表
            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId, id);
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoService.remove(graphInfoLambdaQueryWrapper);
            //2.删除配套列表
            LambdaQueryWrapper<ApartmentFacility> FacilityWrapper = new LambdaQueryWrapper<>();
            FacilityWrapper.eq(ApartmentFacility::getApartmentId, id);
            facilityService.remove(FacilityWrapper);
            //3.删除标签列表
            LambdaQueryWrapper<ApartmentLabel> LabelWrapper = new LambdaQueryWrapper<>();
            LabelWrapper.eq(ApartmentLabel::getApartmentId, id);
            labelService.remove(LabelWrapper);
            //4.删除杂费列表
            LambdaQueryWrapper<ApartmentFeeValue> FeeWrapper = new LambdaQueryWrapper<>();
            FeeWrapper.eq(ApartmentFeeValue::getApartmentId, id);
            feeValueService.remove(FeeWrapper);
        }
    }

}




