package com.wkclz.auto.mock;

import com.wkclz.auto.bean.ApiParamInfo;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockHelper {

    private static final Logger logger = LoggerFactory.getLogger(MockHelper.class);

    private final ApplicationContext applicationContext;

    private final Map<String, Object> originalBeans = new ConcurrentHashMap<>();
    private final List<Object> activeMocks = new ArrayList<>();

    public MockHelper(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void mockControllerDependencies(Class<?> controllerClass) {
        Field[] fields = controllerClass.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            Class<?> fieldType = field.getType();
            String beanName = resolveBeanName(fieldType);
            if (beanName != null) {
                Object originalBean = applicationContext.getBean(beanName);
                Object mockBean = Mockito.mock(fieldType);
                originalBeans.put(beanName, originalBean);
                activeMocks.add(mockBean);
                logger.debug("mocked dependency: {} -> {}", field.getName(), fieldType.getSimpleName());
            }
        }
    }

    public Object generateParamValue(ApiParamInfo paramInfo) {
        return TestDataGenerator.generate(paramInfo.getType());
    }

    public void resetAll() {
        for (Object mock : activeMocks) {
            Mockito.reset(mock);
        }
        activeMocks.clear();
    }

    private String resolveBeanName(Class<?> type) {
        String[] beanNames = applicationContext.getBeanNamesForType(type);
        return beanNames.length > 0 ? beanNames[0] : null;
    }
}
