package com.wkclz.micro.material.bean.resp;

import com.wkclz.web.bean.EntityResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialGroupResp extends EntityResp {

    private String groupCode;
    private String parentCode;
    private String groupName;
    private String groupType;
    private Integer sort;
}
