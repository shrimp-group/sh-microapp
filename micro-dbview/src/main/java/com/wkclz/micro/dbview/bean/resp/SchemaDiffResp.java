package com.wkclz.micro.dbview.bean.resp;

import com.wkclz.micro.dbview.utils.schemadiff.model.DiffResult;
import lombok.Data;

import java.util.List;

@Data
public class SchemaDiffResp {
    private String baseDesc;
    private String otherDesc;
    private DiffResult diffResult;
    /** 将 other 统一到 base 的 DDL；前端交换 base/other 重调即可反转方向 */
    private List<String> syncDdls;
}
