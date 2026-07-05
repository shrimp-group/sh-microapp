package com.wkclz.auto.mock;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AutoMockBeanPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AutoMockBeanPostProcessor.class);

    private static boolean mockEnabled = false;

    public static void enableMock() {
        mockEnabled = true;
    }

    public static void disableMock() {
        mockEnabled = false;
    }

    public static boolean isMockEnabled() {
        return mockEnabled;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!mockEnabled) {
            return;
        }

        String[] beanNames = registry.getBeanDefinitionNames();
        List<String> mapperBeanNames = new ArrayList<>();

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }

            if (isMapperBean(beanClassName)) {
                mapperBeanNames.add(beanName);
                logger.info("auto mock mapper: {}", beanName);
            }
        }

        for (String beanName : mapperBeanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }

            try {
                Class<?> beanClass = Class.forName(beanClassName);
                registry.removeBeanDefinition(beanName);

                GenericBeanDefinition mockDefinition = new GenericBeanDefinition();
                mockDefinition.setBeanClass(beanClass);
                mockDefinition.setInstanceSupplier(() -> Mockito.mock(beanClass));
                mockDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

                registry.registerBeanDefinition(beanName, mockDefinition);
            } catch (ClassNotFoundException e) {
                logger.warn("failed to mock bean {}: {}", beanName, e.getMessage());
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    private boolean isMapperBean(String beanClassName) {
        return beanClassName.contains(".mapper.");
    }
}
