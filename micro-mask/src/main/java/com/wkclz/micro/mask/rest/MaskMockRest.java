package com.wkclz.micro.mask.rest;

import com.alibaba.fastjson2.JSONObject;
import com.wkclz.core.base.R;
import com.wkclz.micro.mask.bean.req.MaskRuleTestReq;
import com.wkclz.micro.mask.bean.resp.MaskRuleTestResp;
import com.wkclz.micro.mask.config.MaskResponseAdvice;
import com.wkclz.micro.mask.bean.entity.MdmMaskRule;
import com.wkclz.tool.utils.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Description Create by shrimp-gen
 * @author wangkaicun
 * @table mdm_mask_rule (脱敏规则) 示例rest 接口，代码重新生成会覆盖
 */
@Tag(name = "2.脱敏测试", description = "脱敏规则测试接口")
@Validated
@RestController
@RequestMapping(Route.PREFIX)
public class MaskMockRest {

    @Operation(summary = "1.脱敏规则-测试", description = "测试脱敏规则效果")
    @PostMapping(Route.RULE_TEST)
    public R<MaskRuleTestResp> maskRuleTest(@Valid @RequestBody MaskRuleTestReq req) {
        String mockValue = req.getMockValue();
        MdmMaskRule rule = BeanUtil.cp(req, MdmMaskRule.class);
        String maskValue = MaskResponseAdvice.maskByString(mockValue, rule);

        MaskRuleTestResp resp = new MaskRuleTestResp();
        resp.setMockValue(mockValue);
        resp.setMaskValue(maskValue);
        resp.setMaskType("使用兜底规则进行脱敏！");
        if (StringUtils.isNotBlank(req.getMaskRuleScript())) {
            resp.setMaskType("使用JS脚本进行脱敏！");
        }
        if (StringUtils.isNotBlank(req.getMaskRuleRegular())) {
            resp.setMaskType("使用正则表达式进行匹配脱敏！");
        }
        return R.ok(resp);
    }

    @Operation(summary = "2.脱敏规则-验证", description = "验证脱敏规则对JSON数据的脱敏效果")
    @GetMapping(Route.RULE_VERIFY)
    public R<JSONObject> maskRuleVerify() {
        JSONObject jsonObject = mockJson();
        return R.ok(jsonObject);
    }

    private static JSONObject mockJson() {
        String jsonStr = """
            {
                "rows": [
                    {
                        "mobile2": "13812342222",
                        "mobile3": [
                            "13812343333",
                            "13812343333"
                        ],
                        "children": [
                            {
                                "age": 18,
                                "mobile4": "13812344444",
                                "mobile5": [
                                    [
                                        "13812345555",
                                        "13812345555"
                                    ],
                                    [

                                    ]
                                ]
                            },
                            {
                                "age": 18,
                                "mobile4": "13812344444"
                            }
                        ]
                    },
                    {
                        "mobile2": "13812342222",
                        "mobile3": [
                            "13812343333",
                            "13812343333"
                        ],
                        "children": [
                            {
                                "age": 18,
                                "mobile4": "13812344444"
                            },
                            {
                                "age": 18,
                                "mobile4": "13812344444"
                            }
                        ]
                    }
                ],
                "current": 1,
                "size": 20,
                "total": 2,
                "page": 1,
                "mobile0": 13812340000,
                "mobile1": "13812341111"
            }
            """;
        return JSONObject.parseObject(jsonStr);
    }


}

