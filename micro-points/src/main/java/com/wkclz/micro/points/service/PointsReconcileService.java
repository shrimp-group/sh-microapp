package com.wkclz.micro.points.service;

import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.points.bean.entity.PointsConsumeRecord;
import com.wkclz.micro.points.bean.entity.PointsDeductionRecord;
import com.wkclz.micro.points.bean.enums.PointsConsumeStatus;
import com.wkclz.micro.points.bean.enums.PointsDeductionStatus;
import com.wkclz.micro.points.bean.req.PointsReconcileReq;
import com.wkclz.micro.points.bean.resp.PointsReconcileResp;
import com.wkclz.micro.points.mapper.PointsConsumeRecordMapper;
import com.wkclz.micro.points.mapper.PointsDeductionRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 积分对账服务
 * 核对消费流水（points_consume_record）与扣减记录（points_deduction_record）一致性
 * - DEDUCTED 消费：COMPLETED 动作记录（earn_flow_no 非空）之和应等于消费 points
 * - FROZEN 消费：应存在对应 PENDING 任务记录
 * - PARTIAL 任务记录：标记异常待处理
 * 仅统计 COMPLETED 动作记录（earn_flow_no 非空），不统计任务记录
 */
@Slf4j
@Service
public class PointsReconcileService {

    @Autowired
    private PointsConsumeRecordMapper consumeMapper;

    @Autowired
    private PointsDeductionRecordMapper deductionMapper;

    /**
     * 对账查询：核对用户消费流水与扣减记录一致性
     *
     * @param req 对账查询入参（tenantCode / userCode / 可选时间范围）
     * @return 对账结果列表
     */
    public List<PointsReconcileResp> reconcile(PointsReconcileReq req) {
        log.info("对账查询开始, tenantCode={}, userCode={}, startTime={}, endTime={}",
                req.getTenantCode(), req.getUserCode(), req.getStartTime(), req.getEndTime());

        // 参数校验
        if (StringUtils.isBlank(req.getTenantCode())) {
            throw ValidationException.of("租户编码不能为空");
        }
        if (StringUtils.isBlank(req.getUserCode())) {
            throw ValidationException.of("用户编码不能为空");
        }

        // 查询用户消费流水（按 userCode，可选时间范围过滤）
        List<PointsConsumeRecord> consumeRecords = consumeMapper.selectByUserCodeAndTimeRange(
                req.getTenantCode(), req.getUserCode(), req.getStartTime(), req.getEndTime());
        log.info("对账查询到消费流水 {} 条, tenantCode={}, userCode={}",
                consumeRecords.size(), req.getTenantCode(), req.getUserCode());

        List<PointsReconcileResp> result = new ArrayList<>(consumeRecords.size());
        for (PointsConsumeRecord consume : consumeRecords) {
            PointsReconcileResp resp = reconcileOne(req.getTenantCode(), consume);
            result.add(resp);
        }

        log.info("对账查询完成, 共核对 {} 条消费流水, tenantCode={}, userCode={}",
                result.size(), req.getTenantCode(), req.getUserCode());
        return result;
    }

    /**
     * 核对单条消费流水一致性
     */
    private PointsReconcileResp reconcileOne(String tenantCode, PointsConsumeRecord consume) {
        String orderNo = consume.getOrderNo();
        String consumeFlowNo = consume.getFlowNo();
        Integer consumePoints = consume.getPoints() == null ? 0 : consume.getPoints();

        // 聚合 COMPLETED 动作记录（earn_flow_no 非空）deduction_points 之和
        Integer deductedSum = deductionMapper.sumCompletedDeductionPointsByOrderNo(tenantCode, orderNo);
        if (deductedSum == null) {
            deductedSum = 0;
        }

        // 差异 = 消费积分 - 扣减记录之和
        Integer diff = consumePoints - deductedSum;

        // 查询任务记录（earn_flow_no IS NULL）用于判断 PENDING / PARTIAL / PROCESSED
        PointsDeductionRecord taskRecord = deductionMapper.selectTaskRecordByOrderNo(tenantCode, orderNo);

        // 判断对账状态
        String status = determineStatus(consume.getStatus(), diff, taskRecord, orderNo);

        PointsReconcileResp resp = new PointsReconcileResp();
        resp.setConsumeFlowNo(consumeFlowNo);
        resp.setPoints(consumePoints);
        resp.setDeductedSum(deductedSum);
        resp.setDiff(diff);
        resp.setStatus(status);

        // 异常状态告警日志
        if ("不一致".equals(status) || "异常".equals(status) || "异常待处理".equals(status)) {
            log.warn("对账异常, orderNo={}, consumeFlowNo={}, consumePoints={}, deductedSum={}, diff={}, status={}",
                    orderNo, consumeFlowNo, consumePoints, deductedSum, diff, status);
        } else {
            log.info("对账结果, orderNo={}, consumeFlowNo={}, consumePoints={}, deductedSum={}, diff={}, status={}",
                    orderNo, consumeFlowNo, consumePoints, deductedSum, diff, status);
        }
        return resp;
    }

    /**
     * 根据消费状态、差异、任务记录判断对账状态
     * - PARTIAL 任务记录：异常待处理（优先级最高）
     * - DEDUCTED 消费：diff==0 一致，否则 不一致
     * - FROZEN 消费：存在 PENDING 任务记录 冻结中，否则 异常
     */
    private String determineStatus(String consumeStatus, int diff, PointsDeductionRecord taskRecord, String orderNo) {
        // PARTIAL 任务记录检查（优先级最高，无论消费状态）
        if (taskRecord != null && PointsDeductionStatus.PARTIAL.name().equals(taskRecord.getStatus())) {
            return "异常待处理";
        }

        // 按消费状态判断
        if (PointsConsumeStatus.FROZEN.name().equals(consumeStatus)) {
            // FROZEN 消费：检查存在 PENDING 任务记录
            if (taskRecord != null && PointsDeductionStatus.PENDING.name().equals(taskRecord.getStatus())) {
                return "冻结中";
            }
            log.warn("FROZEN 消费未找到 PENDING 任务记录, orderNo={}, taskRecordStatus={}",
                    orderNo, taskRecord == null ? null : taskRecord.getStatus());
            return "异常";
        }

        if (PointsConsumeStatus.DEDUCTED.name().equals(consumeStatus)) {
            // DEDUCTED 消费：COMPLETED 动作记录之和应等于消费 points
            return diff == 0 ? "一致" : "不一致";
        }

        // 未知消费状态
        log.warn("未知消费状态, orderNo={}, consumeStatus={}", orderNo, consumeStatus);
        return "异常";
    }

}
