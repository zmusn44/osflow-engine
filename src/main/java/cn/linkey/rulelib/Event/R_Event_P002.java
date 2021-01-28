package cn.linkey.rulelib.Event;

import cn.linkey.rule.rule.LinkeyRule;
import cn.linkey.workflow.wf.ProcessUtil;

import java.util.HashMap;

/**
 *
 * 等待所有分支完成才启动
 * <p>EventParams传的是nodeid，多个以逗号隔开</p>
 *
 * @author alibao Y , walkwithdream@163.com
 * <p>createTime 2021-01-28 14:38 </p>
 * @version v1.0
 */
public class R_Event_P002 implements LinkeyRule {
    @Override
    public String run(HashMap<String, Object> params) throws Exception {

        //params为运行本规则时所传入的参数
        String nodeid=(String)params.get("EventParams");
        boolean isCurrentNode= ProcessUtil.isCurrentNode(nodeid); //看是否有还有环节是活动的

        if(isCurrentNode){
            return "0"; //不能启动
        }else{
            return "1"; //可以启动
        }
    }
}
