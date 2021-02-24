package cn.linkey.workflow.api;

import cn.linkey.orm.dao.Rdb;
import cn.linkey.orm.dao.impl.RdbImpl;
import cn.linkey.orm.doc.Document;
import cn.linkey.orm.doc.impl.DocumentsUtil;
import cn.linkey.rule.factory.BeanServer;
import cn.linkey.rule.rule.LinkeyRule;
import cn.linkey.workflow.factory.BeanCtx;
import cn.linkey.workflow.util.Tools;
import cn.linkey.workflow.wf.ProcessEngine;
import cn.linkey.workflow.wf.ProcessUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * @author Mr.Yun
 * 流程引擎运行类主要接口实现类
 * 一切只为成就更好的您
 * 1.0
 * 2020/07/21 13:29
 */
public class WorkFlowImpl implements WorkFlow {
    // 提示消息
    private String msg;
    private Connection conn;

    /**
     * 构造函数初始化
     * @param conn 必须要传入数据源对象
     */
    public WorkFlowImpl(Connection conn) {
        this.conn = conn;
        BeanCtx.setConnection(conn);
        Rdb rdb = new RdbImpl();
        rdb.setConnection(conn);
        BeanCtx.setRdb(rdb);
    }

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
    @Override
    public JSONObject openProcess(String docUnid, String processid, String userid) {
        JSONObject obj = new JSONObject();
        obj.put("message", "");
        obj.put("success", false);

        if(StringUtils.isBlank(userid)){
            obj.put("message", "参数当前用户身份为空");
            return obj;
        }
        if(StringUtils.isBlank(docUnid) && StringUtils.isBlank(processid)){
            obj.put("message", "存在必须参数流程id和文档id都为空");
            return obj;
        }

        try {
            // 获取流程信息，包含节点配置前端表单的配置字段JSON信息，用于前端解析表内容
            obj = getOpenInfo(docUnid, processid, userid);
        } catch (Exception e){
            e.printStackTrace();
            obj.put("message", "获取流程信息时错误");
        }
        return obj;
    }

    /**
     * 获取表单显示模板和业务数据接口
     *    本接口主要为前端打开流程时提供流程的基本信息，
     *    如流程的文档数据、流程的当前节点信息、流程的下一节点信息、以及移动化配置的前端表单字段信息
     * @param docUnid 流程实例文档的id
     * @param processid 与流程id
     * @param userid 用户id
     * @return 查询结果
     * @throws Exception
     */
    private JSONObject getOpenInfo(String docUnid, String processid, String userid) throws Exception {
        JSONObject object = new JSONObject();
        // 设置当前用户
        BeanCtx.setUserid(userid);
        Rdb rdb = BeanCtx.getRdb();//获取数据库查询对象

        // 获取当前流程文档doc
        Document mainDoc = BeanCtx.getDocumentBean("");
        if(StringUtils.isNotBlank(docUnid)){
            String sql = "select * from bpm_alldocument a where a.WF_ORUNID ='" + docUnid + "'";
            mainDoc = rdb.getDocumentBySql("bpm_alldocument", sql);
        }

        // 获取流程属性表
        if(StringUtils.isBlank(processid) && !mainDoc.isNull()){
            // 取流程主文档中的流程id
            processid = mainDoc.g("WF_PROCESSID");
        }
        String sql = "select * from bpm_modprocesslist m where m.processid='" + processid + "'";

        Document moddoc = rdb.getDocumentBySql("bpm_modprocesslist", sql);
        if (moddoc.isNull()) {
            object.put("success", false);
            object.put("message", "未获取到流程建模记录");
            return object;
        }

        // 标注当前流程是否为只读，并给定默认值为非只读，即表示在途的状态
        boolean isReadOnly = false;
        // 在这里判断当前流程的状态，是否归档对流程的输出信息是不一样的
        if("ARC".equals(mainDoc.g("WF_STATUS"))){
            isReadOnly = true;
        }

        // 初始化引擎
        ProcessEngine linkeywf = new ProcessEngine();
        BeanCtx.setLinkeywf(linkeywf); //把工作流引擎对像设置为全局变量对像
        //如果没有传入文档unid则说明要启动一个新文档
        if (StringUtils.isBlank(docUnid)) {
            docUnid = rdb.getNewUnid();
        }
        linkeywf.init(processid, docUnid, BeanCtx.getUserid(), ""); //初始化工作流引擎

        if(!isReadOnly){
            // 重新获取流程的只读状态
            isReadOnly = linkeywf.isReadOnly();
        }

        // 输出当前文档的所有字段内容
        linkeywf.getDocument().copyAllItems(mainDoc);
        mainDoc.s("WF_SucessFlag", "1");
        // mainDoc.s("WF_IsProcessAdmin", String.valueOf(linkeywf.isProcessOwner())); // 暂不支持组织的判断
        mainDoc.s("WF_LockStatus", linkeywf.getLockStatus());
        mainDoc.s("WF_ProcessName", linkeywf.getProcessName());
        if(isReadOnly){
            // 流程为只读状态，给个空值
            mainDoc.s("WF_CurrentNodeid", "");
        } else {
            mainDoc.s("WF_CurrentNodeid", linkeywf.getCurrentNodeid());
        }

        // 获取节点配置信息
        JSONObject nodeobj = getNodeConfigInfo(linkeywf, isReadOnly);
        // 追加返回的内容
        object.putAll(mainDoc.getAllItems());
        object.putAll(nodeobj);

        // 追加表单字段配置 202010
        object.put("WF_FormShowConfig",getFormConfig(processid));

        object.put("message", "ok");
        object.put("success", true);
        return object;
    }

