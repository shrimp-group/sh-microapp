package com.wkclz.micro.form.aop;


import com.alibaba.fastjson2.JSONPath;
import com.wkclz.core.base.R;
import com.wkclz.micro.form.cache.FormRuleCache;
import com.wkclz.micro.form.bean.dto.MdmFormRuleDto;
import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldDto;
import com.wkclz.micro.form.bean.dto.MdmFormRuleFieldValidatorDto;
import com.wkclz.micro.form.bean.enums.ValidatorTypeEnum;
import com.wkclz.micro.form.validator.IValidator;
import com.wkclz.micro.form.validator.ValidatorFactory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

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
        List<MdmFormRuleFieldDto> fields = formRule.getFields();
        if (CollectionUtils.isEmpty(fields)) {
            return point.proceed();
        }

        // 参数验证过程 【暂时只管第一个参数】
        String args = getArgs(point);
        R rt = paramCheck(args, fields);
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
        return objectMapper.writeValueAsString(first);
    }

    private R paramCheck(String args, List<MdmFormRuleFieldDto> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }
        if (StringUtils.isBlank(args)) {
            return R.error("参数校验: 未检测到任何参数!");
        }

        for (MdmFormRuleFieldDto field : fields) {
            String fieldCode = field.getFieldCode();
            if (StringUtils.isBlank(fieldCode)) {
                continue;
            }
            if (!fieldCode.startsWith("$")) {
                fieldCode = "$." + fieldCode;
            }

            List<MdmFormRuleFieldValidatorDto> validators = field.getValidators();
            if (CollectionUtils.isEmpty(validators)) {
                continue;
            }

            Object eval = JSONPath.eval(args, fieldCode);
            String value = eval == null ? null : eval.toString();

            for (MdmFormRuleFieldValidatorDto validatorDto : validators) {
                ValidatorTypeEnum validatorType = ValidatorTypeEnum.valueOf(validatorDto.getValidatorType());

                IValidator validator = ValidatorFactory.getValidator(validatorType);
                String validate = validator.validate(value, validatorDto);
                if (validate != null) {
                    return R.error(validate);
                }
            }
        }
        return null;
    }

}
