package com.wkclz.micro.file.pojo.dto;

import com.wkclz.micro.file.pojo.entity.MdmFsBucket;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_fs_bucket (Bucket管理) 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFsBucketDto extends MdmFsBucket {




    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFsBucketDto copy(MdmFsBucket source) {
        MdmFsBucketDto target = new MdmFsBucketDto();
        MdmFsBucket.copy(source, target);
        return target;
    }
}

