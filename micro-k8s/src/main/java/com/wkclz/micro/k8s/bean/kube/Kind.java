package com.wkclz.micro.k8s.bean.kube;

public enum Kind {

    Pod("一个Kubernetes中最基本的资源类型，它用于定义一个或多个容器的共同运行环境"),
    Deployment("用于定义应用程序的声明式更新"),
    Service("用于定义一组pod的逻辑集合，以及访问这些pod的方式"),
    DaemonSet("用于在集群中运行一个pod的声明式更新和管理"),
    ReplicaSet("用于确保在集群中运行指定数量的pod的声明式更新和管理"),
    ServiceAccount("定义一个ServiceAccount对象，用于给Pod分配身份与访问权限"),
    PodDisruptionBudget("用于定义维护期间可以安全中断的pod的最小数量，以确保Kubernetes集群的高可用性"),
    PersistentVolumeClaim("PersistentVolumeClaim（PVC）是Kubernetes中用于声明持久化存储资源的对象"),
    PersistentVolume("用于定义持久化存储卷，并使它们在Kubernetes集群中可用"),
    Job("定义一个Job对象，用于定义一个运行一次性任务的作业"),
    CronJob("定义一个CronJob对象，用于定义一个周期性运行任务的作业"),
    StatefulSet("用于有状态应用程序的声明式更新和管理"),
    ConfigMap("用于存储非敏感数据（如配置文件）的声明式更新和管理"),
    Secret("用于存储敏感数据（如密码和密钥）的声明式更新和管理"),
    Ingress("定义一个Ingress对象，用于配置集群中的HTTP和HTTPS路由规则"),
    StorageClass("用于定义不同类型的存储，例如云存储、本地存储等，并为这些存储类型指定默认的参数和策略"),
    Namespace("用于在Kubernetes集群中创建逻辑分区，从而将资源隔离开来，以提高安全性和可维护性"),
    ServiceMonitor("用于自动发现和监控在Kubernetes集群中运行的服务"),
    HorizontalPodAutoscaler("定义一个HorizontalPodAutoscaler对象，用于自动调整Pod副本数量以适应负载"),
    NetworkPolicy("定义一个NetworkPolicy对象，用于在Pod之间定义网络流量规则"),
    CustomResourceDefinition("用于定义自定义资源，以扩展Kubernetes API和自定义资源类型"),
    Role("用于定义对Kubernetes资源的操作权限，例如读、写、更新、删除等"),
    ClusterRole("与Role类似，但是可以在整个Kubernetes集群中使用"),
    ClusterRoleBinding("定义一个集群角色绑定对象，将集群角色与用户或ServiceAccount关联"),
    RoleBinding("定义一个角色绑定对象，将角色与用户或ServiceAccount关联"),
    Endpoints("定义一个Endpoint对象，用于指定Service的后端IP地址和端口"),
    Volume("定义一个Volume对象，用于将存储挂载到Pod中"),
    PodSecurityPolicy("定义一个PodSecurityPolicy对象，用于定义Pod的安全策略"),
    Event("定义一个Event对象，用于记录集群中发生的事件"),
    ResourceQuota("定义一个ResourceQuota对象，用于限制命名空间中资源的使用量"),
    PriorityClass("定义一个PriorityClass对象，用于设置Pod的优先级"),
    VolumeSnapshot("定义一个VolumeSnapshot对象，用于创建和管理存储卷的快照"),
    ;


    private String desc;

    Kind(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
