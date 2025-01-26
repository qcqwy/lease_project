/**
 * @program: lease
 * @description:String转BaseEnum枚举类的转换器工厂
 * @author: LDY
 * @create: 2024-12-26 21:03
 **/
package com.atguigu.lease.web.admin.custom.convert;

import com.atguigu.lease.model.enums.BaseEnum;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class StringToBaseEnumConvertFactory implements ConverterFactory<String, BaseEnum> {
    /*
     * 因为该项目我们有很多的枚举类型都需要考虑类型转换这个问题，
     * 按照之前为某一具体枚举类创建一个转换器对象的思路，我们需要为每个枚举类型都定义一个Converter，
     * 但由于每个枚举类的实际上都差不多，并且每个Converter的转换逻辑都完全相同，针对这种情况，我们使用ConverterFactory接口更为合适，
     * 这个接口可以将同一个转换逻辑应用到一个接口的所有实现类，
     * 因此我们可以定义一个`BaseEnum`接口，然后令所有的枚举类都实现该接口，
     * 然后就可以自定义`ConverterFactory`，集中编写各枚举类的转换逻辑了
    * */
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        //获取转换器之后还需要通过该转换器获得相应的对象，而Converter是一个接口，所以还要进行内部类的方法重写
        //而重写的内容就是一个统一的对象获取方法
        return new Converter<String, T>() {
            @Override
            public T convert(String code) {
                //通过反射，获取目标类的所有枚举对象

                for(T enumType: targetType.getEnumConstants()){
                    if(enumType.getCode().equals(Integer.valueOf(code))){
                        return enumType;
                    }
                }
                throw new IllegalArgumentException("code:"+code+"非法");
            }
        };
    }
}
