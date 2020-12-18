package cn.linkey.rulelib.S003;

import java.util.HashMap;
import java.util.HashSet;
import cn.linkey.orm.dao.Rdb;
import cn.linkey.rule.rule.LinkeyRule;
import cn.linkey.workflow.factory.BeanCtx;
import cn.linkey.workflow.util.Tools;
import cn.linkey.workflow.wf.NodeUser;
import cn.linkey.workflow.wf.ProcessEngine;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 运行工作流引擎
 *
 * <pre>
 *    5.准备运行工作流的map参数************************************开始
 *    全部可用参数如下：
 *    WF_Processid          流程unid
 *    WF_DocUnid            文档unid
 *    WF_Action             动作id标识
 *    WF_NextNodeid         提交下一环节类型为HashSet(String)
 *    WF_NextUserList       提交的下一环节参与者类型HashMap(String,String);key为 COPYUSER时表示传阅用户
 *    WF_NextNodeUser       所有提交的用户名称列表多个用逗号分隔，此值从WF_NextUserList参数中拆分而来
 *    WF_NextUserDept       提交下一环节的参与者所指定的部门id map参数，兼职或多架构时可以强制让用户以某个身份来处理
 *    WF_StopNodeType       停止推进的节点类型 String
 *    WF_UsePostOwner       强制使用提交的WF_NextUserList参数的用户作为参与者 String
 *    WF_SendSms            发送手机短信标记 String
 *    WF_IsBackFlag         回退后是否需要返回标记1表示再次提交，2表示直接返回 String
 *    WF_ReassignmentFlag   转交后是否需要返回标记1表示不需要，2表示需要返回 String
 *    WF_AllRouterPath      使用指定的路径集合进行运行 String
 *    WF_RunActionid        动作标识由WF_Action传入
 *    WF_RunNodeid          要运行的节点，一般由Action规则根据后续节点生成
 *    WF_Remark             审批意见
 *    WF_IsAgree            会签时Y表示同意，N表示不同意
 *    WF_SubNextUserList    要提交的子流程的目标节点用户
 * </pre>private Rdb rdb = BeanCtx.getRdb();
 *
 * @author Administrator
 */
