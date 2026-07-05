package com.wkclz.micro.material.bean.req;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MaterialBatchCreateReq {

    @NotEmpty(message = "素材列表不能为空")
    private List<MaterialCreateItem> items;
    private String materialType;
    private String groupCode;
    private String visibility;

    @Data
    public static class MaterialCreateItem {

        private String fileId;
        private String fileName;
        private Long fileSize;
    }
}
