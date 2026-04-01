package com.wkclz.micro.file.pojo.dto;

import com.wkclz.micro.file.pojo.entity.MdmFileBucket;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmFileBucket () 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFileBucketDto extends MdmFileBucket {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFileBucketDto copy(MdmFileBucket source) {
        MdmFileBucketDto target = new MdmFileBucketDto();
        MdmFileBucket.copy(source, target);
        return target;
    }
}