public class R_S003_B035 implements LinkeyRule {
    private  Rdb rdb = BeanCtx.getRdb();
    private static Logger logger = LoggerFactory.getLogger(R_S003_B035.class);
    @Override
    public String run(HashMap<String, Object> nodeParams) throws Exception {
        long ts = System.currentTimeMillis();
        JSONObject alertmsg = new JSONObject();

        String processid = (String) nodeParams.get("WF_Processid");
        String docUnid = (String) nodeParams.get("WF_DocUnid");
        String taskid = (String) nodeParams.get("WF_Taskid");//任务id
        String actionid = (String) nodeParams.get("WF_Action");//动作id
        JSONObject maindata = (JSONObject) nodeParams.get("maindata");//主表的审批数据
        try {
            if (!Tools.isString(docUnid) || !Tools.isString(processid)) {
                alertmsg.put("msg", "Error:WF_DocUNID or WF_Processid is null!");
                alertmsg.put("Status", "Error");
                return alertmsg.toJSONString();
            }

            //1.开启事务并初始化工作流引擎
            rdb.setAutoCommit(false); //开启事务
            ProcessEngine linkeywf = new ProcessEngine();
            BeanCtx.setLinkeywf(linkeywf); //把工作流引擎对像设置为全局变量对像
            BeanCtx.setMainData(maindata);//将主文档内容添加到全局变量中
            linkeywf.init(processid, docUnid, BeanCtx.getUserid(), taskid); //初始化工作流引擎

            //2.触发流程初始化后事件，(20200805添加，主要用于在流程运行前对业务进行处理的，例如指定下一节点，处理人等信息)
            BeanCtx.getEventEngine().run(processid, "Process", "EngineAfterInit", nodeParams);

            //增加调试功能
            if (linkeywf.isDebug()) {
                logger.debug("*******流程运行调试开始流程id为:" + linkeywf.getProcessid() + "实例id为:" + linkeywf.getDocUnid() + " *************");
            }

            //3.准备节点参数
            String nextNodeList = (String) nodeParams.get("WF_NextNodeid");
            HashSet<String> nextNodeSet;//提交的后继节点列表
            if (Tools.isNotBlank(nextNodeList)) {
                nextNodeSet = Tools.splitAsSet(nextNodeList); //把节点变为set集合
            }
            else {
                nextNodeSet = new HashSet<String>(); //给一个空值
            }

            //4.准备用户参数
            NodeUser insNodeUser = new NodeUser();
            HashMap<String, String> nextUserMap = new HashMap<String, String>(); //提交后继环节的用户列表
            HashMap<String, HashMap<String, String>> nextUserDeptMap = new HashMap<String, HashMap<String, String>>(); //key为节点id,提交后继环节的用户和部门列表,user01#Deptid,user02#Deptid

            //用户格式为8.0版本user01,user02或者user01$T10003,user02T10002
            //8.1版本增加了如下格式支持 user01$T10003#DP1002,user02T10002#DP1003 后面可以支持强制指定用户所处的部门id
            String nextUserList = (String) nodeParams.get("WF_NextUserList");
            if (Tools.isNotBlank(nextUserList)) {
                //有节点有用户
                if (Tools.isNotBlank(nextNodeList)) {
                    for (String nodeid : nextNodeSet) {
                        nextUserMap.put(nodeid, insNodeUser.getNodeUser(nextUserList, nodeid));
                        nextUserDeptMap.put(nodeid, insNodeUser.getNodeUserAndDept(nextUserList, nodeid));//获得要提交的用户的部门id,如果有部门指定时有用，没有也可以为空
                    }
                }
                else {
                    nextUserMap.put("ALL", nextUserList); //没有节点只有用户设置为全部
                    nextUserDeptMap.put("ALL", insNodeUser.getNodeUserAndDept(nextUserList, "*"));//获得要提交的用户的部门id,如果有部门指定时有用，没有也可以为空
                }
                linkeywf.getDocument().s("WF_NextNodeUser", insNodeUser.getNodeUser(nextUserList, "*")); //所有提交的用户
            }

            //5.准备传阅用户
            String copyUserList = (String) nodeParams.get("WF_CopyUserList");
            if (Tools.isNotBlank(copyUserList)) {
                nextUserMap.put("COPYUSER", copyUserList); //要抄送的用户
            }

            //准备启动流程的节点和用户参数
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("WF_NextNodeid", nextNodeSet); //要提交的节点类型为HashSet<String> 值为nodeid多个逗号分隔
            params.put("WF_NextUserList", nextUserMap); //要提交的用户类型为HashMap<String,String> key为nodeid,value为userid多个逗号分隔,不包含部门id信息
            params.put("WF_NextUserDept", nextUserDeptMap);//要提交的用户和部门的组合，用来强制指定用户以什么部门身份来审批文档,兼职时有用 key为节点nodeid,value为用户与部门的map对像
            params.put("WF_SubNextUserList", "");//要提交的子流程的目标用户,暂时不支持多节点提交，也就是一次只能提交一个子流程节点

            //5.1设置发送手机短信标记
            String sendSms = (String) nodeParams.get("WF_SendSms");
            if (Tools.isNotBlank(sendSms)) {
                params.put("WF_SendSms", sendSms);//发送手机短信标记
            }

            //5.2设置回退后返回标记，如果为2表示回退后需要直接返回给回退者
            String isBackFlag = (String) nodeParams.get("WF_IsBackFlag");
            if (Tools.isNotBlank(isBackFlag)) {
                params.put("WF_IsBackFlag", isBackFlag);
            }

            //5.3设置转他人处理并返回标记
            String ReassignmentFlag = (String) nodeParams.get("WF_ReassignmentFlag"); //转交时是否需要转交者返回的标记1表示不需要2表示需要
            if (Tools.isNotBlank(ReassignmentFlag)) {
                params.put("WF_ReassignmentFlag", ReassignmentFlag);
            }

            //5.4设置审批意见
            params.put("WF_Remark", (String) nodeParams.get("WF_Remark"));

            if (linkeywf.isDebug()) {
                logger.debug("Debug:运行Actionid==" + actionid);
                logger.debug("Debug:提交流程引擎的运行参数为==" + params.toString());
            }
            //************************************************************准备结束

            //6.提交工作流引擎运行
            String msg = linkeywf.run(actionid, params);
            if (linkeywf.isDebug()) {
                logger.debug("Debug:" + msg);
            }

            //7.如果出错则数据需要回滚
            if (BeanCtx.isRollBack()) {
                //获得回滚后的提示信息
                if (Tools.isBlank(linkeywf.getRollbackMsg())) {
                    msg = BeanCtx.getMsg("Engine", "Error_EngineRun");
                }
                else {
                    msg = linkeywf.getRollbackMsg();
                }
                nodeParams.put("ErrorType", "RollBack");
                BeanCtx.getEventEngine().run(processid, "Process", "EngineRunError", nodeParams); //注册流程运行出错后的事件

            }

            // 添加事务的提交动作，否则无法保存流转过程数据
            rdb.commit();
            rdb.setAutoCommit(true);

            //增加调试功能
            if (linkeywf.isDebug()) {
                long te = System.currentTimeMillis();
                logger.debug("流程提交总消耗时间=" + (te - ts));
                logger.debug("*******流程运行调试信息输出结束*************");
            }

            alertmsg.put("msg", msg);
            alertmsg.put("Status", "ok");
            // alertmsg.put("isApp", (BeanCtx.isAndroid() || BeanCtx.isIOS()) + "");
        }
        catch (Exception e) {
            logger.error("流程运行时出错");//这里会调用setRollback(true);对流程数据进行回滚
            String msg = BeanCtx.getMsg("Engine", "Error_EngineRun");
            alertmsg.put("msg", msg);
            alertmsg.put("Status", "Error");
            nodeParams.put("ErrorType", "Exception");
            BeanCtx.getEventEngine().run(processid, "Process", "EngineRunError", nodeParams); //注册流程运行出错后的事件,流程出错事件中不能用默认链接操作数据库，因为会被回滚，要建一个新的链接对像
        }
        return alertmsg.toJSONString();
    }
}
