package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.BrowsingHistory;
import com.atguigu.lease.web.app.mapper.BrowsingHistoryMapper;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.vo.history.HistoryItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author liubo
 * @description 针对表【browsing_history(浏览历史)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class BrowsingHistoryServiceImpl extends ServiceImpl<BrowsingHistoryMapper, BrowsingHistory>
        implements BrowsingHistoryService {
    @Autowired
    BrowsingHistoryMapper browsingHistoryMapper;
    @Override
    public IPage<HistoryItemVo> pageHistoryItemVo(IPage<HistoryItemVo> page, Long id) {
        IPage<HistoryItemVo> res = browsingHistoryMapper.pageHistoryItemVo(page, id);
        return res;
    }
    @Async
    @Override
    public void saveHistory(Long userId, Long roomId) {

        LambdaQueryWrapper<BrowsingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowsingHistory::getUserId, userId);
        wrapper.eq(BrowsingHistory::getRoomId, roomId);
        //查询该用户是否已经浏览过该房间
        BrowsingHistory selected = browsingHistoryMapper.selectOne(wrapper);
        if(selected == null){
            //未浏览则构建浏览历史对象并插入
            BrowsingHistory history = new BrowsingHistory();
            history.setUserId(userId);
            history.setRoomId(roomId);
            history.setBrowseTime(new Date());
            browsingHistoryMapper.insert(history);
        }else{
            //已浏览则在查询到的对象基础上进行更新
            selected.setBrowseTime(new Date());
            browsingHistoryMapper.updateById(selected);
        }


    }
}