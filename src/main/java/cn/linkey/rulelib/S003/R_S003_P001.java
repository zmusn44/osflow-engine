package cn.linkey.rulelib.S003;

import java.io.IOException;
import java.util.*;
import cn.linkey.orm.dao.Rdb;
import cn.linkey.orm.doc.Document;
import cn.linkey.rule.rule.LinkeyRule;
import cn.linkey.workflow.factory.BeanCtx;
import cn.linkey.workflow.util.Tools;
import cn.linkey.workflow.wf.ProcessEngine;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
/**
 * @author Mr.Yun
 * 统一审批任务同步
 * 8.0
 * 2020-07-29 15:07:38
 */

final public class R_S003_P001 implements LinkeyRule {


    // 一些固定的参数信息，请在这里进行配置
    final private String HttpServerUrl = "http://dev3.h3c.com:8080/bpm";
    final private String UniTask_URI = "";
    final private String UniTask_SID = "IFLOW";
    final private String UniTask_PWD = "";
    final private String CURR_SYSTEMID = "IFLOW";
    private Rdb rdb = BeanCtx.getRdb();

    @Override
    public String run(HashMap<String, Object> params) throws Exception {
        //params为运行本规则时所传入的参数
        ProcessEngine engine = BeanCtx.getLinkeywf(); //获得流程引擎实例
        Document doc = engine.getDocument(); //获得流程实例文档对像

        /**
         * 第1步：获取Rest接口请求前的header参数Map
         */
        HashMap<String, String> header = getHeaderMap();

        /**
         * 第2步：获取Rest接口请求前的body内的JSON串参数
         */
        HashMap<String,Object> param = getBodyJSONArgers(engine, doc);


        /**
         * 第3步：发起Rest请求，将数据提交到中台中做统一待办的显示
         */
       /* try {
            String res = httpPost(UniTask_URI, header, param.toJSONString());
        } catch (Exception e) {
        }*/
        //WorkFlowUpdate workFlowUpdate = BeanCtx.getWorkFlowUpdate();
        //workFlowUpdate.saveData(param);

        return "";
    }

    /**
     * 获取Rest接口请求前的header参数Map
     * @return
     */
    private HashMap<String, String> getHeaderMap() {
        HashMap<String, String> header = new HashMap<String, String>();
        header.put("sysid", UniTask_SID);
        header.put("syspwd", UniTask_PWD);
        header.put("userId", BeanCtx.getUserid());
        header.put("Content-Type", "application/json");
        return header;
    }

