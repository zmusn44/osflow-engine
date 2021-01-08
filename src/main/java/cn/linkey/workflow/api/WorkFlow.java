package cn.linkey.workflow.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author Mr.Yun
 * 流程引擎运行类主要接口
 * 一切只为成就更好的您
 *  1.0
 * 2020/07/21 11:12
 */
public interface WorkFlow {

    /**
     * 获取表单显示模板和业务数据接口
     *  本接口主要为前端打开流程时提供流程的基本信息，
     *      如流程的文档数据、流程的当前节点信息、流程的下一节点信息、以及移动化配置的前端表单字段信息
     * @param docUnid 流程实例文档的id，与流程id 两者必填其一，也可两者都填写
     * @param processid 流程id，与流程实例文档的id 两者必填其一，也可两者都填写
     * @param userid 用户id
     * @return 流程基础信息
     *  其中 WF_CurrentNodeConfig 表示当前节点信息，WF_NextNodeConfig 表示下一节点列表信息，只有当流程为非只读状态，才会有信息
     *  这里还将返回特殊定制的前端JSON配置项，WF_FormShowConfig 用于前端页面对配置字段的渲染和显示
     *   WF_FormShowConfig （当前因未规范化未做实现）
     *
     *  示例结构如下：
     *  {"WF_Author_CN":"gw1833","WF_OrUnid":"5ebfa44c0ddfd04345097a00c316ba8e7c99","WF_ProcessNumber":"TP_FIRST",
     *  "WF_BusinessNum":"","WF_AddName":"gw1833","WF_Folderid":"","WF_Author":"gw1833","WF_CurrentNodeName":"审批",
     *  "WF_ProcessName":"第一次测试流程","Subject":"第一次测试流程","WF_DocNumber":"200924194015327",  ........ 流程主文档中的字段
     *  "WF_CurrentNodeConfig":[],
     *  "WF_FormShowConfig":[],
     *  "WF_NextNodeConfig":[],
     *  "WF_SucessFlag":"1","message":"ok",
     *  "success":true}
     */
    public JSONObject openProcess(String docUnid, String processid, String userid);

    /**
     * 获取审批过程信息接口
     * @param docUnid 流程实例文档的id
     * @return 返回结果，示例结构如下
     *   {"total":1,"data":[{"approver":"gw1833","approveType":"Agree","receiver":"gw1833",
     *      "processid":"b9d23b3106510048cc08ffd0321ea0209bff","processName":"第一次测试流程","nextNode":"审批",
     *      "docUnid":"5ebfa44c0ddfd04345097a00c316ba8e7c99","currentNode":"填单","approveOpinion":"提交。。",
     *      "approveTime":"2020-09-24 19:40:13","approveTypeVal":"同意"}],
     *   "success":true,"message":"ok"}
     */
    public JSONObject getApprovalInfo(String docUnid);

    /**
     * 运行流程统一接口
     *
     * @param processid        流程id
     * @param docUnid          流程实例文档的id
     * @param taskid           任务id
     * @param action           要执行的动作id
     * @param currentNodeid    当前节点id
     * @param nextNodeid       要提交的节点id
     * @param nextUserList     要提交的用户
     * @param copyUserList     要抄送的用户
     * @param userid           当前用户id
     * @param remark           审批意见
     * @param isBackFlag       设置回退后返回标记，如果为2表示回退后需要直接返回给回退者
     * @param reassignmentFlag 转交时是否需要转交者返回的标记1表示不需要2表示需要
     * @param maindata         传入的文档参数(JSON)
     * @return 以JSON的形式返回运行的结果，
     *  {"Status":"Error/ok","msg":"运行结果提示内容"}
     */
    public String runProcess(String processid, String docUnid, String taskid, String action, String currentNodeid,
                             String nextNodeid, String nextUserList, String copyUserList, String userid, String remark,
                             String isBackFlag, String reassignmentFlag, JSONObject maindata);


    /**
     * 获取所有流程信息
     * add by alibao 202010
     * @return  所有流程信息的JSONArray对象
     */
    public JSONArray getProcessMsg();


    /**
     * 获取用户待办信息
     * add by alibao 202010
     * @param userid 用户ID
     * @return 用户所有代办列表
     */
    public JSONArray getUserToDoInfo(String userid);


}
