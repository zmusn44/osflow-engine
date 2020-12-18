package cn.linkey.rulelib.S003;

import java.util.HashMap;
import cn.linkey.workflow.factory.BeanCtx;
import cn.linkey.rule.rule.LinkeyRule;
import cn.linkey.workflow.util.Tools;
import cn.linkey.workflow.wf.InsNode;
import cn.linkey.workflow.wf.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 本规则负责结束前置节点事件
 * 
 * @author Administrator
 *
 */
public class R_S003_B024 implements LinkeyRule {

    private static Logger logger = LoggerFactory.getLogger(R_S003_B024.class);

    @Override
    public String run(HashMap<String, Object> params) throws Exception {
        ProcessEngine linkeywf = BeanCtx.getLinkeywf();
        String processid = linkeywf.getProcessid();
        String runNodeid = (String) params.get("WF_RunNodeid"); //获得要运行的节点id
        if (Tools.isBlank(runNodeid)) {
           // BeanCtx.log("E", "未传入要运行的节点id=" + runNodeid);
            logger.info("未传入要运行的节点id=" + runNodeid);
            return "";
        }
        //BeanCtx.out("R_S003_B024要结束的节点="+runNodeid);

        //1.结束当前前置事件环节
        InsNode insNode = (InsNode) BeanCtx.getBean("InsNode");
        insNode.endNode(processid, BeanCtx.getLinkeywf().getDocUnid(), runNodeid);

        //2.推进到本环节的下一个节点(前置节点后面的节点一定是路由线)
        linkeywf.goToNextNode(runNodeid, params);

        return "";

    }
}
