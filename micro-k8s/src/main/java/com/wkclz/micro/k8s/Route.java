package com.wkclz.micro.k8s;


import com.wkclz.core.annotation.Desc;
import com.wkclz.core.annotation.Router;

/**
 * Description:
 * Created: wangkaicun @ 2017-10-19 上午12:46
 */


@Router(module = "micro-k8s", prefix = Route.PREFIX)
public interface Route {

    String PREFIX = "/micro-k8s";


    /**
     * k8s config
     */

    @Desc("1. k8s配置-分页")
    String CONFIG_PAGE = "/config/page";
    @Desc("2. k8s配置-详情")
    String CONFIG_INFO = "/config/info";
    @Desc("3. k8s配置-新增")
    String CONFIG_CREATE = "/config/create";
    @Desc("4. k8s配置-更新")
    String CONFIG_UPDATE = "/config/update";
    @Desc("5. k8s配置-删除")
    String CONFIG_REMOVE = "/config/remove";
    @Desc("1. k8s配置-选项")
    String CONFIG_OPTIONS = "/config/options";

    @Desc("0. k8s-获取 集群 node 节点")
    String CLUSTER_NODES = "/cluster/nodes";
    @Desc("1. k8s-获取 namespaces")
    String CLUSTER_NAMESPACES = "/cluster/namespaces";
    @Desc("2. k8s-获取 namespaces")
    String CLUSTER_NAMESPACES_BRIEFLY = "/cluster/namespaces/briefly";


    @Desc("1. k8s-kind 获取列表")
    String CLUSTER_KIND_LIST = "/cluster/kind/list";
    @Desc("2. k8s-kind 获取Yaml")
    String CLUSTER_KIND_YAML = "/cluster/kind/yaml";
    @Desc("3. k8s-kind 创建")
    String CLUSTER_KIND_CREATE = "/cluster/kind/create";
    @Desc("4. k8s-kind 更新")
    String CLUSTER_KIND_UPDATE = "/cluster/kind/update";
    @Desc("5. k8s-kind 删除")
    String CLUSTER_KIND_DELETE = "/cluster/kind/delete";

}
