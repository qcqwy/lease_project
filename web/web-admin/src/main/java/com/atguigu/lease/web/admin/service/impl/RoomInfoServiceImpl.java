package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import kotlin.jvm.internal.CollectionToArray;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Autowired
    RoomInfoMapper roomInfoMapper;
    @Autowired
    ApartmentInfoMapper apartmentInfoMapper;
    @Autowired
    GraphInfoMapper graphInfoMapper;
    @Autowired
    RoomAttrValueMapper roomAttrValueMapper;
    @Autowired
    RoomFacilityMapper roomFacilityMapper;
    @Autowired
    RoomLabelMapper roomLabelMapper;
    @Autowired
    RoomPaymentTypeMapper roomPaymentTypeMapper;
    @Autowired
    RoomLeaseTermMapper roomLeaseTermMapper;
    @Autowired
    GraphInfoService graphInfoService;
    @Autowired
    RoomAttrValueService roomAttrValueService;
    @Autowired
    RoomFacilityService roomFacilityService;
    @Autowired
    RoomLabelService roomLabelService;
    @Autowired
    RoomPaymentTypeService roomPaymentTypeService;
    @Autowired
    RoomLeaseTermService roomLeaseTermService;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Override
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate = roomSubmitVo.getId() != null;
        super.saveOrUpdate(roomSubmitVo);
        Long roomId = roomSubmitVo.getId();
        if(isUpdate){

            //1.移除图片列表
            LambdaQueryWrapper<GraphInfo> graphInfoWrapper = new LambdaQueryWrapper<>();
            graphInfoWrapper.eq(GraphInfo::getItemId, roomId);
            graphInfoWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphInfoService.remove(graphInfoWrapper);
            //2.移除属性信息列表
            LambdaQueryWrapper<RoomAttrValue> roomAttrValueWrapper = new LambdaQueryWrapper<>();
            roomAttrValueWrapper.eq(RoomAttrValue::getRoomId, roomId);
            roomAttrValueService.remove(roomAttrValueWrapper);
            //3.移除配套信息列表
            LambdaQueryWrapper<RoomFacility> roomFacilityWrapper = new LambdaQueryWrapper<>();
            roomFacilityWrapper.eq(RoomFacility::getRoomId, roomId);
            roomFacilityService.remove(roomFacilityWrapper);
            //4.移除标签信息列表
            LambdaQueryWrapper<RoomLabel> roomLabelWrapper = new LambdaQueryWrapper<>();
            roomLabelWrapper.eq(RoomLabel::getRoomId, roomId);
            roomLabelService.remove(roomLabelWrapper);
            //5.移除支付方式列表
            LambdaQueryWrapper<RoomPaymentType> roomPaymentWrapper = new LambdaQueryWrapper<>();
            roomPaymentWrapper.eq(RoomPaymentType::getRoomId, roomId);
            roomPaymentTypeService.remove(roomPaymentWrapper);
            //6.移除可选租期列表
            LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermWrapper = new LambdaQueryWrapper<>();
            roomLeaseTermWrapper.eq(RoomLeaseTerm::getRoomId, roomId);
            roomLeaseTermService.remove(roomLeaseTermWrapper);

            //7.删除缓存
            String key = RedisConstant.APP_ROOM_PREFIX + roomId;
            redisTemplate.delete(key);
        }


        //1.新增图片列表
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)){
            List<GraphInfo> graphInfoList = new ArrayList<>();
            for(GraphVo g: graphVoList){
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomId);
                graphInfo.setUrl(g.getUrl());
                graphInfo.setName(g.getName());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }
        //2.新增属性列表
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if(!CollectionUtils.isEmpty(attrValueIds)){
            List<RoomAttrValue> roomAttrValueList = new ArrayList<>();
            for(Long id : attrValueIds){
                RoomAttrValue roomAttrValue = RoomAttrValue.builder().build();
                roomAttrValue.setRoomId(roomId);
                roomAttrValue.setAttrValueId(id);
                roomAttrValueList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueList);
        }
        //3.新增配套列表
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if(!CollectionUtils.isEmpty(facilityInfoIds)){
            List<RoomFacility> roomFacilities = new ArrayList<>();
            for(Long id : facilityInfoIds){
                RoomFacility roomFacility = RoomFacility.builder().build();
                roomFacility.setFacilityId(id);
                roomFacility.setRoomId(roomId);
                roomFacilities.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilities);
        }
        //4.新增标签列表
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if(!CollectionUtils.isEmpty(labelInfoIds)){
            List<RoomLabel> roomLabels = new ArrayList<>();
            for(Long id : labelInfoIds){
                RoomLabel roomLabel = RoomLabel.builder().build();
                roomLabel.setRoomId(roomId);
                roomLabel.setLabelId(id);
                roomLabels.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabels);
        }
        //5.新增支付方式列表
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if(!CollectionUtils.isEmpty(paymentTypeIds)){
            List<RoomPaymentType> roomPaymentTypes = new ArrayList<>();
            for(Long id: paymentTypeIds){
                RoomPaymentType roomPaymentType = RoomPaymentType.builder().build();
                roomPaymentType.setRoomId(roomId);
                roomPaymentType.setPaymentTypeId(id);
                roomPaymentTypes.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypes);
        }
        //6.新增可选租期列表
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if(!CollectionUtils.isEmpty(leaseTermIds)){
            List<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for(Long id : leaseTermIds){
                RoomLeaseTerm roomLeaseTerm = new RoomLeaseTerm();
                roomLeaseTerm.setRoomId(roomId);
                roomLeaseTerm.setLeaseTermId(id);
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }

    }

    @Override
    public IPage<RoomItemVo> pageItem(IPage<RoomItemVo> iPage, RoomQueryVo queryVo) {
        IPage<RoomItemVo> ipage = roomInfoMapper.pageItem(iPage, queryVo);
        return  iPage;
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        //1.房间信息
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        if(roomInfo != null){
            //2.公寓信息
            ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());
            //3.图片列表
            List<GraphVo> graphVoList = graphInfoMapper.selectByRoomId(ItemType.ROOM,id);
            //4.属性列表
            List<AttrValueVo> attrValueVoList = roomAttrValueMapper.selectByRoomId(id);
            //5.配套列表
            List<FacilityInfo> facilityInfoList = roomFacilityMapper.selectByRoomId(id);
            //6.标签列表
            List<LabelInfo> labelInfoList = roomLabelMapper.selectByRoomId(id);
            //7.支付方式列表
            List<PaymentType> paymentTypeList = roomPaymentTypeMapper.selectByRoomId(id);
            //8.可选租期列表
            List<LeaseTerm> leaseTermList = roomLeaseTermMapper.selectByRoomId(id);

            RoomDetailVo roomDetailVo = new RoomDetailVo();
            BeanUtils.copyProperties(roomInfo, roomDetailVo);
            roomDetailVo.setApartmentInfo(apartmentInfo);
            roomDetailVo.setFacilityInfoList(facilityInfoList);
            roomDetailVo.setLabelInfoList(labelInfoList);
            roomDetailVo.setGraphVoList(graphVoList);
            roomDetailVo.setPaymentTypeList(paymentTypeList);
            roomDetailVo.setLeaseTermList(leaseTermList);
            roomDetailVo.setAttrValueVoList(attrValueVoList);
            return  roomDetailVo;
        }
        return null;

    }

    @Override
    public void removeRoomById(Long id) {
        super.removeById(id);
        //1.移除图片列表
        LambdaQueryWrapper<GraphInfo> graphInfoWrapper = new LambdaQueryWrapper<>();
        graphInfoWrapper.eq(GraphInfo::getItemId, id);
        graphInfoWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        graphInfoService.remove(graphInfoWrapper);
        //2.移除属性信息列表
        LambdaQueryWrapper<RoomAttrValue> roomAttrValueWrapper = new LambdaQueryWrapper<>();
        roomAttrValueWrapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(roomAttrValueWrapper);
        //3.移除配套信息列表
        LambdaQueryWrapper<RoomFacility> roomFacilityWrapper = new LambdaQueryWrapper<>();
        roomFacilityWrapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(roomFacilityWrapper);
        //4.移除标签信息列表
        LambdaQueryWrapper<RoomLabel> roomLabelWrapper = new LambdaQueryWrapper<>();
        roomLabelWrapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(roomLabelWrapper);
        //5.移除支付方式列表
        LambdaQueryWrapper<RoomPaymentType> roomPaymentWrapper = new LambdaQueryWrapper<>();
        roomPaymentWrapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(roomPaymentWrapper);
        //6.移除可选租期列表
        LambdaQueryWrapper<RoomLeaseTerm> roomLeaseTermWrapper = new LambdaQueryWrapper<>();
        roomLeaseTermWrapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(roomLeaseTermWrapper);
        //7.删除redis缓存
        redisTemplate.delete(RedisConstant.APP_ROOM_PREFIX+id);
    }
}




