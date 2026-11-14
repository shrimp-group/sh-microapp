package com.wkclz.micro.dag.sample;

import com.wkclz.micro.dag.api.NodeContext;
import com.wkclz.micro.dag.api.NodeHandler;
import com.wkclz.micro.dag.api.NodeResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 集成验证样例：演示 NodeHandler 三个 SPI 能力（执行 / 协作式中断 / 清理钩子）
 * 仅存在于 test 源集，主应用请勿依赖 test 代码
 */
@Slf4j
public class SampleHandlers {

    /** 模拟数据拉取：运行期生成 traceCode 演示回填 */
    public static class FetchHandler implements NodeHandler {
        public String nodeCode() { return "sample-fetch"; }
        public String nodeName() { return "样例-数据拉取"; }

        public NodeResult execute(NodeContext ctx) throws Exception {
            log.info("sample-fetch execute attempt={} params={}", ctx.getAttempt(), ctx.inputParams());
            for (int i = 0; i < 10; i++) {
                Thread.sleep(200);
                ctx.checkInterrupted();
            }
            return NodeResult.ok(Map.of("rows", 42)).withTraceCode("TRC-" + ctx.getInstanceCode());
        }

        public void cleanup(NodeContext ctx) {
            log.info("sample-fetch cleanup traceCode={}", ctx.getTraceCode());
        }
    }

    /** 模拟下发：读取上游输出 */
    public static class PushHandler implements NodeHandler {
        public String nodeCode() { return "sample-push"; }

        public NodeResult execute(NodeContext ctx) {
            log.info("sample-push execute params={}", ctx.inputParams());
            return NodeResult.ok();
        }
    }
}
