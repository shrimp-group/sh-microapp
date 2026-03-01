package com.wkclz.micro.k8s.bean.kube;

import lombok.Data;

@Data
public class K8sParam {

    private String clusterName;
    private String namespace;
    private String kind;
    private String name;
    private String yaml;

}
