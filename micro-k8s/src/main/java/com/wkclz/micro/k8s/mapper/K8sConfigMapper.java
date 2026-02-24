package com.wkclz.micro.k8s.mapper;

import com.wkclz.micro.k8s.bean.entity.K8sConfig;
import com.wkclz.mybatis.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Description Create by sh-generator
 * @author shrimp
 * @table k8s_config (k8s配置) DAO 接口，代码重新生成不覆盖
 */

@Mapper
public interface K8sConfigMapper extends BaseMapper<K8sConfig> {

    List<String> getClusterOptions();

}

