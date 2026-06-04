package com.wkclz.micro.fileos.bean.dto;

import com.wkclz.micro.fileos.bean.entity.MdmFileosRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MdmFileosRecordDto extends MdmFileosRecord {

    private List<String> fileIds;

    private String previewUrl;

    public static MdmFileosRecordDto copy(MdmFileosRecord source) {
        MdmFileosRecordDto target = new MdmFileosRecordDto();
        MdmFileosRecord.copy(source, target);
        return target;
    }
}
