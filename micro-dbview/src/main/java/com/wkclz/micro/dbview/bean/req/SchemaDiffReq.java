package com.wkclz.micro.dbview.bean.req;

import com.wkclz.micro.dbview.utils.schemadiff.model.DiffOptions;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SchemaDiffReq {

    @NotNull(message = "基准侧对比源不能为空")
    @Valid
    private DiffSourceReq base;

    @NotNull(message = "对比侧对比源不能为空")
    @Valid
    private DiffSourceReq other;

    /** 对比开关，为空时使用全默认 */
    private DiffOptions options;

    @Data
    public static class DiffSourceReq {

        @NotNull(message = "对比源类型不能为空")
        private SourceType type;

        /** type=DATASOURCE 时必填 */
        private Long datasourceId;

        /** type=DATASOURCE 时必填 */
        private String schemaName;

        /** 可选：指定单表，空=整库 */
        private String tableName;

        /** type=DDL_TEXT 时必填；DDL 文件由前端读取内容后传入 */
        @Size(max = 5_000_000, message = "DDL 文本超过长度上限 5MB")
        private String ddlText;

        public enum SourceType {
            DATASOURCE, DDL_TEXT
        }
    }
}
