package cn.linkey.flowdesign.api;

import cn.linkey.orm.doc.impl.DocumentsUtil;
import cn.linkey.workflow.util.Tools;
import cn.linkey.orm.dao.Rdb;
import cn.linkey.orm.doc.Document;
import cn.linkey.orm.factory.BeanCtx;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class FlowchartImp implements FlowChart {


    /**
     * 保存流程图模型数据接口
     * 保存流程图到表：BPM_ModGraphicList中，保存前必须验证在BPM_ModProcessList中有对应processid的流程图配置信息
     *
     * @param processid 流程id，必须
     * @param flowJSON  流程图模型数据，必须
     * @return {"status","0/1","msg":"提示信息"}
     */
    @Override
    public JSONObject saveFlowChartGraphic(String processid, String flowJSON) {
        Rdb rdb = BeanCtx.getRdb();
        JSONObject resultJson = new JSONObject();
        String sql = "select Processid from BPM_ModProcessList where Processid='" + processid + "'";
        if (rdb.hasRecord(sql)) {
            sql = "select * from BPM_ModGraphicList where Processid='" + processid + "'";
            Document doc = rdb.getDocumentBySql(sql);
            doc.s("GraphicBody", flowJSON);
            doc.s("Processid", processid);
            doc.s("FlowType", "2");
            doc.save();
            resultJson.put("Status", "1");
        } else {
            resultJson.put("Status", "0");
            resultJson.put("msg", "请在空白处点击键并在过程属性中指定流程的名称!");
        }
        return resultJson;
    }

    /**
     * 获取流程图模型数据接口
     * 前端获得数据后，依据Processid和flowJSON的内容，重新将保存过的流程图模型进行渲染显示
     *
     * @param processid 流程id，非必须，为空时表示新建流程
     * @return {"status","0/1","msg":"提示信息","Processid":"36位UUID","flowJSON":"流程图模型数据"}
     */
    @Override
    public JSONObject getFlowChartGraphic(String processid) {
        String processName = "", sql = "";
        Rdb rdb = BeanCtx.getRdb();
        JSONObject resultJson = new JSONObject();
        if (Tools.isBlank(processid)) {
            processid = rdb.getNewUnid();
            processName = "新建流程";
        } else {
            sql = "select * from BPM_ModProcessList where Processid='" + processid + "'";
            Document doc = rdb.getDocumentBySql(sql);
            if (doc.isNull()) {
                resultJson.put("0", "流程(" + processid + ")不存在!");
                return resultJson;
            } else {
                processName = doc.g("NodeName");
            }
        }
        String sql1 = "select GraphicBody from bpm_modgraphiclist where Processid='" + processid + "' and FlowType='2'";
        String flowJSON = rdb.getValueBySql(sql1);

        resultJson = JSONObject.parseObject("{\"Status\":\"1\",\"msg\":\"" + processName + "\",\"flowJSON\": " + (Tools.isBlank(flowJSON) ? "\"\"" : flowJSON) + ",\"Processid\": \"" + processid + "\" }");
        return resultJson;
    }


    /**
     * 保存各节点类型的统一请求接口，这里依据节点id进行类另和保存表的区分
     * 节点类型包含：全局属性（Process，对应表 bpm_modprocesslist），
     * 人工节点（userTask/T10001，T开头，对应表 Bpm_Modtasklist），
     * 自由节点（businessRuleTask/T10003，T开头，对应表 Bpm_Modtasklist），
     * 路由节点（sequenceFlow/R10005，R开头，对应表 Bpm_ModSequenceflowList），
     * 网关节点（Gateway/G10007，G开头，对应表 Bpm_Modgatewaylist），
     * 事件节点（Event/E10009，E开头，对应表 Bpm_Modeventlist），
     * 开始节点（startEvent/E10011，E开头，对应表 Bpm_Modeventlist），
     * 结束节点（endEvent/E10013，E开头，对应表 Bpm_Modeventlist）.
     * 使用nodeid的开始字符，可以决定保存的表名，再使用流程id，两者做为条件，可以找到表中唯一的一条记录，
     * 若记录不存在则insert，若记录存在则update
     * 请注意：formParmes中有不少的字段，在对应的表中是不存在这些表字段的，
     * 所以实现存储时是将数据存在到表中的XmlData大字段的，这里建议使用ORM存储引擎中的Document对象进行存储
     *
     * @param processid   流程id，必须
     * @param nodeid      节点id，必须
     * @param extNodeType 节点类型，非必须，一般用于后端判断某些节点某些字段必填
     * @param formParmes  非必须，大多数据情况下是有值的，节点中表单需要保存的参数，仅对传入的参数进行保存，
     *                    这里为了后续扩展方便，不使用单独的字段，请遍历formParmes对象进行保存，需要保存什么，交给前端进行选择和决断
     * @return {"status","0/1","msg":"提示信息"}
     */
    @Override
    public JSONObject saveFlowChartModByNodeType(String processid, String nodeid, String extNodeType,
                                                 JSONObject formParmes) {
        JSONObject json = new JSONObject();
        if (extNodeType.equals("Process")) {
            json = saveProcess(processid, nodeid, extNodeType, formParmes);
        } else {
            json = saveNode(processid, nodeid, extNodeType, formParmes);
        }

        return json;
    }

    /**
     * 获取各节点类型的统一请求接口，这里依据节点id进行类另和保存表的区分
     * 节点类型包含：参考saveFlowChartModByNodeType中的内容
     * 使用nodeid的开始字符，可以决定保存的表名，再使用流程id，两者做为条件，可以找到表中唯一的一条记录
     * 请注意：获取数据时，需要将XmlData大字段的内容一并获取，并反回，这里建议使用ORM存储引擎中的Document对象进行处理
     * 前端获得数据后，依据formdata的内容，结合相应的表单字段，设置进专表单中进行展示显示历史保存配置。
     *
     * @param processid 流程id，必须
     * @param nodeid    节点id，必须（节点id，为前端流程图设计器生成）
     * @return {"status","0/1","msg":"提示信息","formdata":{"field1":"字段1","field2":"字段1"......}}
     */
    @Override
    public JSONObject getFlowChartModByNodeType(String processid, String nodeid) {
        Rdb rdb = BeanCtx.getRdb();
        JSONObject resultJson = new JSONObject();
        String nodeTableName = Tools.getNodeTableName(processid, nodeid); //节点所在数据库表
        String sql = "select * from " + nodeTableName + " where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
        Document doc = rdb.getDocumentBySql(sql);
        resultJson.put("基本属性", JSONObject.parse(doc.toJson()));

        sql = "select * from  BPM_EngineEventConfig where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
        Document[] docs = rdb.getAllDocumentsBySql(sql);
        JSONArray list = new JSONArray();
        for (Document d : docs) {
            list.add(d.toJson());
        }
        resultJson.put("事件设置", list);

        sql = "select * from  BPM_ModProcessList where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
        doc = rdb.getDocumentBySql(sql);
        resultJson.put("归档设置", JSONObject.parse(doc.toJson()));

        if (nodeid.substring(0, 1).equals("T")) {
            nodeTableName = "BPM_MailConfig";
            sql = "select * from  " + nodeTableName + " where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
            docs = rdb.getAllDocumentsBySql(sql);
            list.clear();
            for (Document d : docs) {
                list.add(d.toJson());
            }
            resultJson.put("邮件设置", list);
        }

        sql = "select * from  BPM_ModTaskList where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
        doc = rdb.getDocumentBySql(sql);
        resultJson.put("移动设备", JSONObject.parse(doc.toJson()));

        return resultJson;
    }


    /**
     * 保存各节点类型的事件统一请求接口，事件统一保存到表 BPM_EngineEventConfig 中
     * 筛选条件：流程id AND 节点id
     * 处理逻辑：保存时，只处理表中符合筛选条件的所有行内容，以及前端传入的eventRows行内容。
     * 对于已存在的行（WF_OrUnid相同），做update，不存在的行，做insert，对比在表中，又不在eventRows中的，做delete处理。
     * 即每次保存，都以前端传入的eventRows做为保存的准确内容。
     *
     * @param processid 流程id，必须
     * @param nodeid    节点id，必须（节点id，为前端流程图设计器生成）
     * @param eventRows 需要保存的事件明细列表。每行数据包含有 WF_OrUnid、eventid、rulenum、params、sortnum 等字段
     * @return {"status","0/1","msg":"提示信息"}
     */
    @Override
    public JSONObject saveFlowChartEventByNodeType(String processid, String nodeid, JSONArray eventRows) {
        Rdb rdb = BeanCtx.getRdb();
        JSONObject json = JSONObject.parseObject(eventRows.get(0).toString());
        int index = 0;
        if (eventRows.size() > 0) {
            for (int i = 0; i < eventRows.size(); i++) {
                JSONObject job = eventRows.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
                String sql = "select * from BPM_EngineEventConfig where WF_OrUnid = '" + job.get("WF_OrUnid") + "'";
                Document doc = rdb.getDocumentBySql(sql);
                if (Tools.isBlank(doc.g("WF_OrUnid"))) {
                    doc.s("WF_OrUnid", rdb.getNewUnid());
                }
                doc.s("Processid", processid);
                doc.s("Nodeid", nodeid);
                doc.s("Eventid", job.get("Eventid"));
                doc.s("RuleNum", job.get("RuleNum"));
                doc.s("Params", job.get("Params"));
                doc.s("SortNum", job.get("SortNum"));
                doc.s("WF_LastModified", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                doc.save();
                index++;
            }
        }
        JSONObject result = new JSONObject();
        if (index == eventRows.size()) {
            result.put("1", "事件保存成功！");
        } else {
            result.put("msg", "事件保存失败！");
        }
        return result;
    }


    /**
     * 获取各节点类型的事件统一请求接口，事件统一到表 BPM_EngineEventConfig 中依据筛选条件进行获取
     * 筛选条件：流程id AND 节点id
     * 注意：这里不考虑分页的情况，即一次获取全部数据。一般情况下，流程事件不宜增加过多，1-2条为宜，最多不能超过10条。
     * 前端获得数据后，依据eventRows的内容进行展示显示历史保存的事件。
     *
     * @param processid 流程id，必须
     * @param nodeid    节点id，必须（节点id，为前端流程图设计器生成）
     * @return {"status","0/1","msg":"提示信息","eventRows":[{row1},{row2}]}
     * eventRows 事件明细行（可能多行），依据sortnum和wf_lastmodified进行排序
     * row1 事件单行明细内容，字段包含有 WF_OrUnid、eventid、rulenum、params、sortnum、wf_lastmodified、wf_addname。
     */
    @Override
    public JSONObject getFlowChartEventByNodeType(String processid, String nodeid) {
        Rdb rdb = BeanCtx.getRdb();
        String sql = "select * from BPM_EngineEventConfig where Processid = '" + processid + "' and Nodeid = '" + nodeid + "'";
        Document[] docs = rdb.getAllDocumentsBySql(sql);
        JSONArray jsonarr = new JSONArray();
        for (Document doc : docs) {
            jsonarr.add(JSONObject.parse(doc.toJson()));
        }
        JSONObject json = new JSONObject();
        json.put("rows", jsonarr);
        return json;
    }

    /**
     * 保存或更新人工活动节点配置的发送邮件信息到数据库表 BPM_MailConfig
     * 筛选条件：流程id AND 节点id
     * 注意：这里，如果是新建的数据，则insert一条，如果数据已经存在（WF_OrUnid判断）则进行update
     *
     * @param processid  流程id，必须
     * @param nodeid     节点id，必须（节点id，为前端流程图设计器生成）
     * @param formParmes 节点邮件配置表单F_S002_A025，中的所有字段信息组成JSON（包含 WF_OrUnid，新建时为空）
     * @return {"status","0/1","msg":"提示信息"}
     */
    @Override
    public JSONObject saveFlowChartMailConfigByNodeType(String processid, String nodeid, JSONObject formParmes) {
        Rdb rdb = BeanCtx.getRdb();
        String sql = "select * from BPM_MailConfig where WF_OrUnid = '" + formParmes.get("WF_OrUnid") + "'";
        Document doc = rdb.getDocumentBySql(sql);
        if (Tools.isBlank(doc.g("WF_OrUnid"))) {
            doc.s("WF_OrUnid", rdb.getNewUnid());
            doc.s("Processid", processid);
            doc.s("Nodeid", nodeid);
        }
        doc.s("SendTo", formParmes.get("SendTo"));
        doc.s("CopyTo", formParmes.get("CopyTo"));
        doc.s("MailTitle", formParmes.get("MailTitle"));
        doc.s("MailBody", formParmes.get("MailBody"));
        doc.s("Actionid", formParmes.get("Actionid"));
        doc.s("WF_LastModified", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        int i = doc.save();
        JSONObject json = new JSONObject();
        if (i > 0) {
            json.put("ok", "保存成功");
        } else {
            json.put("error", "保存失败");
        }
        return json;
    }


    /**
     * 删除人工活动节点配置的发送邮件信息 表BPM_MailConfig
     * 筛选条件：流程id AND 节点id
     *
     * @param docUnidList 需要删除行的 WF_OrUnid 键的值，多条记录使用逗号分隔
     * @return {"status","0/1","msg":"提示信息"}
     */
    @Override
    public JSONObject deleteFlowChartMailConfigByNodeType(String docUnidList) {
        String[] unids = docUnidList.split(",");
        Rdb rdb = BeanCtx.getRdb();
        int i = 0;
        for (String unid : unids) {
            String sql = "delete from BPM_MailConfig where WF_OrUnid = '" + unid + "'";
            if (rdb.execSql(sql) > 0) {
                i++;
            }
        }
        JSONObject json = new JSONObject();
        json.put("ok", "成功删除" + i + "条记录！");
        return json;
    }

    /**
     * 获取人工活动节点配置的发送邮件信息 表BPM_MailConfig
     * 筛选条件：流程id AND 节点id
     *
     * @param processid 流程id，必须
     * @param nodeid    节点id，必须（节点id，为前端流程图设计器生成）
     * @return {"status","0/1","msg":"提示信息","mailConfigRows":[{row1},{row2}]}
     * mailConfigRows 送邮件信息明细行（可能多行），依据wf_lastmodified进行升序排序，
     * row1 邮件信息单行明细内容，字段包含 表BPM_MailConfig 中的所有字段（包含WF_OrUnid）
     */
    @Override
    public JSONObject getFlowChartMailConfigByNodeType(String processid, String nodeid) {
        Rdb rdb = BeanCtx.getRdb();
        String sql = "select * from BPM_MailConfig where Processid = '" + processid + "' and Nodeid = '" + nodeid + "'";
        Document[] docs = rdb.getAllDocumentsBySql(sql);
        JSONArray jsonarr = new JSONArray();
        for (Document doc : docs) {
            jsonarr.add(JSONObject.parse(doc.toJson()));
        }
        JSONObject json = new JSONObject();
        json.put("rows", jsonarr);
        return json;
    }

    /**
     * 获取人工活动节点配置的发送邮件信息 表BPM_MailConfig
     * 筛选条件：流程id AND 节点id
     *
     * @param unid 数据id，必须
     * @return {"status","0/1","msg":"提示信息","mailConfig":{row1}}
     * mailConfigRows 送邮件信息明细行，依据wf_lastmodified进行升序排序，
     * row1 邮件信息单行明细内容，字段包含 表BPM_MailConfig 中的所有字段（包含WF_OrUnid）
     */
    @Override
    public JSONObject getFlowChartMailConfigByUnid(String unid) {
        Rdb rdb = BeanCtx.getRdb();
        String sql = "select * from BPM_MailConfig where WF_OrUnid = '" + unid + "'";
        Document doc = rdb.getDocumentBySql(sql);
        JSONObject json = new JSONObject();
        json = (JSONObject) JSONObject.parse(doc.toJson());
        return json;
    }

    /**
     * 存盘流程过程属性
     *
     * @param processid   流程ID
     * @param nodeid      节点ID
     * @param extNodeType 扩展节点类型
     * @param formParmes  流程属性
     * @return "ok", "保存成功"  || "error", "保存失败"
     */
    public JSONObject saveProcess(String processid, String nodeid, String extNodeType, JSONObject formParmes) {
        String nodeTableName = Tools.getNodeTableName(processid, nodeid); //节点所在数据库表
        String sql = "select * from " + nodeTableName + " where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
        Rdb rdb = BeanCtx.getRdb();
        Document nodeDoc = rdb.getDocumentBySql(sql);

//        nodeDoc.appendFromRequest(BeanCtx.getRequest());
        //录入数据
        for (String key : formParmes.keySet()) {
            nodeDoc.s(key, formParmes.get(key));
        }
        nodeDoc.s("Processid", processid);
        nodeDoc.s("Nodeid", nodeid);
        nodeDoc.s("ExtNodeType", extNodeType);
        nodeDoc.s("WF_Appid", "S029");
        int i = nodeDoc.save();
        JSONObject json = new JSONObject();
        if (i > 0) {
            json.put("ok", "保存成功");
        } else {
            json.put("error", "保存失败");
        }
        return json;
    }

    /**
     * 流程保存前操作，如缺省保存，删除节点，校验
     *
     * @param processid   流程ID
     * @param nodeid      节点ID
     * @param nodeList    节点ID的字符串，逗号隔开
     * @param action      CheckNodeAttr、SaveAllDefaultNode、DeleteNode
     * @param nodeType    节点类型
     * @param startNodeid 开始节点ID
     * @param endNodeid   结束节点ID
     * @return 操作结果
     */
    @Override
    public JSONObject actionFlowChartGraphic(String processid, String nodeid, String nodeList, String action, String nodeType, String startNodeid, String endNodeid) {
        if (action.equals("CheckNodeAttr")) {
            return checkAllNodeAttr(processid, nodeList);
        } else if (action.equals("SaveAllDefaultNode")) {
            return saveAllDefaultNodeAttr(processid, nodeid, nodeType, startNodeid, endNodeid);
        } else if (action.equals("DeleteNode")) {
            return deleteNode(processid, nodeid);
        }
        return null;
    }

    /**
     * 存盘通用节点
     *
     * @param processid   流程ID
     * @param nodeid      节点ID
     * @param extNodeType 扩展节点类型
     * @param formParmes  节点属性
     * @return "ok", "保存成功" || "error", "保存失败"
     */
    public JSONObject saveNode(String processid, String nodeid, String extNodeType, JSONObject formParmes) {
        String nodeTableName = Tools.getNodeTableName(processid, nodeid); //节点所在数据库表
        String sql = "select * from " + nodeTableName + " where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
        Rdb rdb = BeanCtx.getRdb();
        Document nodeDoc = rdb.getDocumentBySql(sql);
        if (Tools.isBlank(nodeDoc.g("WF_OrUnid"))) {
            nodeDoc.s("WF_OrUnid", rdb.getNewUnid());
        }
        nodeDoc.s("ExtNodeType", extNodeType);
        nodeDoc.s("Processid", processid);
        nodeDoc.s("Nodeid", nodeid);
        for (String key : formParmes.keySet()) {
            nodeDoc.s(key, formParmes.get(key));
        }
        nodeDoc.removeItem("QryNodeType");
        int i = nodeDoc.save();
        JSONObject json = new JSONObject();
        if (i > 0) {
            json.put("ok", "保存成功");
        } else {
            json.put("error", "保存失败");
        }
//         BeanCtx.userlog(processid, "修改流程节点", "修改流程节点(" + nodeDoc.g("NodeName") + ")");
        return json;
    }

    /**
     * 删除流程事件
     *
     * @param processid   流程ID
     * @param nodeid      节点类型
     * @param docUnidList 删除的记录id，多个以逗号隔开
     * @return 处理结果 "ok", "成功删除"+i+"条记录！"
     */
    @Override
    public JSONObject deleteFlowChartEventByNodeType(String processid, String nodeid, String docUnidList) {
        String[] unids = docUnidList.split(",");
        Rdb rdb = BeanCtx.getRdb();
        int i = 0;
        for (String unid : unids) {
            String sql = "delete from BPM_EngineEventConfig where WF_OrUnid = '" + unid + "'";
            if (rdb.execSql(sql) > 0) {
                i++;
            }
        }
        JSONObject json = new JSONObject();
        json.put("ok", "成功删除" + i + "条记录！");
        return json;
    }

    /**
     * 搜索流程
     *
     * @param searchStr 搜索字段，为空时返回所有流程
     * @return 返回搜索或所有流程信息
     */
    public JSONObject getProcessList(String searchStr) {
        String sql = "select * from bpm_modprocesslist";
        if (Tools.isNotBlank(searchStr)) {
            sql += " where NodeName like '%" + searchStr + "%'";
        }

        sql += " Order by WF_DocCreated DESC";

        Rdb rdb = BeanCtx.getRdb();
        Document[] docs = rdb.getAllDocumentsBySql(sql);
        JSONArray jsonarr = new JSONArray();
        for (Document doc : docs) {
            jsonarr.add(JSONObject.parse(doc.toJson()));
        }
        JSONObject json = new JSONObject();
        json.put("rows", jsonarr);
        return json;

    }


    /**
     * 删除流程设计
     *
     * @param processids 流程ID(用逗号","隔开)
     * @return 返回删除结果 "ok", "成功删除"+i+"条记录！"
     */
    @Override
    public JSONObject deleteProcessList(String processids) {
        String[] processidArr = processids.split(",");
        Rdb rdb = BeanCtx.getRdb();
        int i = 0;
        for (String processid : processidArr) {
            String sql = "delete from bpm_modprocesslist where WF_OrUnid = '" + processid + "'";
            if (rdb.execSql(sql) > 0) {
                i++;
            }
        }
        JSONObject json = new JSONObject();
        json.put("ok", "成功删除" + i + "条记录！");
        return json;
    }

    /**
     * 表单字段配置
     *
     * @param processid 流程id，必须
     * @return 表单字段配置JSON
     */
    @Override
    public JSONObject getFormConfig(String processid) {

        String sql = "select formConfig from bpm_modprocesslist where Processid = '" + processid + "'";
        Rdb rdb = BeanCtx.getRdb();
        String formConfig = rdb.getValueBySql(sql);

        formConfig = Tools.isNotBlank(formConfig) ? formConfig : "{total:0,rows:[]}";

        return JSON.parseObject(formConfig);
    }

    /**
     * 设置表单字段配置
     * add by alibao 202010
     *
     * @param formConfig 表单字段配置JSON字符串
     * @param processid  对应流程ID
     * @return {"status","0/1","msg":"提示信息"}
     */
    @Override
    public JSONObject setFormConfig(String formConfig, String processid) {

        String sql = "select * from bpm_modprocesslist where Processid = '" + processid + "'";

        Rdb rdb = BeanCtx.getRdb();
        Document doc = rdb.getDocumentBySql(sql);

        if (Tools.isBlank(doc.g("WF_OrUnid"))) {
            doc.s("WF_OrUnid", rdb.getNewUnid());
        }

        doc.s("formConfig", formConfig);

        doc.save();

        JSONObject returnJSON = new JSONObject();
        returnJSON.put("status", "1");
        returnJSON.put("msg", "操作成功~");

        return returnJSON;
    }

    /**
     * 通用删除表单记录方法
     * add by alibao 202010
     *
     * @param tableName 需要删除的表名称
     * @param wforunid  需要删除的唯一字段WF_Orunid
     * @return {"status","0/1","msg":"提示信息"}
     */
    @Override
    public JSONObject delCommonTableRows(String tableName, String wforunid) {

        String sql = "delete from " + tableName + " where WF_OrUnid='" + wforunid + "'";

        Rdb rdb = BeanCtx.getRdb();
        int i = rdb.execSql(sql);

        JSONObject returnJSON = new JSONObject();
        if (i > 0) {
            returnJSON.put("status", "1");
            returnJSON.put("msg", "删除成功~");
        } else {
            returnJSON.put("status", "0");
            returnJSON.put("msg", "删除出错！");
        }
        return returnJSON;
    }

    /**
     * 获取节点操作按钮配置
     * add by alibao 202010
     *
     * @param processid 流程ID
     * @param nodeid    节点ID
     * @return 操作按钮配置JSON
     */
    @Override
    public JSONObject getButtonConfig(String processid, String nodeid) {

        String sql = "select nodeButtonConfig from bpm_modtasklist where Processid = '" + processid + "' and Nodeid='" + nodeid + "'";
        Rdb rdb = BeanCtx.getRdb();
        String nodeButtonConfig = rdb.getValueBySql(sql);

        if (Tools.isBlank(nodeButtonConfig)) {
            nodeButtonConfig = "{total:0,rows:[]}";
        }

        return JSONObject.parseObject(nodeButtonConfig);

    }

    /**
     * 配置节点操作按钮
     * add by alibao 202010
     *
     * @param buttonConfig 节点操作按钮配置
     * @param processid    对应流程ID
     * @param nodeid       对应用节点id
     * @return {"status","0/1","msg":"提示信息"}
     */
    @Override
    public JSONObject setButtonConfig(String buttonConfig, String processid, String nodeid) {

        String sql = "select * from bpm_modtasklist where Processid='" + processid + "' and Nodeid='" + nodeid + "'";

        Rdb rdb = BeanCtx.getRdb();
        Document doc = rdb.getDocumentBySql(sql);

        if (Tools.isBlank(doc.g("WF_OrUnid"))) {
            doc.s("WF_OrUnid", rdb.getNewUnid());
        }

        doc.s("nodeButtonConfig", buttonConfig);

        doc.save();

        JSONObject returnJSON = new JSONObject();
        returnJSON.put("status", "1");
        returnJSON.put("msg", "操作成功~");

        return returnJSON;

    }


    /**
     * 获得所有节点按钮的动作信息
     * add by alibao 202010
     * [{"ActionName":"提交下一会签用户","Actionid":"GoToNextParallelUser"},{"ActionName":"收回文档","Actionid":"Undo"},{"ActionName":"结束当前环节并推进到下一环节","Actionid":"GoToNextNode"},{"ActionName":"回退首环节","Actionid":"GoToFirstNode"},{"ActionName":"尝试结束子流程节点","Actionid":"EndSubProcessNode"},{"ActionName":"回退上一环节","Actionid":"GoToPrevNode"},{"ActionName":"暂停","Actionid":"Pause"},{"ActionName":"标记为阅","Actionid":"EndCopyTo"},{"ActionName":"后台启动用户任务","Actionid":"StartUser"},{"ActionName":"办理完成","Actionid":"EndUserTask"},{"ActionName":"转他人处理","Actionid":"GoToOthers"},{"ActionName":"归档","Actionid":"GoToArchived"},{"ActionName":"后台结束用户任务","Actionid":"EndUser"},{"ActionName":"提交任意环节","Actionid":"GoToAnyNode"},{"ActionName":"回退上一用户","Actionid":"GoToPrevUser"},{"ActionName":"后台结束节点","Actionid":"EndNode"},{"ActionName":"回退任意环节","Actionid":"ReturnToAnyNode"},{"ActionName":"返回给转交者","Actionid":"BackToDeliver"},{"ActionName":"恢复","Actionid":"UnPause"},{"ActionName":"提交下一串行用户","Actionid":"GoToNextSerialUser"},{"ActionName":"自动运行","Actionid":"AutoRun"},{"ActionName":"返回给回退者","Actionid":"BackToReturnUser"},{"ActionName":"传阅用户","Actionid":"CopyTo"},{"ActionName":"同步任务","Actionid":"SyncUserTask"}]
     *
     * @return 返回所有按钮动作 JSON信息
     */
    @Override
    public JSONArray getALLActionConfig() {

        String sql = "select ActionName,Actionid from bpm_engineactionconfig";

        Rdb rdb = BeanCtx.getRdb();
        Document[] docs = rdb.getAllDocumentsBySql(sql);
        String atcionJsonStr = Tools.dc2json(docs, "", false);

        return JSONArray.parseArray(atcionJsonStr);
    }


    /**
     * 新增流程id
     *
     * @return 返回新创建的流程ID
     */
    @Override
    public JSONObject getUnid() {
        Rdb rdb = BeanCtx.getRdb();
        JSONObject json = new JSONObject();
        json.put("Processid", rdb.getNewUnid());
        return json;
    }

    /**
     * 删除指定节点
     *
     * @param processid 流程ID
     * @param nodeid    节点ID
     * @return "ok", "节点成功删除!"
     */
    public JSONObject deleteNode(String processid, String nodeid) {
        JSONObject result = new JSONObject();
//        String nodeid = BeanCtx.g("Nodeid", true);
        Rdb rdb = BeanCtx.getRdb();
        String tableName = Tools.getNodeTableName(processid, nodeid);
        if (Tools.isNotBlank(tableName)) {
            String sql = "delete from " + tableName + " where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
            rdb.execSql(sql);
        }
        result.put("ok", "节点成功删除!");
        return result;
    }

    /**
     * 检测所有节点是否有保存属性
     *
     * @param processid 流程ID
     * @param nodeList  节点ID，多个以逗号隔开
     * @return 返回保存接口，json
     */
    public JSONObject checkAllNodeAttr(String processid, String nodeList) {
        JSONObject result = new JSONObject();
        Rdb rdb = BeanCtx.getRdb();
        HashSet<String> noAttrNode = new HashSet<String>();
//        String nodeList = BeanCtx.g("NodeList", true);
        String[] nodeArray = cn.linkey.orm.util.Tools.split(nodeList, ",");
        for (String itemid : nodeArray) {
            int spos = itemid.indexOf("#");
            String nodeid = itemid.substring(0, spos);
            String objid = itemid.substring(spos + 1);
            String sql = "select * from BPM_AllModNodeList where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
            if (!rdb.hasRecord(sql)) {
                noAttrNode.add(objid);
            }
        }
        result.put("ok", cn.linkey.orm.util.Tools.join(noAttrNode, ","));
        return result;
    }

    /**
     * 保存节点的所有缺省属性
     *
     * @param processid   流程ID
     * @param nodeid      节点ID
     * @param nodeType    节点类型
     * @param startNodeid 开始节点ID
     * @param endNodeid   结束节点ID
     * @return {"ok", "ok"}
     */
    public JSONObject saveAllDefaultNodeAttr(String processid, String nodeid, String nodeType, String startNodeid, String endNodeid) {
        JSONObject result = new JSONObject();
        Rdb rdb = BeanCtx.getRdb();
        String sql = "select * from BPM_AllModNodeList where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
        Document doc = rdb.getDocumentBySql(sql);
        if (doc.isNull()) {
            //说明节点没有保存过，进行保存
            doc.s("Processid", processid);
            doc.s("Nodeid", nodeid);
            doc.s("NodeType", Tools.getNodeType(processid, nodeid));
            doc.setTableName(Tools.getNodeTableName(processid, nodeid));
            if (nodeType.equals("Router")) {
                //保存路由线
                // BeanCtx.out("startnodeid="+BeanCtx.g("StartNodeid"));
                doc.s("ExtNodeType", "sequenceFlow");
                doc.s("SourceNode", startNodeid);
                doc.s("TargetNode", endNodeid);
                doc.s("NodeName", "");
                doc.setTableName("BPM_ModSequenceFlowList");
            } else if (nodeType.equals("EndNode")) {
                //结束节点
                doc.s("NodeName", "结束");
                doc.s("ExtNodeType", "endEvent");
                doc.s("Terminate", "1");
                doc.s("EndBusinessName", "已结束");
                doc.s("EndBusinessid", "1");
                doc.setTableName("BPM_ModEventList");
            } else if (nodeType.equals("StartNode")) {
                //开始节点
                doc.s("NodeName", "开始");
                doc.s("ExtNodeType", "startEvent");
                doc.setTableName("BPM_ModEventList");
            } else if (nodeType.equals("Event")) {
                //事件节点
                doc.s("NodeName", "");
                doc.setTableName("BPM_ModEventList");
            }
            //BeanCtx.setDebug();
            int i = doc.save();
        }
        result.put("ok", "ok");
        return result;
    }


    /**
     * 更新事件规则配置到数据库中
     *
     * @return {"status","0/1","msg":"提示信息"}
     */
    @Override
    public JSONObject updateEventRuleConfig() {

        // 流程引擎 即osflow-engine.jar 内部类更新
        // 包名  事件地址：cn.linkey.rulelib.Event;
        String packageName = "cn.linkey.rulelib.Event";
        updateEventConfg(packageName);

        // 外部类更新
        String extpackageName = "cn.linkey.rulelib.extevent";
        updateEventConfg(extpackageName);


        JSONObject jsonObj = new JSONObject();
        jsonObj.put("status", "1");
        jsonObj.put("msg", "更新成功！");

        return jsonObj;
    }


    /**
     * 更新事件规则配置到数据库中
     *
     * @return {"status","0/1","msg":"提示信息"}
     */
    public Boolean updateEventConfg(String packageName){

        Rdb rdb = BeanCtx.getRdb();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        List<String> classNames = null;
        try {
            classNames = getClasspath(packageName);
            if (classNames != null) {
                for (String className : classNames) {

                    String sql = "select * from bpm_rulelist where ClassPath='" + className + "'";
                    Document doc = rdb.getDocumentBySql(sql);

                    if (doc.isNewDoc()) {
                        doc.s("WF_OrUnid", rdb.getNewUnid());
                        doc.s("WF_DocCreated", ft.format(new Date()));
                        doc.s("RuleName", className.substring(className.lastIndexOf(".") + 1));
                    }

                    doc.s("RuleNum", className.substring(className.lastIndexOf(".") + 1));
                    doc.s("RuleType", "8");
                    doc.s("ClassPath", className);
                    doc.s("Singleton", "1");
                    doc.s("WF_CacheFlag", "0");
                    doc.s("WF_AddName", BeanCtx.getUserid());
                    doc.s("WF_AddName_CN", BeanCtx.getUserid());
                    doc.save();

                    System.out.println("className: " + className);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * 遍历某个包下面的类路径
     * @param packagePath 包路径
     * @return 包下面所有类名
     * @throws Exception 获取类名出错则抛出异常
     */
    public List<String> getClasspath(String packagePath) throws Exception {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        // 加载系统所有类资源
        Resource[] resources = resourcePatternResolver.getResources("classpath*:" + packagePath.replaceAll("[.]", "/") + "/**/*.class");
        List<String> list = new ArrayList<>();
        // 把每一个class文件找出来
        for (Resource r : resources) {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
            Class<?> clazz = ClassUtils.forName(metadataReader.getClassMetadata().getClassName(), null);
            list.add(clazz.getName());

        }
        return list;
    }


    /**
     * 通用更新表格数据
     *
     * @param tableName 表名
     * @return {"status","0/1","msg":"提示信息"}
     */
    public JSONObject saveEventRuleConfig(JSONArray eventRows, String tableName) {

        LinkedHashSet<Document> docs = DocumentsUtil.jsonStr2dc(eventRows.toJSONString(), tableName);
        DocumentsUtil.saveAll(docs);

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("status","1");
        jsonObj.put("msg","更新成功！");

        return jsonObj;
    }


    /**
     * 通用删除表格数据
     *
     * @param docUnidList 删除的记录id，多个以逗号隔开
     * @param tableName   表名
     * @return "ok", "成功删除"+i+"条记录！"
     */
    public JSONObject deleteEventRuleConfig(String docUnidList, String tableName) {

        String[] unids = docUnidList.split(",");
        Rdb rdb = BeanCtx.getRdb();
        int i = 0;
        for (String unid : unids) {
            String sql = "delete from " + tableName + " where WF_OrUnid = '" + unid + "'";
            if (rdb.execSql(sql) > 0) {
                i++;
            }
        }
        JSONObject json = new JSONObject();
        json.put("ok", "成功删除" + i + "条记录！");

        return json;
    }


    /**
     * 通用获取表格数据
     *
     * @param page               分页
     * @param rows               记录数
     * @param tableName          表名
     * @param searchStr          搜索字段
     * @param DefaultSearchField 查询字段名称，如：ruleName,classpath
     * @return {"status","0/1","msg":"提示信息"}
     */
    public JSONObject getCommonJson(int page, int rows, String tableName, String searchStr, String DefaultSearchField) {

        Rdb rdb = BeanCtx.getRdb();
        Connection conn = null;
        Document[] docs = null;

        try {
            conn = rdb.getConnection();

            String sqlWhere = "select * from " + tableName;

            // 组合搜索字符串
            if (Tools.isNotBlank(searchStr) && Tools.isNotBlank(DefaultSearchField)) {
                searchStr = "%" + searchStr + "%";
                String defaultSearchField = "";
                if (rdb.getDbType(conn).equals("MSSQL")) {
                    defaultSearchField = DefaultSearchField.replace(",", "+"); // sql server把,号换成+号
                } else if (rdb.getDbType().equals("MYSQL")) {
                    defaultSearchField = "concat(" + DefaultSearchField + ")"; // mysql需要使用concat方法
                } else {
                    defaultSearchField = DefaultSearchField.replace(",", "||"); // oracle把,号换成||号
                }
                searchStr = " where " + defaultSearchField + " like '" + searchStr + "'";
                sqlWhere = sqlWhere + searchStr;
            }

            sqlWhere = sqlWhere + " order by WF_DocCreated DESC ";

            docs = rdb.getAllDocumentsBySql(tableName, sqlWhere, page, rows);

        } catch (Exception e) {
            e.printStackTrace();
        }

        String jsonStr = DocumentsUtil.dc2json(docs, "");

        JSONObject json = new JSONObject();
        json.put("total", docs.length);
        json.put("rows", JSONArray.parseArray(jsonStr));

        return json;
    }


    /**
     * 返回规则分类，带有具体规则
     *
     * @return 返回规则 json tree
     */
    @Override
    public JSONArray getRuleTree() {

        // 这里规则分类树与 /design/linkey/bpm/newFlow/json/RuleSort.json 对应
        String treeJsonStr = "[{\"id\":\"001\",\"text\":\"规则分类树\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"view?wf_num=V_S010_G001\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"4316D1E504ECC04E8F087B5084D1A56C8F8E\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001007\",\"text\":\"流程规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"78c98edb06873045700af5d00df8e46a76a6\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001007002\",\"text\":\"超时规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"2D56E8310AEB904D0B08EB804D9E197DC5B7\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001007004\",\"text\":\"参与者规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"95be8a390224504d0b0b7c30d80c1b6088b4\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001007004001\",\"text\":\"参与者规则配置\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"editorgrid?wf_num=V_S001_E012\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"9a8e3d370be2c0435d09e6d024dbf07df9e5\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001007004002\",\"text\":\"参与者规则管理\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"ee592ee800e0904c280ae180f2651edda691\",\"spread\":\"\",\"state\":\"open\"}]},{\"id\":\"001007001\",\"text\":\"路由规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"b1f8d9c00e14b048a809dd502aca332ce404\",\"spread\":\"1\",\"state\":\"open\"},{\"id\":\"001007006\",\"text\":\"节点规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"954e86fd0173904e65093d00a07dad7c8cbe\",\"spread\":\"1\",\"state\":\"open\",\"children\":[{\"id\":\"001007006001\",\"text\":\"开始节点\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"445c3df80147804590098c805ec8124204ef\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001007006002\",\"text\":\"结束节点\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"4f7dc1cf0cf1f04b170ade105f64528ec8d5\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001007006003\",\"text\":\"人工节点\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"7410d2db0ef9204b290a74700e9c028615fd\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001007006004\",\"text\":\"自动节点\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"49030ed409229041dc0b8e90335167d78a3a\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001007006005\",\"text\":\"前置事件节点\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"eff773e8072360432808193071f39fbd43f1\",\"spread\":\"1\",\"state\":\"open\"},{\"id\":\"001007006006\",\"text\":\"网关节点\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"3bab86f806e3504f910b6c70e54b74766d7e\",\"spread\":\"1\",\"state\":\"open\"},{\"id\":\"001007006007\",\"text\":\"子流程节点\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"7ebfbc550d66f04a490981108061e3520003\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001007006008\",\"text\":\"后置事件节点\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"1df23e740003e041230815f05aee330d1ab0\",\"spread\":\"\",\"state\":\"open\"}]},{\"id\":\"001007005\",\"text\":\"补偿规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"bae3c11d09b1904c8e09a250075c6a65bd5b\",\"spread\":\"1\",\"state\":\"open\"},{\"id\":\"001007003\",\"text\":\"过程规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"123e914d0109f041330b7f80ec961cc1a5cf\",\"spread\":\"\",\"state\":\"open\"}]},{\"id\":\"001002\",\"text\":\"管控规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"1b94b9640bd060432b095290a563505593c4\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001002004\",\"text\":\"质量管控\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"faaae7c901cad046730bd190500833d1fb44\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001002004001\",\"text\":\"质量计算规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"be234afe0aec004449097c40b2c8d6f9726c\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001002004002\",\"text\":\"质量应对规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"854cb4110778904adb0bef60a1162c1e7cb7\",\"spread\":\"\",\"state\":\"open\"}]},{\"id\":\"001002002\",\"text\":\"成本管控\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"e9a78353060ec046c80b82f0f36aa9f47af9\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001002002001\",\"text\":\"成本计算规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"bf7ed7010ef01045bd0bdd70db5fd78be82e\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001002002002\",\"text\":\"成本应对规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"a61190a20bacf04e140a2040db05613521d0\",\"spread\":\"\",\"state\":\"open\"}]},{\"id\":\"001002001\",\"text\":\"风险管控\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"a07e6dcc023ae043920a0890d27c2d0b115f\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001002001001\",\"text\":\"风险等级规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"ccc59678093d504e090a33a02a6163ce2611\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001002001002\",\"text\":\"风险应对规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"b146eff802c100448008c660b18421a3cbbe\",\"spread\":\"\",\"state\":\"open\"}]},{\"id\":\"001002003\",\"text\":\"绩效管控\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"dafddc8c09cf604d8209c600fc8e6e9b1828\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001002003001\",\"text\":\"绩效计算规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"bd79f58e038f004a47082df025016a9ac45e\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001002003002\",\"text\":\"绩效应对规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"110a92b70fb4004cb80989008117d35d4a6f\",\"spread\":\"\",\"state\":\"open\"}]}]},{\"id\":\"001006\",\"text\":\"表单规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"cead6cdb08795047a00a2730a1f5b82486ce\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001006001\",\"text\":\"表单事件\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"view?wf_num=V_S001_G034&WF_Appid=S009&EventType=1\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"b951e0f60284404d6c0978e0326021febb3d\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001006002\",\"text\":\"后端字段规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"view?wf_num=V_S001_G033\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"eebfc75209ac70425909170007a0e04b1958\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001006003\",\"text\":\"前端验证规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"editorgrid?wf_num=V_S001_E009\",\"Itemid\":\"\",\"OpenType\":\"center\",\"WF_OrUnid\":\"af96118e0ef000494a0b88e0d9e17bd8d80c\",\"spread\":\"1\",\"state\":\"open\"}]},{\"id\":\"001001\",\"text\":\"业务规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"1545bef7064160428309bf90e4e4294f45c5\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001001001\",\"text\":\"集成规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"D094FD090FEC104EA80823E0709A70A132C0\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001001001001\",\"text\":\"SAP\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"f333316d01b44046be0a04706d94fe9bc1f1\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001001001002\",\"text\":\"HR\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"5c0e817f05b6a04bcb0a0ea0fc036aa2d484\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001001001003\",\"text\":\"数据库\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"b39ae46d0a09804dfd0ae910d14be1348190\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001001001004\",\"text\":\"CRM\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"e7e71e6501afd045d409c550fa8e8e92705c\",\"spread\":\"\",\"state\":\"open\"}]},{\"id\":\"001001002\",\"text\":\"公共规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"51583e7e052a6043060b7c40568ce1de160a\",\"spread\":\"1\",\"state\":\"open\"}]},{\"id\":\"001003\",\"text\":\"示例规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"d9fee5b40410b04c81086f10db28b241aec0\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001004\",\"text\":\"项目规则\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"a025354c072f60495e0bc010e6a5694d4799\",\"spread\":\"\",\"state\":\"closed\",\"children\":[{\"id\":\"001004001\",\"text\":\"转正申请\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"7dd8a23e05b74046080a2a80bd3ee16d4cfe\",\"spread\":\"\",\"state\":\"open\"},{\"id\":\"001004002\",\"text\":\"请假申请\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"b6ff200d0fc3004d8a091ff0ef6ceb2398df\",\"spread\":\"\",\"state\":\"open\"}]},{\"id\":\"001005\",\"text\":\"Alibao测试\",\"iconCls\":\"\",\"Treeid\":\"T_S010_001\",\"ItemUrl\":\"\",\"Itemid\":\"\",\"OpenType\":\"\",\"WF_OrUnid\":\"7cafda460c0990458809521085a6cb0a5659\",\"spread\":\"\",\"state\":\"open\"}]}]\n";

        JSONArray jsonArray = JSONArray.parseArray(treeJsonStr);

        getRuleTreeChildren(jsonArray);


        return jsonArray;
    }

    /**
     * 对规则树循环遍历，添加规则
     *
     * @param jsonArray 规则分类树
     * @return 含有分类的规则分类树
     */
    public JSONArray getRuleTreeChildren(JSONArray jsonArray) {

        for (int i = 0; i < jsonArray.size(); i++) {

            JSONObject chaildJSON = jsonArray.getJSONObject(i);
            String folderName = chaildJSON.getString("text");
            String folderid = chaildJSON.getString("id");
            chaildJSON.put("text", folderName += "(" + getRuleNum(folderid) + ")");

            JSONArray childrenArr = (JSONArray) chaildJSON.get("children");

            if (childrenArr == null) {
                chaildJSON.put("children", getAllDocList(folderid));
            } else {
                getRuleTreeChildren(childrenArr);
            }
        }

        return jsonArray;
    }


    /**
     * 获得分类下的所有流程规则
     *
     * @param folderid 角色编号
     * @return
     */
    public JSONArray getAllDocList(String folderid) {

        Rdb rdb = BeanCtx.getRdb();

        StringBuilder jsonStr = new StringBuilder();
        String sql = "select * from BPM_RuleList where Folderid='" + folderid + "' and RuleType='8'";
        Document[] dc = rdb.getAllDocumentsBySql(sql);

        JSONArray ruleArr = new JSONArray();

        int i = 0;
        for (Document doc : dc) {

            JSONObject ruleConfig = new JSONObject();
            ruleConfig.put("text", doc.g("RuleName") + "(" + doc.g("RuleNum") + ")");
            ruleConfig.put("id",doc.g("RuleNum"));
            ruleConfig.put("iconCls","icon-method");
            ruleConfig.put("state","open");

            ruleArr.add(ruleConfig);

        }
        return ruleArr;
    }


    /**
     * 获得分类下有几个流程数
     *
     * @param folderid 规则分类id
     * @return 规则具体分类下的规则个数
     */
    public String getRuleNum(String folderid) {
        Rdb rdb = BeanCtx.getRdb();
        String sql = "select Count(*) as TotalNum from BPM_RuleList where Folderid like '" + folderid + "%'";
        return rdb.getValueBySql(sql);
    }


}
