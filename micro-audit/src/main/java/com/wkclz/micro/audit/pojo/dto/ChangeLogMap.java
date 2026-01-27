package com.wkclz.micro.audit.pojo.dto;

import com.wkclz.micro.audit.pojo.entity.MdmChangeLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_change_log (变更记录) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class ChangeLogMap extends MdmChangeLog {

    private Map<String, Object> dataFromEntity;
    private Map<String, Object> dataToEntity;

    /**
     * entity 转 Param
     */
    public static ChangeLogMap copy(MdmChangeLog source) {
        ChangeLogMap target = new ChangeLogMap();
        MdmChangeLog.copy(source, target);
        return target;
    }
}

