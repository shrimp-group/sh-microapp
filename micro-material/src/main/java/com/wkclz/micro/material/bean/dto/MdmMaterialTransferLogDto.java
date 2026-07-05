package com.wkclz.micro.material.bean.dto;

import com.wkclz.micro.material.bean.entity.MdmMaterialTransferLog;
import com.wkclz.tool.utils.BeanUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmMaterialTransferLog (素材转移日志) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmMaterialTransferLogDto extends MdmMaterialTransferLog {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmMaterialTransferLogDto copy(MdmMaterialTransferLog source) {
        MdmMaterialTransferLogDto target = new MdmMaterialTransferLogDto();
        BeanUtil.cpAll(source, target);
        return target;
    }
}
