/**
 * @program: lease
 * @description: 字符串转ItemType转换器，用于客户端向服务器传参的参数类型转换
 * @author: LDY
 * @create: 2024-12-26 20:39
 **/
package com.atguigu.lease.web.admin.custom.convert;

import com.atguigu.lease.model.enums.ItemType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToItemTypeConvert implements Converter <String, ItemType>{
    @Override
    public ItemType convert(String code) {
        ItemType[] values = ItemType.values();
        for(ItemType value: values){
            if(value.getCode().equals(Integer.valueOf(code))){
                return value;
            }
        }
        throw new IllegalArgumentException("code非法"+code);
    }
}
