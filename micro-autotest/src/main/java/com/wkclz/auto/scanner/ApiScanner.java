package com.wkclz.auto.scanner;

import com.wkclz.auto.bean.ApiInfo;
import com.wkclz.auto.bean.ApiParamInfo;
import com.wkclz.core.annotation.ApiDesc;
import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;
import com.wkclz.tool.utils.ClassUtil;
import com.wkclz.tool.utils.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ApiScanner {

    private static final Logger logger = LoggerFactory.getLogger(ApiScanner.class);

    @Autowired
    private ApplicationContext applicationContext;

    public List<ApiInfo> scan() {
        String packagePath = getDefaultPackage();
        return scan(packagePath);
    }

    public List<ApiInfo> scan(String packagePath) {
        if (StringUtils.isBlank(packagePath)) {
            packagePath = getDefaultPackage();
        }

        logger.info("scanning apis in package: {}", packagePath);

        List<ApiInfo> apiInfos = new ArrayList<>();
        Set<Class<?>> classes = ClassUtil.getClasses(packagePath);

        List<Class<?>> restClassList = classes.stream()
            .filter(clazz -> clazz.isAnnotationPresent(RestController.class) || clazz.isAnnotationPresent(Controller.class))
            .toList();

        List<Class<?>> routerClassList = classes.stream()
            .filter(clazz -> clazz.isAnnotationPresent(Router.class))
            .toList();

        for (Class<?> clazz : restClassList) {
            String prefix = getPrefix(clazz);
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                ApiInfo apiInfo = buildApiInfo(method, prefix, clazz);
                if (apiInfo != null) {
                    apiInfos.add(apiInfo);
                }
            }
        }

        appendDesc(routerClassList, apiInfos);
        return apiInfos;
    }

    private String getDefaultPackage() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
        if (!beans.isEmpty()) {
            Object app = beans.values().iterator().next();
            return app.getClass().getPackageName();
        }
        String clazzName = ApiScanner.class.getName();
        int index = clazzName.indexOf(".", clazzName.indexOf(".") + 1);
        return clazzName.substring(0, index);
    }

    private String getPrefix(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(RequestMapping.class)) {
            return null;
        }
        RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
        String[] values = annotation.value();
        String prefix = values.length > 0 ? values[0] : null;
        if (prefix != null && !prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (prefix != null && prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix;
    }

    private ApiInfo buildApiInfo(Method method, String prefix, Class<?> clazz) {
        Annotation[] annotations = method.getAnnotations();
        String uri = null;
        String desc = null;
        RequestMethod requestMethod = null;

        for (Annotation annotation : annotations) {
            if (RequestMapping.class == annotation.annotationType()) {
                RequestMapping request = (RequestMapping) annotation;
                RequestMethod[] requestMethods = request.method();
                requestMethod = requestMethods.length > 0 ? requestMethods[0] : RequestMethod.GET;
                String[] values = request.value();
                uri = values.length == 0 ? null : values[0];
                continue;
            }
            if (GetMapping.class == annotation.annotationType()) {
                GetMapping request = (GetMapping) annotation;
                requestMethod = RequestMethod.GET;
                String[] values = request.value();
                uri = values.length == 0 ? null : values[0];
                continue;
            }
            if (PostMapping.class == annotation.annotationType()) {
                PostMapping request = (PostMapping) annotation;
                requestMethod = RequestMethod.POST;
                String[] values = request.value();
                uri = values.length == 0 ? null : values[0];
                continue;
            }
            if (PutMapping.class == annotation.annotationType()) {
                PutMapping request = (PutMapping) annotation;
                requestMethod = RequestMethod.PUT;
                String[] values = request.value();
                uri = values.length == 0 ? null : values[0];
                continue;
            }
            if (DeleteMapping.class == annotation.annotationType()) {
                DeleteMapping request = (DeleteMapping) annotation;
                requestMethod = RequestMethod.DELETE;
                String[] values = request.value();
                uri = values.length == 0 ? null : values[0];
                continue;
            }
            if (Desc.class == annotation.annotationType()) {
                Desc descAnno = (Desc) annotation;
                desc = descAnno.value();
            }
            if (ApiDesc.class == annotation.annotationType()) {
                ApiDesc descAnno = (ApiDesc) annotation;
                desc = descAnno.value();
            }
        }

        if (uri == null || requestMethod == null) {
            return null;
        }

        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        if (prefix != null) {
            uri = prefix + uri;
        }

        String restName = uri.substring(1);
        restName = restName.replace("-", "_");
        restName = restName.replace("/", "_");
        restName = restName.replace("{", "");
        restName = restName.replace("}", "");
        restName = restName.replace("*", "");
        restName = StringUtil.underlineToCamel(restName);

        List<ApiParamInfo> paramInfos = analyzeParams(method);

        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setControllerClass(clazz);
        apiInfo.setMethod(requestMethod.name());
        apiInfo.setUri(uri);
        apiInfo.setName(restName);
        apiInfo.setDesc(desc);
        apiInfo.setParams(paramInfos);
        apiInfo.setReturnType(method.getReturnType());
        return apiInfo;
    }

    private List<ApiParamInfo> analyzeParams(Method method) {
        List<ApiParamInfo> paramInfos = new ArrayList<>();
        Parameter[] parameters = method.getParameters();

        for (Parameter parameter : parameters) {
            ApiParamInfo paramInfo = new ApiParamInfo();
            paramInfo.setName(parameter.getName());
            paramInfo.setType(parameter.getType());
            paramInfo.setRequestBody(parameter.isAnnotationPresent(RequestBody.class));
            paramInfo.setPathVariable(parameter.isAnnotationPresent(PathVariable.class));
            paramInfo.setRequestParam(parameter.isAnnotationPresent(RequestParam.class));
            paramInfos.add(paramInfo);
        }

        return paramInfos;
    }

    private void appendDesc(List<Class<?>> routerClassList, List<ApiInfo> apiInfos) {
        if (CollectionUtils.isEmpty(routerClassList) || CollectionUtils.isEmpty(apiInfos)) {
            return;
        }

        Map<String, List<ApiInfo>> apiMap = apiInfos.stream().collect(Collectors.groupingBy(ApiInfo::getUri));

        for (Class<?> routerClazz : routerClassList) {
            Field[] fields = routerClazz.getDeclaredFields();
            try {
                Router routerAnno = routerClazz.getAnnotation(Router.class);
                String module = null;
                String prefix = null;
                if (routerAnno != null) {
                    module = routerAnno.module();
                    prefix = routerAnno.prefix();
                    if (StringUtils.isBlank(routerAnno.prefix())) {
                        prefix = "";
                    }
                }

                if (module != null) {
                    String routerPackage = routerClazz.getPackageName();
                    for (ApiInfo apiInfo : apiInfos) {
                        if (apiInfo.getControllerClass() == null) {
                            continue;
                        }
                        String restPackage = apiInfo.getControllerClass().getPackageName();
                        if (restPackage.startsWith(routerPackage)) {
                            apiInfo.setModule(module);
                        }
                    }
                }

                for (Field field : fields) {
                    Object o = field.get(null);
                    if (o == null) {
                        continue;
                    }
                    Desc desc = field.getAnnotation(Desc.class);
                    ApiDesc apiDesc = field.getAnnotation(ApiDesc.class);

                    String value = null;
                    if (desc != null) {
                        value = desc.value();
                    }
                    if (apiDesc != null) {
                        value = apiDesc.value();
                    }
                    if (StringUtils.isBlank(value)) {
                        continue;
                    }

                    String uri = o.toString();
                    String fullUri = prefix + uri;
                    List<ApiInfo> matchedApis = apiMap.get(fullUri);
                    if (CollectionUtils.isEmpty(matchedApis)) {
                        continue;
                    }
                    for (ApiInfo apiInfo : matchedApis) {
                        apiInfo.setDesc(value);
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
