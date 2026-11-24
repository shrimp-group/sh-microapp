package com.wkclz.micro.dag.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * dag_node_instance 租约操作入参（CAS 领取 casClaim / 租约续期 renewLease 共用）
 */
@Schema(description = "节点实例租约操作入参（CAS 领取 casClaim / 租约续期 renewLease 共用）")
@Data
public class NodeInstanceLeaseDto {

    @Schema(description = "节点实例 id")
    private Long id;

    @Schema(description = "乐观锁版本号，CAS 领取校验用；租约续期不使用")
    private Integer version;

    @Schema(description = "租约持有者（worker id）")
    private String leaseOwner;

    @Schema(description = "租约到期时间")
    private LocalDateTime leaseExpireTime;
}
