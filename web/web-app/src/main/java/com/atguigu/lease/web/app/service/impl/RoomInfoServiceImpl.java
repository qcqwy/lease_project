package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.attr.AttrValueVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {
    @Autowired
    RoomInfoMapper roomInfoMapper;
    @Autowired
    GraphInfoMapper graphInfoMapper;
    @Autowired
    AttrValueMapper attrValueMapper;
    @Autowired
    LeaseTermMapper leaseTermMapper;
    @Autowired
    FacilityInfoMapper facilityInfoMapper;
    @Autowired
    LabelInfoMapper labelInfoMapper;
    @Autowired
    PaymentTypeMapper paymentTypeMapper;
    @Autowired
    FeeValueMapper feeValueMapper;
    @Autowired
    ApartmentInfoService apartmentInfoService;
    @Autowired
    BrowsingHistoryService browsingHistoryService;
    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Override
    public IPage<RoomItemVo> pageItem(IPage<RoomItemVo> page, RoomQueryVo queryVo) {
        IPage<RoomItemVo> res =  roomInfoMapper.pageItem(page, queryVo);
        return res;
    }

    @Override
    public RoomDetailVo getDetailById(Long id) {
        String key = RedisConstant.APP_ROOM_PREFIX + id;
        RoomDetailVo roomDetailVo = (RoomDetailVo) redisTemplate.opsForValue().get(key);
        //判断房间信息是否在redis中
        if(roomDetailVo == null){
            //1.查询房间信息
            RoomInfo roomInfo = roomInfoMapper.selectById(id);
            if(roomInfo == null){
                return null;
            }
            //2.查询图片
            List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, id);
            //3.查询租期
            List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);
            //4.查询配套
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);
            //5.查询标签
            List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);
            //6.查询支付方式
            List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);
            //7.查询基本属性
            List<AttrValueVo> attrValueVoList = attrValueMapper.selectListByRoomId(id);
            //8.查询杂费信息
            List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(roomInfo.getApartmentId());
            //9.查询公寓信息
            ApartmentItemVo apartmentItemVo = apartmentInfoService.selectApartmentItemVoById(roomInfo.getApartmentId());


            roomDetailVo = new RoomDetailVo();
            BeanUtils.copyProperties(roomInfo, roomDetailVo);
            roomDetailVo.setGraphVoList(graphVoList);
            roomDetailVo.setLeaseTermList(leaseTermList);
            roomDetailVo.setFacilityInfoList(facilityInfoList);
            roomDetailVo.setLabelInfoList(labelInfoList);
            roomDetailVo.setPaymentTypeList(paymentTypeList);
            roomDetailVo.setAttrValueVoList(attrValueVoList);
            roomDetailVo.setFeeValueVoList(feeValueVoList);
            roomDetailVo.setApartmentItemVo(apartmentItemVo);

            //若redis中不存在则再保存
            redisTemplate.opsForValue().set(key, roomDetailVo);

        }
        //10.保存浏览信息
        browsingHistoryService.saveHistory(LoginUserHolder.getLoginUser().getUserId(), id);

        return roomDetailVo;
    }

    @Override
    public IPage<RoomItemVo> pageItemByApartmentId(IPage<RoomItemVo> page, Long id) {
        IPage<RoomItemVo> res = roomInfoMapper.pageItemByApartmentId(page, id);
        return res;
    }
}




