package com.wkclz.micro.liteflow.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "LiteFlow 通用", description = "LiteFlow 通用查询")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class CommonLiteFlowRest {


}

