package com.wkclz.micro.k8s;


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

    String CONFIG_PAGE = "/config/page";
    String CONFIG_INFO = "/config/info";
    String CONFIG_CREATE = "/config/create";
    String CONFIG_UPDATE = "/config/update";
    String CONFIG_REMOVE = "/config/remove";
    String CONFIG_OPTIONS = "/config/options";

    String CLUSTER_NODES = "/cluster/nodes";
    String CLUSTER_NAMESPACES = "/cluster/namespaces";
    String CLUSTER_NAMESPACES_BRIEFLY = "/cluster/namespaces/briefly";


    String CLUSTER_KIND_LIST = "/cluster/kind/list";
    String CLUSTER_KIND_YAML = "/cluster/kind/yaml";
    String CLUSTER_KIND_CREATE = "/cluster/kind/create";
    String CLUSTER_KIND_UPDATE = "/cluster/kind/update";
    String CLUSTER_KIND_DELETE = "/cluster/kind/delete";

}
