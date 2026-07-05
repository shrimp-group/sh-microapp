package com.wkclz.micro.material.bean.resp;

import com.wkclz.web.bean.EntityResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialResp extends EntityResp {

    private String materialCode;
    private String materialName;
    private String materialType;
    private String sourceType;
    private String groupCode;
    private String fileId;
    private String fileName;
    private String fileExt;
    private Long fileSize;
    private String linkUrl;
    private String linkStatus;
    private String visibility;
    private String coverFileId;
    private String description;
    private String signedUrl;
    private List<MaterialRefResp> refs;
    private List<MaterialVersionResp> versions;
}
