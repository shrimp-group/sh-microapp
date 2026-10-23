package com.wkclz.micro.flowable.service;

import com.wkclz.core.base.PageData;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.flowable.bean.entity.FlowableProcessDesign;
import com.wkclz.micro.flowable.bean.req.DesignPageReq;
import com.wkclz.micro.flowable.bean.resp.DesignPageResp;
import com.wkclz.micro.flowable.mapper.FlowableProcessDesignMapper;
import com.wkclz.mybatis.helper.PageQuery;
import com.wkclz.mybatis.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FlowableProcessDesignService extends BaseService<FlowableProcessDesign, FlowableProcessDesignMapper> {

    private static final Logger log = LoggerFactory.getLogger(FlowableProcessDesignService.class);

    /** process 标签（兼容 bpmn:/bpmn2: 等命名空间前缀与自闭合写法） */
    private static final Pattern PROCESS_TAG_PATTERN =
            Pattern.compile("<(?:[A-Za-z][\\w.-]*:)?process\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PROCESS_ID_PATTERN = Pattern.compile("(\\bid=\")([^\"]*)(\")");
    private static final Pattern PROCESS_NAME_PATTERN = Pattern.compile("(\\bname=\")([^\"]*)(\")");

    public PageData<DesignPageResp> getDesignPage(DesignPageReq req) {
        return PageQuery.page(req, mapper::getDesignPage);
    }

    /**
     * BPMN process id/name 归一化为设计表设置（designCode/designName）：
     * 保存（创建/修改）时统一改写，引擎 definition key/name 由入库 XML process id/name 决定，
     * 保证全链路以设计表为准；改名未重传 XML 时对存量 XML 重入（幂等）；节点元素（userTask 等）id 不受影响。
     */
    public String normalizeProcess(String xmlContent, String designCode, String designName) {
        log.info("归一化 BPMN process id/name: designCode={}, designName={}", designCode, designName);
        if (xmlContent == null || xmlContent.isEmpty()) {
            log.error("BPMN XML 为空: designCode={}", designCode);
            throw ValidationException.of("BPMN XML 内容为空");
        }
        Matcher tagMatcher = PROCESS_TAG_PATTERN.matcher(xmlContent);
        if (!tagMatcher.find()) {
            log.error("BPMN XML 缺少 process 节点: designCode={}", designCode);
            throw ValidationException.of("BPMN XML 缺少 process 节点");
        }
        String tag = tagMatcher.group();

        // id 改写为 designCode
        Matcher idMatcher = PROCESS_ID_PATTERN.matcher(tag);
        if (!idMatcher.find()) {
            log.error("BPMN process 节点缺少 id 属性: designCode={}", designCode);
            throw ValidationException.of("BPMN process 节点缺少 id 属性");
        }
        String newTag = tag.substring(0, idMatcher.start(2)) + designCode + tag.substring(idMatcher.end(2));

        // name 改写为 designName，无该属性时补插在 id 之后
        String safeName = designName == null ? "" : designName.replace("\"", "&quot;");
        Matcher nameMatcher = PROCESS_NAME_PATTERN.matcher(newTag);
        if (nameMatcher.find()) {
            newTag = newTag.substring(0, nameMatcher.start(2)) + safeName + newTag.substring(nameMatcher.end(2));
        } else {
            Matcher idAgain = PROCESS_ID_PATTERN.matcher(newTag);
            idAgain.find();
            int insertAt = idAgain.end(3);
            newTag = newTag.substring(0, insertAt) + " name=\"" + safeName + "\"" + newTag.substring(insertAt);
        }
        return xmlContent.substring(0, tagMatcher.start()) + newTag + xmlContent.substring(tagMatcher.end());
    }
}
