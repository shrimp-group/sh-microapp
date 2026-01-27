package com.wkclz.micro.form.aop;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wkclz.core.base.R;
import com.wkclz.core.exception.ValidationException;
import com.wkclz.micro.form.cache.FormRuleCache;
import com.wkclz.micro.form.pojo.dto.MdmFormRuleDto;
import com.wkclz.micro.form.pojo.entity.MdmFormRuleItem;
import com.wkclz.tool.tools.RegularTool;
import com.wkclz.tool.utils.JsUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * RestAop
 * wangkc @ 2019-07-28 23:56:25
 */
@Aspect
@Component
@Order(Integer.MIN_VALUE)
public class FormRuleAop {

    /**
     * : @Around环绕通知
     * : @Before通知执行
     * : @Before通知执行结束
     * : @Around环绕通知执行结束
     * : @After后置通知执行了!
     * : @AfterReturning第一个后置返回通知的返回值：18
     */

    private static final Logger logger = LoggerFactory.getLogger(FormRuleAop.class);

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private FormRuleCache  formRuleCache;

    private static final String POINT_CUT = "(" +
        "@within(org.springframework.stereotype.Controller) " +
        "|| @within(org.springframework.web.bind.annotation.RestController)" +
        ") && !execution(* org.springframework..*.*(..))";


    @Pointcut(POINT_CUT)
    public void pointCut() {
    }

    /**
     * 环绕通知：
     * 注意:Spring AOP的环绕通知会影响到AfterThrowing通知的运行,不要同时使用
     * <p>
     * 环绕通知非常强大，可以决定目标方法是否执行，什么时候执行，执行时是否需要替换方法参数，执行完毕是否需要替换返回值。
     * 环绕通知第一个参数必须是org.aspectj.lang.ProceedingJoinPoint类型
     */
    @Around(value = POINT_CUT)
    public Object doAroundAdvice(ProceedingJoinPoint point) throws Throwable {
        return servletRequestHandle(point);
    }

    private Object servletRequestHandle(ProceedingJoinPoint point) throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest req = requestAttributes.getRequest();
        HttpServletResponse resp = requestAttributes.getResponse();

        String method = req.getMethod();
        String uri = req.getRequestURI();
        MdmFormRuleDto formRule = formRuleCache.getFormRule(method, uri);

        // 没有表单验证规则
        if (formRule == null) {
            return point.proceed();
        }
        List<MdmFormRuleItem> items = formRule.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return point.proceed();
        }

        // 参数验证过程 【暂时只管第一个参数】
        String args = getArgs(point);
        R rt = paramCheck(args, items);
        if (rt != null) {
            return rt;
        }

        // 验证通过
        return point.proceed();
    }

    private String getArgs(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        if (args == null) {
            return null;
        }
        List<Object> params = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof HttpServletRequest) {
                continue;
            }
            if (arg instanceof HttpServletResponse) {
                continue;
            }
            if (arg instanceof MultipartFile) {
                continue;
            }
            params.add(arg);
        }
        if (CollectionUtils.isEmpty(params)) {
            return null;
        }
        Object first = params.get(0);
        try {
            return objectMapper.writeValueAsString(first);
        } catch (JsonProcessingException e) {
            throw ValidationException.of("can not get args!");
        }
    }

    private R paramCheck(String args, List<MdmFormRuleItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        if (StringUtils.isBlank(args)) {
            return R.error("参数校验: 未检测到任何参数!");
        }

        for (MdmFormRuleItem item : items) {
            String path = item.getFieldName();
            if (StringUtils.isBlank(path)) {
                continue;
            }
            if (!path.startsWith("$")) {
                path = "$." + path;
            }

            Object eval = JSONPath.eval(args, path);
            String value = eval == null ? null : eval.toString();

            if (item.getRequired() == 1 && (eval == null || "".equals(value)) ) {
                return R.error(item.getMessage());
            }

            // 以下参数校验，需要与 UI 支持的类型保持一致

            if ("string".equals(item.getFieldType()) && StringUtils.isNotBlank(value)) {
                // 字符串
                if (item.getMinLength() != null && value.length() < item.getMinLength()) {
                    return R.error(item.getMessage());
                }
                if (item.getMaxLength() != null && value.length() > item.getMaxLength()) {
                    return R.error(item.getMessage());
                }
                if (item.getPattern() != null && !value.matches(item.getPattern())) {
                    return R.error(item.getMessage());
                }
                if (StringUtils.isNotBlank(item.getValidator())) {
                    return validateScript(item, args);
                }
            }

            if ("number".equals(item.getFieldType()) && StringUtils.isNotBlank(value)) {
                // 数字
                if (!NumberUtils.isCreatable(value)) {
                    return R.error(item.getMessage());
                }
                Double numberValue = NumberUtils.createDouble(value);
                if (item.getDataMin() != null && numberValue < item.getDataMin()) {
                    return R.error(item.getMessage());
                }
                if (item.getDataMax() != null && numberValue > item.getDataMax()) {
                    return R.error(item.getMessage());
                }
                if (StringUtils.isNotBlank(item.getValidator())) {
                    return validateScript(item, args);
                }
            }
            if ("integer".equals(item.getFieldType()) && StringUtils.isNotBlank(value)) {
                // 数字
                if (!NumberUtils.isCreatable(value)) {
                    return R.error(item.getMessage());
                }
                int integerValue = Integer.parseInt(value);
                if (item.getDataMin() != null && integerValue < item.getDataMin()) {
                    return R.error(item.getMessage());
                }
                if (item.getDataMax() != null && integerValue > item.getDataMax()) {
                    return R.error(item.getMessage());
                }
                if (StringUtils.isNotBlank(item.getValidator())) {
                    return validateScript(item, args);
                }
            }
            if ("float".equals(item.getFieldType()) && StringUtils.isNotBlank(value)) {
                // 数字
                if (!NumberUtils.isCreatable(value)) {
                    return R.error(item.getMessage());
                }
                Double doubleValue = NumberUtils.createDouble(value);
                if (item.getDataMin() != null && doubleValue < item.getDataMin()) {
                    return R.error(item.getMessage());
                }
                if (item.getDataMax() != null && doubleValue > item.getDataMax()) {
                    return R.error(item.getMessage());
                }
                if (StringUtils.isNotBlank(item.getValidator())) {
                    return validateScript(item, args);
                }
            }
            if ("date".equals(item.getFieldType()) && StringUtils.isNotBlank(value)) {
                // 日期
                if (!RegularTool.isDate(value)) {
                    return R.error(item.getMessage());
                }
            }
            if ("email".equals(item.getFieldType()) && StringUtils.isNotBlank(value)) {
                // 邮箱
                if (!RegularTool.isEmail(value)) {
                    return R.error(item.getMessage());
                }
            }
            if ("url".equals(item.getFieldType()) && StringUtils.isNotBlank(value)) {
                // URL
                if (!RegularTool.isUrl(value)) {
                    return R.error(item.getMessage());
                }
            }
        }
        return null;
    }

    private static R validateScript(MdmFormRuleItem item, String args) {
        if (item == null || StringUtils.isBlank(item.getValidator())) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(args);
        String exec = JsUtil.exec(item.getValidator(), jsonObject);
        if ("true".equals( exec)) {
            return null;
        }
        return R.error(item.getMessage());
    }

}
