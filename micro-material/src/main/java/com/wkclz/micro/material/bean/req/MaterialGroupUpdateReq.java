package com.wkclz.micro.material.bean.req;

import com.wkclz.web.bean.UpdateReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialGroupUpdateReq extends UpdateReq {

    private String groupName;
}
