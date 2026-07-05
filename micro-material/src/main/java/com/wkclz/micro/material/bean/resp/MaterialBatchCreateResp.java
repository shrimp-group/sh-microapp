package com.wkclz.micro.material.bean.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MaterialBatchCreateResp implements Serializable {

    private List<MaterialResp> materials;
}
