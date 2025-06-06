package com.atguigu.lease.web.admin.controller.apartment;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.service.LabelInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "标签管理")
@RestController
@RequestMapping("/admin/label")
public class LabelController {
    @Autowired
    LabelInfoService labelInfoService;

    @Operation(summary = "（根据类型）查询标签列表")
    @GetMapping("list")
    public Result<List<LabelInfo>> labelList(@RequestParam(required = false) ItemType type) {
        //1.传入的参数为枚举类，对应所查寻的标签数据为房间还是公寓，或者两者都要（无参情况）。
        //2.由于传入的数据是可有可无的，所以在查询时，要进行条件判断
        LambdaQueryWrapper<LabelInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(type!=null, LabelInfo::getType, type);
        List<LabelInfo> list = labelInfoService.list(wrapper);
        return Result.ok(list);
    }

    @Operation(summary = "新增或修改标签信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdateLabel(@RequestBody LabelInfo labelInfo) {
        boolean b = labelInfoService.saveOrUpdate(labelInfo);
        return b ? Result.ok() : Result.fail();
    }

    @Operation(summary = "根据id删除标签信息")
    @DeleteMapping("deleteById")
    public Result deleteLabelById(@RequestParam Long id) {
        boolean b = labelInfoService.removeById(id);
        return b ? Result.ok() : Result.fail();
    }
}
