package com.wkclz.micro.file.bean.dto;

import com.wkclz.micro.file.bean.entity.MdmFileRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table MdmFileRecord () 数据库实例扩展，代码重新生成不覆盖
 */

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFileRecordDto extends MdmFileRecord {

    private List<String> fileIds;

    private String previewUrl;

    /**
     * entity 转 Dto
     * @param source
     * @return
     */
    public static MdmFileRecordDto copy(MdmFileRecord source) {
        MdmFileRecordDto target = new MdmFileRecordDto();
        MdmFileRecord.copy(source, target);
        return target;
    }
}