    /**
     * 获取是特定状态下的流程信息，包含以下信息
     *  其中 WF_CurrentNodeConfig 表示当前节点信息，WF_NextNodeConfig 表示下一节点列表信息，只有当流程为非只读状态，才会有信息
     *  其中 WF_FormShowConfig 表示特殊定制的前端JSON配置项，一般用于前端页面对配置字段的渲染和显示，不管是否是只读状态
     * @param linkeywf 流程引擎对象
     * @param isReadOnly 是否只读状态，true 表示只读
     * @return 反回的信息
     * @throws Exception
     */
    private JSONObject getNodeConfigInfo(ProcessEngine linkeywf, boolean isReadOnly) throws Exception {
        JSONObject object = new JSONObject();
        // 当前节点配置信息（多个）
        JSONArray currentNodeConfig = new JSONArray();
        // 下一节点配置信息（多个）
        JSONArray nextNodeConfig = new JSONArray();

        // 只有在非只读状态，且当前节点不为空时，才分析当前节点以及下一节点的配置信息，否则反回空即可
        if (!isReadOnly && linkeywf.getCurrentModNodeDoc() != null) {
            linkeywf.getCurrentModNodeDoc().s("WF_Actionid", linkeywf.getCurrentActionid());
            // 当前节点配置信
            JSONObject currentConfig = new JSONObject();
            currentConfig.putAll(linkeywf.getCurrentModNodeDoc().getAllItems());

            currentNodeConfig.add(currentConfig);

            // 找到所有后继节点的配置信息
            int canNextNodeFlag = 0;
            if (Tools.isNotBlank(linkeywf.getCurrentNodeid())) {
                canNextNodeFlag = linkeywf.canSelectNodeAndUser(); //获得是否可以选择后继环节返回0表示可以
                if (canNextNodeFlag == 0) { //看是可以显示节点和人员选项
                    //这里要找到所有路由和节点输出节点的配置文档对像
                    LinkedHashSet<Document> nextNodeDc = new LinkedHashSet<Document>();
                    ProcessUtil.getNextNodeDoc(linkeywf.getProcessid(), linkeywf.getCurrentNodeid(), nextNodeDc);
                    for (Document doc : nextNodeDc) {
                        JSONObject row = new JSONObject();
                        row.putAll(doc.getAllItems());

                        nextNodeConfig.add(row);
                    }
                }
            }
        }
        object.put("WF_CurrentNodeConfig", currentNodeConfig);
        object.put("WF_NextNodeConfig", nextNodeConfig);

//        // 返回特殊定制的前端JSON配置项，WF_FormShowConfig 用于前端页面对配置字段的渲染和显示
//        JSONArray formShowConfig = new JSONArray();
//        object.put("WF_FormShowConfig", formShowConfig);



        return object;
    }


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
    @Override
    public JSONObject getApprovalInfo(String docUnid) {
        JSONObject obj = new JSONObject();
        try {
            //String sql = "select t.* from BPM_AllRemarkList t where t.DOCUNID='" + docUnid + "' order by t.EndTime ";

            // add by alibao 202010 获取审批记录的SQL，添加当前正在审批信息
            /*
            (select PROCESSID,PROCESSNAME,USERID, NEXTUSERLIST,Nodeid,NODENAME,NEXTNODENAME,StartTime,EndTime,REMARK from BPM_AllRemarkList  where DocUnid='7662dfff0689804aac0b2fa098ab515968f1' order by EndTime)
            union all
            (select i.PROCESSID,(select NodeName from bpm_modprocesslist where Processid = i.PROCESSID) PROCESSNAME,i.USERID,'' NEXTUSERLIST, i.Nodeid,i.NODENAME,'' NEXTNODENAME, i.StartTime,i.EndTime,'' REMARK from BPM_InsUserList i where i.DocUnid='7662dfff0689804aac0b2fa098ab515968f1' and i.Status='Current' order by i.StartTime)
            * */

            String sql = "(select PROCESSID,PROCESSNAME,USERID, NEXTUSERLIST,Nodeid,NODENAME,NEXTNODENAME,ACTIONID,StartTime,EndTime,REMARK from BPM_AllRemarkList  where DocUnid='" + docUnid
                    + "' order by EndTime) union all (select i.PROCESSID,(select NodeName from bpm_modprocesslist where Processid = i.PROCESSID) PROCESSNAME,i.USERID,'' NEXTUSERLIST, i.Nodeid,i.NODENAME,'' NEXTNODENAME, '',i.StartTime,i.EndTime,'' REMARK from BPM_InsUserList i where i.DocUnid='" + docUnid
                    + "' and i.Status='Current' order by i.StartTime)";

            LinkedHashSet<Document> docs = BeanCtx.getRdb().getAllDocumentsSetBySql("BPM_AllRemarkList", sql);
            // 用于存审批记录
            JSONArray rows = new JSONArray();
            for (Document doc : docs) {
                JSONObject row = new JSONObject();
                row.put("processid", doc.g("PROCESSID"));
                row.put("processName", doc.g("PROCESSNAME"));
                row.put("docUnid", docUnid);
                // 操作人员
                row.put("approver", doc.g("USERID"));
                // 接收人员
                row.put("receiver", doc.g("NEXTUSERLIST"));
                // 当前操作人员的操作节点
                row.put("currentNode", doc.g("NODENAME"));
                // 节点id
                row.put("currentNodeid", doc.g("Nodeid"));

                // 当前操作人员提交的下一节点
                row.put("nextNode", doc.g("NEXTNODENAME"));

                // 开始时间
                row.put("startTime", doc.g("startTime"));
                // 当前操作人员的审批完成时间
                row.put("approveTime", doc.g("ENDTIME"));

                if(Tools.isBlank(doc.g("ENDTIME"))){
                    row.put("approveType", "handle");
                    row.put("approveTypeVal", "处理中");
                }
                else if ("GoToFirstNode,GoToPrevNode,GoToPrevUser,ReturnToAnyNode,CancelProcess,EndUser,Undo"
                        .toLowerCase().indexOf(doc.g("ACTIONID").toLowerCase()) != -1) {
                    row.put("approveType", "Reject");
                    row.put("approveTypeVal", "拒绝");
                } else if ("GoToOthers".toLowerCase().indexOf(doc.g("ACTIONID").toLowerCase()) != -1) {
                    row.put("approveType", "Transfer");
                    row.put("approveTypeVal", "转发");
                } else {
                    row.put("approveType", "Agree");
                    row.put("approveTypeVal", "同意");
                }
                // 当前操作人员操作审批意见
                row.put("approveOpinion", doc.g("REMARK"));
                rows.add(row);
            }
            obj.put("data",rows);
            obj.put("total", rows.size());
            obj.put("success", true);
            obj.put("message", "ok");
        } catch (Exception e) {
            e.printStackTrace();
            obj.put("success", false);
            obj.put("total", 0);
            obj.put("message", "获取审批记录出错");
        }
        return obj;
    }

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
     * @return 返回流程运行结果
     */
    @Override
    public String runProcess(String processid, String docUnid, String taskid, String action, String currentNodeid,
                             String nextNodeid, String nextUserList, String copyUserList, String userid, String remark,
                             String isBackFlag, String reassignmentFlag, JSONObject maindata) {

        // 将信息组装到参数中
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("WF_Processid", processid);
        params.put("WF_DocUnid", docUnid);
        params.put("WF_Taskid", taskid);
        params.put("WF_Action", action);
        params.put("WF_CurrentNodeid", currentNodeid);
        params.put("WF_NextNodeid", nextNodeid);
        params.put("WF_NextUserList", nextUserList);
        params.put("WF_CopyUserList", copyUserList);
        params.put("userid", userid);
        params.put("WF_Remark", remark);
        params.put("WF_IsBackFlag", isBackFlag);
        params.put("WF_ReassignmentFlag", reassignmentFlag);
        params.put("maindata", maindata);

        // 判断参数信息，是否符合继续执行的条件
        boolean flage = isCanSubmit(processid, action, nextNodeid, nextUserList, userid, params);
        if (!flage) {
            return getMsg();
        }

        // 设置当前用户
        BeanCtx.setUserid(userid);

        // 调用规则引擎处理流程并反回信息
        LinkeyRule linkeyRule = (LinkeyRule) BeanServer.getBeanByRuleNum("R_S003_B035");
        try {
            // 执行规则引擎，并设置执行结果
            setMsg(linkeyRule.run(params));
        } catch (Exception e) {
            setMsg("{\"Status\":\"Error\",\"msg\":\"提交流程时出错：" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
        return getMsg();
    }

    /**
     * 判断参数信息，是否符合继续执行的条件
     * @param processid 流程id
     * @param action 主要的判断类型，不同类型对参数的要求不一致
     * @param nextNodeid 下一节点列表
     * @param nextUserList 下一节点用户列表
     * @param userid 当前用户身份
     * @param params 运行参数，传入主要是为了方便修改其中的引用值
     * @return true 通过验证，false 不通过验证
     */
    private boolean isCanSubmit(String processid, String action, String nextNodeid, String nextUserList, String userid, HashMap<String, Object> params) {
        if (StringUtils.isBlank(processid) || StringUtils.isBlank(action) && StringUtils.isBlank(userid)) {
            setMsg("{\"Status\":\"Error\",\"msg\":\"提交流程验证时出错：必须参数中存在空参数！\"}");
            return false;
        }

        if("EndUserTask".equals(action)){
            // 正常的办理完成，理论上节点id必须，用户信息在归档时非必须
            if(StringUtils.isBlank(nextNodeid)){
                setMsg("{\"Status\":\"Error\",\"msg\":\"提交流程验证时出错：提交节点参数为空！\"}");
                return false;
            }
        }
        else if("GoToOthers".equals(action)){
            // 转他人处理，节点id非必填，用户信息必填
            if(StringUtils.isBlank(nextUserList)) {
                setMsg("{\"Status\":\"Error\",\"msg\":\"提交流程验证时出错：转交用户参数为空！\"}");
                return false;
            }
            if(StringUtils.isNotBlank(nextNodeid)){
                // 转交时，下一节点不用填写，否则可能会出现空指针错误
                params.put("WF_NextNodeid", "");
            }
        }
        else if("ReturnToAnyNode".equals(action) || "GoToAnyNode".equals(action)){
            // 回退任意环节-（ReturnToAnyNode-R_S003_B042），
            if(StringUtils.isBlank(nextNodeid) || StringUtils.isBlank(nextUserList)) {
                // 两个参数必须存在
                setMsg("{\"Status\":\"Error\",\"msg\":\"提交流程验证时出错：节点或用户参数为空！\"}");
                return false;
            }
        }
        else {
            /**
             * 以下动作id相应执行事件规则中有体现，不需要任何节点和用户信息
             *  回退首环节-（GoToFirstNode-R_S003_B004），回退上一环节-（GoToPrevNode-R_S003_B005），回退上一用户-（GoToPrevUser-R_S003_B006）
             *  返回给转交者-BackToDeliver-R_S003_B039），返回给回退者-（BackToReturnUser-R_S003_B041），归档-（GoToArchived-R_S003_B057）
             *  提交下一会签用户-（GoToNextParallelUser-R_S003_B043），流程撤销-（CancelProcess-R_S003_B096）
             *  收回文档-（Undo-R_S003_B007），暂停-（Pause-R_S003_B054），恢复-（UnPause-R_S003_B055）
             *
             * 暂不支持的动作id
             *  后台启动用户任务-（StartUser-R_S003_B072），后台结束用户任务-（EndUser-R_S003_B073）  -- 用户和节点不为空
             *  后台结束节点-（EndNode-R_S003_B074）  --节点不为空
             *
             * 不支持前端的操作id
             *  结束当前环节并推进到下一环节-（GoToNextNode-R_S003_B080），引擎中调用一般处理自动运行的功能
             */
        }
        return true;
    }


    /**
     * 获取所有流程信息
     * add by alibao 202010
     * @return 所有流程信息的JSONObject对象
     */
    @Override
    public JSONArray getProcessMsg(){

        String sql = "select * from BPM_ModProcessList order by WF_DocCreated desc";
//        LinkedHashSet<Document> docSets = BeanCtx.getRdb().getAllDocumentsSetBySql(sql);
//        JSONArray processMsg = JSONArray.parseArray(DocumentsUtil.dc2json(docSets, ""));

        Document[] docs = BeanCtx.getRdb().getAllDocumentsBySql(sql);
        JSONArray processMsg = JSONArray.parseArray(DocumentsUtil.dc2json(docs, ""));

        return processMsg;
    }

    /**
     * 用户所有代办列表
     * @param userid 用户唯一ID，如果为空则返回所有用户待办
     * @return 返回用户待办列表
     */
    @Override
    public JSONArray getUserToDoInfo(String userid) {

        String sql = "select * from bpm_usertodo ";

        if(Tools.isNotBlank(userid)){
            sql += " where Userid='"+ userid + "'";
        }
        sql += " ORDER BY WF_LastModified desc;";

        LinkedHashSet<Document> docSets = BeanCtx.getRdb().getAllDocumentsSetBySql(sql);
        JSONArray todoJSONArr = JSONArray.parseArray(DocumentsUtil.dc2json(docSets, ""));

        return todoJSONArr;
    }

    /**
     * 表单字段配置
     * add by alibao 202010
     *
     * @param processid 流程ID
     * @return 表单字段配置JSON
     */
    public JSONObject getFormConfig(String processid) {

        String sql = "select formConfig from bpm_modprocesslist where Processid = '" + processid + "'";
        Rdb rdb = BeanCtx.getRdb();
        String formConfig = rdb.getValueBySql(sql);

        return JSONObject.parseObject(formConfig);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
