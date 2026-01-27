package com.wkclz.micro.audit.pojo.dto;

import com.wkclz.micro.audit.pojo.entity.MdmChangeLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_change_log (变更记录) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class ChangeLog<T> extends MdmChangeLog {


    private Class<T> clazz;

    private List<ChangeLogItem> items;


    /**
     * entity 转 Param
     */
    public static <T> ChangeLog<T> copy(MdmChangeLog source) {
        ChangeLog<T> target = new ChangeLog<>();
        MdmChangeLog.copy(source, target);
        return target;
    }
}