    /**
     * 获取Rest接口请求前的body内的JSON串参数
     * @param engine 流程引擎对象
     * @param doc 流程主文档对象
     * @return 参数
     */
    private HashMap<String,Object> getBodyJSONArgers(ProcessEngine engine, Document doc) throws Exception {
        HashMap<String,Object> param = new HashMap<String,Object>();

        // 获取流程模型配置信息
        Document pdoc = engine.getProcessModNodedoc();
//        String batchNode = pdoc.g("BatchNode") + ",";
//        String mobileNode = pdoc.g("MobileNode") + ",";
//        String noSMSNode = pdoc.g("NoSMSNode") + ",";
        String redirectUrl = pdoc.g("docRedirectUrl");//重定向链接

        String sURL = HttpServerUrl + "/rule?wf_num=R_S003_B052&wf_docunid=" + doc.g("WF_ORUNID");

        String sNodeID = doc.g("WF_CURRENTNODEID");
        if (Tools.isBlank(sNodeID)) {
            sNodeID += " ";
        }
        String sNodeName = doc.g("WF_CURRENTNODENAME");

//        String mobileAuthorSql = "select userid from BPM_InsUserList t where docunid='" + doc.getDocUnid() + "' and  instr('" + mobileNode + "',t.NODEID)>0  and status='Current'";
//        String mobileAuthor2 = rdb.getValueBySql(mobileAuthorSql);

        // 设置参数信息
        param.put("systemId", CURR_SYSTEMID);
        param.put("appId", doc.g("WF_APPID"));
        param.put("docunId", doc.g("WF_ORUNID"));
        param.put("subject", doc.g("SUBJECT"));
        param.put("nodeId", sNodeID);
        param.put("nodeName", sNodeName);
        param.put("addUserId", doc.g("WF_ADDNAME"));
        param.put("addUserName", doc.g("WF_ADDNAME"));
        param.put("applyTime", doc.g("WF_DOCCREATED"));
        param.put("authorId", doc.g("WF_AUTHOR"));
        param.put("authorName", doc.g("WF_AUTHOR"));
        param.put("processId", doc.g("WF_PROCESSID"));
        // 20200429 Mr.Yun 这里我认为应该使用流程实例的流程名称，而不是使用流程建模的流程名称
        param.put("processName", doc.g("wf_processname"));
        param.put("endUserId", doc.g("WF_ENDUSER"));
        param.put("timesTamp", doc.g("WF_LASTMODIFIED"));
        param.put("assigner", "");
        param.put("url", sURL);
        param.put("mobileAuthorId", doc.g("WF_AUTHOR"));

        String wf_status = doc.g("WF_status");
        if ("Current,Pause".indexOf(wf_status) != -1) {
            wf_status="APPROVING";
        } else if ("Draft".equals(wf_status)) {
            wf_status="WAITING";
        } else {
            wf_status="APPROVED";
        }
        param.put("status", wf_status);

        /*if (mobileNode.indexOf(sNodeID + ",") >= 0 || isContainNode(mobileNode, sNodeID)) {
            param.put("acceptType", "3");//PC&Mobile都使用
        } else {
            param.put("acceptType", "1");//仅PC使用
        }
        if (batchNode.indexOf(sNodeID + ",") >= 0) {
            param.put("isBatch", "1");//可以批量处理
        } else {
            param.put("isBatch", "0");//不能批量处理
        }
        if (noSMSNode.indexOf(sNodeID + ",") >= 0) {
            param.put("isSMS", "0");//不用移动端消息推送
        } else {
            param.put("isSMS", "1");//移动端消息推送
        }*/
        param.put("acceptType", "2");//仅移动使用
        param.put("isBatch", "0");//不能批量处理
        param.put("isSMS", "1");//移动端消息推送

        //移动端新建流程；回退首环节赋值重定向链接；首环节提交后清空重定向链接
        if (Tools.isNotBlank(redirectUrl)) {
            redirectUrl += doc.g("WF_ORUNID");
        } else {

          /*  ModNode modNode = (ModNode) BeanCtx.getBean("ModNode");
            boolean firstNode = modNode.isFirstNode(engine.getProcessid(), sNodeID);
            if (firstNode && "2,3,".indexOf(param.get("acceptType") + ",") != -1) {//回退首环节
                // 这里清除了很多代码，都是从不同的数据源取不同的redirectUrl地址
            }*/
        }
        param.put("redirectUrl", redirectUrl);

        param.put("userId",BeanCtx.getUserid());
        param.put("sysid",CURR_SYSTEMID);

        return param;
    }

    public boolean isContainNode(String sourcestr, String targetNode) {
        HashSet<String> sourceSet = Tools.splitAsSet(sourcestr);
        HashSet<String> targetSet = Tools.splitAsSet(targetNode);
        sourceSet.retainAll(targetSet);
        return sourceSet.size() != 0;
    }

    /**
     * 发送POST请求，发起Rest接口API的调用
     * @param url 调用API的URL
     * @param header 头信息
     * @param jsonStr body参数
     * @return 调用结果
     * @throws IOException 抛出异常
     */
    public static String httpPost(String url,HashMap<String, String> header,String jsonStr) throws IOException {
        CloseableHttpClient httpclient = HttpClients.custom().build();
        CloseableHttpResponse response=null;
        String str="{\"success\":false,\"message\":\"未获取到接口数据\"}";
        try {
            HttpPost httpPost = new HttpPost(url);
            for (String keyName : header.keySet()) {
                httpPost.addHeader(keyName,header.get(keyName));
            }
            StringEntity entity = new StringEntity(jsonStr,"utf-8");
            entity.setContentEncoding("UTF-8");
            entity.setContentType("application/json");//发送json数据需要设置contentType
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            HttpEntity rsEntity = response.getEntity();
            if (rsEntity != null) {
                str = EntityUtils.toString(rsEntity);
                if(Tools.isBlank(str)){
                    str="{\"success\":false,\"message\":\"未获取到接口数据\"}";
                }
                return str;
            }
        }catch (Exception e) {
            return "{\"success\":false,\"message\":\"访问接口报错\"}";
        }finally {
            if(response !=null)response.close();
        }
        return str;
    }
}