package com.atguigu.lease.web.admin.controller.apartment;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.admin.service.RoomInfoService;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.jdbc.SQL;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@Tag(name = "房间信息管理")
@RestController
@RequestMapping("/admin/room")
public class RoomController {
    @Autowired
    RoomInfoService roomInfoService;

    @Operation(summary = "保存或更新房间信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody RoomSubmitVo roomSubmitVo) {
        roomInfoService.saveOrUpdateRoom(roomSubmitVo);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询房间列表")
    @GetMapping("pageItem")
    public Result<IPage<RoomItemVo>> pageItem(@RequestParam long current, @RequestParam long size, RoomQueryVo queryVo) {
        IPage<RoomItemVo> iPage = new Page<>(current, size);
        IPage<RoomItemVo> vo = roomInfoService.pageItem(iPage, queryVo);
        return Result.ok(vo);
    }

    @Operation(summary = "根据id获取房间详细信息")
    @GetMapping("getDetailById")
    public Result<RoomDetailVo> getDetailById(@RequestParam Long id) {
        RoomDetailVo vo = roomInfoService.getDetailById(id);
        return Result.ok(vo);
    }

    @Operation(summary = "根据id删除房间信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        //使用触发器
        LambdaUpdateWrapper<RoomInfo> wrapper = new LambdaUpdateWrapper();
        wrapper.set(RoomInfo::getIsDeleted, 1);
        wrapper.eq(RoomInfo::getId, id);
        try{
            roomInfoService.update(wrapper);
        }catch (Exception e){
            throw e;
//            if(e.getCause() instanceof SQLException){
//                SQLException sqlException = (SQLException) e.getCause();
//                return Result.build(201, sqlException.getMessage());
//            }
        }
        return Result.ok();

//        roomInfoService.removeRoomById(id);
//        return Result.ok();
    }

    @Operation(summary = "根据id修改房间发布状态")
    @PostMapping("updateReleaseStatusById")
    public Result updateReleaseStatusById(Long id, ReleaseStatus status) {
        LambdaUpdateWrapper<RoomInfo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RoomInfo::getId, id);
        wrapper.set(RoomInfo::getIsRelease, status);
        try{
            roomInfoService.update(wrapper);
        }
        catch(Exception e){
            if(e.getCause() instanceof SQLException){
                SQLException sqlException = (SQLException)e.getCause();
                return Result.build(201, sqlException.getMessage());
            }
            return Result.fail();
        }
        return Result.ok();
    }

    @GetMapping("listBasicByApartmentId")
    @Operation(summary = "根据公寓id查询房间列表")
    public Result<List<RoomInfo>> listBasicByApartmentId(Long id) {
        LambdaQueryWrapper<RoomInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomInfo::getApartmentId, id);
        List<RoomInfo> list = roomInfoService.list(wrapper);
        return Result.ok(list);
    }

}


















