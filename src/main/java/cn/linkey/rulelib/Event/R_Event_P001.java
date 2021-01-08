package cn.linkey.rulelib.Event;

import cn.linkey.orm.doc.Document;
import cn.linkey.rule.rule.LinkeyRule;
import cn.linkey.workflow.factory.BeanCtx;
import cn.linkey.workflow.wf.ProcessEngine;

import java.util.HashMap;

/**
 * @author Mr.Yun
 * 流程事件-流程全局事件（EngineAfterInit）流程初始化后。
 *  主要用于在流程初始化后对业务进行额外处理的，例如指定下一节点，处理人等信息，以适应复杂的业务场景时使用
 * 一切只为成就更好的您
 *  事件，按一般计划，都应该存cn.linkey.rulelib.Event包下。
 *  1.0
 * 2020/08/05 10:23
 */
public class R_Event_P001 implements LinkeyRule {
    @Override
    public String run(HashMap<String, Object> params) throws Exception {
        ProcessEngine engine = BeanCtx.getLinkeywf(); // 获得流程引擎实例
        Document doc = engine.getDocument(); // 获得流程实例文档对像
        String enterEventParams = (String) params.get("EventParams");//事件配置中的事件参数（一般是JSON）
        //{"action":"Endusrt","List":[{"T100000,",111111},{T11111,000000}]}

        // *************************** 一些常用的参数，可以只取有用的即可 ***************************
        //当前节点id
        String currentNodeid = engine.getCurrentNodeid();
        //动作id
        String actionid = (String) params.get("WF_Action");
        //下一节点参数
        String nextNodeList = (String) params.get("WF_NextNodeid");
        //下一处理用户参数
        String nextUserList = (String) params.get("WF_NextUserList");
        //传阅用户
        String copyUserList = (String) params.get("WF_CopyUserList");
        //审批意见
        String remark = (String) params.get("WF_Remark");
        // *************************** 一些常用的参数，可以只取有用的即可 ***************************

        if("T00001".equals(currentNodeid) && "EndUserTask".equals(actionid)){
            doc.s("AAA","AAA");
            doc.s("TTT","TTT");

            return "";

        } else if("T00002".equals(currentNodeid) && "EndUserTask".equals(actionid)){

            String appuser = doc.g("WF_AddName");

            /*
            Obj obj= hasXXX();

            obj.lev;
            obj.3ld; ///
            obj.2ld;///
            obj.1ld;///

            if( obj.lev; ==  4){
                params.put("WF_NextNodeid","T00003");
                params.put("WF_NextUserList","obj.3ld");
            } else if ==3{
                params.put("WF_NextNodeid","T00004");
                params.put("WF_NextUserList","obj.2ld");
            } else if ==2{
                params.put("WF_NextNodeid","T00004");
                params.put("WF_NextUserList","obj.1ld");
            }*/

        }

        /**
         * 这里可以依据主业务表的字段，结合当前节点id,动作id对一些业务内容进行修改
         *  1、对业务数据的修改，doc.s("key","value");
         *  2、对一些参数的修改，params.put("WF_NextNodeid", "T00002"); 或 params.put("WF_NextUserList", "dongshiqiang gw1833");
         *      注意：不支持对WF_Action的修改。
         *  业务示例如下
         */
        if("EndUserTask".equals(actionid)){
            // 在提交文档的动作下的业务判断
            if("T00002".equals(currentNodeid)){
                if("1".equals(doc.g("key"))){
                    // 满足某条件时，修改下一节点的处理人
                    params.put("WF_NextUserList", "dongshiqiang gw1833");
                }
            } else if("T00003".equals(currentNodeid)){
                // 当满足某条件时：例如，当前处理人是二级主管，要提交给二级主管节点，这里应该要跳过二级主管节点提交给一级主管
                if(BeanCtx.getUserid().equals(nextUserList)){
                    // 假设，指定下一节点，跳过T00004二级主管，到T00005一级主管
                    params.put("WF_NextNodeid", "T00005");
                    params.put("WF_Remark", "跳过T00004节点");
                    //指定T00005的处理人，注意，这里必须要有T00003->T00005的路由线，否则可能会报错
                    params.put("WF_NextUserList", "dongshiqiang gw1833");
                }
            }
        }
        return "";
    }
}
