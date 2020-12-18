package cn.linkey.rulelib.S003;

import cn.linkey.rule.rule.LinkeyRule;
import cn.linkey.workflow.factory.BeanCtx;
import cn.linkey.workflow.wf.InsNode;

import java.util.HashMap;

/*import cn.linkey.factory.BeanCtx;
import cn.linkey.rule.LinkeyRule;
import cn.linkey.wf.InsNode;*/

/**
 *  结束startEvent
 * @author admin
 *  8.0
 *  2014-05-10 20:51
 */
final public class R_S003_B050 implements LinkeyRule {
    @Override
    public String run(HashMap<String, Object> params) throws Exception {
        //params为运行本规则时所传入的参数
        String processid = BeanCtx.getLinkeywf().getProcessid();
        String runNodeid = (String) params.get("WF_RunNodeid"); //获得要运行的节点id

        //2.结束当前开始事件节点
        InsNode insNode = (InsNode) BeanCtx.getBean("InsNode");
        insNode.endNode(processid, BeanCtx.getLinkeywf().getDocUnid(), runNodeid);

        //3.本环节结束，继续推进到本开始节点的的后继节点
        BeanCtx.getLinkeywf().goToNextNode(runNodeid, params);

        return "";
    }
}