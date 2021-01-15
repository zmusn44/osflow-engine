package cn.linkey.flowdesign.api;

import cn.linkey.workflow.util.Tools;
import cn.linkey.orm.dao.Rdb;
import cn.linkey.orm.doc.Document;
import cn.linkey.orm.factory.BeanCtx;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;


public class FlowchartImp implements FlowChart {


	/**
	 * 保存流程图模型数据接口
	 *  保存流程图到表：BPM_ModGraphicList中，保存前必须验证在BPM_ModProcessList中有对应processid的流程图配置信息
	 * @param processid 流程id，必须
	 * @param flowJSON 流程图模型数据，必须
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
            resultJson.put("Status","1");
        }
        else {
        	resultJson.put("Status","0");
        	resultJson.put("msg", "请在空白处点击键并在过程属性中指定流程的名称!");
        }
        return resultJson;
	}

	/**
	 * 获取流程图模型数据接口
	 * 前端获得数据后，依据Processid和flowJSON的内容，重新将保存过的流程图模型进行渲染显示
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

		resultJson = JSONObject.parseObject("{\"Status\":\"1\",\"msg\":\"" + processName + "\",\"flowJSON\": " + (Tools.isBlank(flowJSON)?"\"\"":flowJSON) + ",\"Processid\": \"" + processid + "\" }");
		return resultJson;
	}


	/**
	 * 保存各节点类型的统一请求接口，这里依据节点id进行类另和保存表的区分
	 *  节点类型包含：全局属性（Process，对应表 bpm_modprocesslist），
	 *                人工节点（userTask/T10001，T开头，对应表 Bpm_Modtasklist），
	 *                自由节点（businessRuleTask/T10003，T开头，对应表 Bpm_Modtasklist），
	 *                路由节点（sequenceFlow/R10005，R开头，对应表 Bpm_ModSequenceflowList），
	 *                网关节点（Gateway/G10007，G开头，对应表 Bpm_Modgatewaylist），
	 *                事件节点（Event/E10009，E开头，对应表 Bpm_Modeventlist），
	 *                开始节点（startEvent/E10011，E开头，对应表 Bpm_Modeventlist），
	 *                结束节点（endEvent/E10013，E开头，对应表 Bpm_Modeventlist）.
	 *   使用nodeid的开始字符，可以决定保存的表名，再使用流程id，两者做为条件，可以找到表中唯一的一条记录，
	 *      若记录不存在则insert，若记录存在则update
	 *   请注意：formParmes中有不少的字段，在对应的表中是不存在这些表字段的，
	 *      所以实现存储时是将数据存在到表中的XmlData大字段的，这里建议使用ORM存储引擎中的Document对象进行存储
	 * @param processid 流程id，必须
	 * @param nodeid 节点id，必须
	 * @param extNodeType 节点类型，非必须，一般用于后端判断某些节点某些字段必填
	 * @param formParmes 非必须，大多数据情况下是有值的，节点中表单需要保存的参数，仅对传入的参数进行保存，
	 *                   这里为了后续扩展方便，不使用单独的字段，请遍历formParmes对象进行保存，需要保存什么，交给前端进行选择和决断
	 * @return {"status","0/1","msg":"提示信息"}
	 */
	@Override
	public JSONObject saveFlowChartModByNodeType(String processid, String nodeid, String extNodeType,
                                                 JSONObject formParmes) {
		JSONObject json = new JSONObject();
		if (extNodeType.equals("Process")) {
			json = saveProcess(processid, nodeid, extNodeType, formParmes);
        }
        else {
        	json = saveNode(processid, nodeid, extNodeType,formParmes);
        }
        
		return json;
	}

	/**
	 * 获取各节点类型的统一请求接口，这里依据节点id进行类另和保存表的区分
	 *  节点类型包含：参考saveFlowChartModByNodeType中的内容
	 *  使用nodeid的开始字符，可以决定保存的表名，再使用流程id，两者做为条件，可以找到表中唯一的一条记录
	 *  请注意：获取数据时，需要将XmlData大字段的内容一并获取，并反回，这里建议使用ORM存储引擎中的Document对象进行处理
	 * 前端获得数据后，依据formdata的内容，结合相应的表单字段，设置进专表单中进行展示显示历史保存配置。
	 * @param processid 流程id，必须
	 * @param nodeid 节点id，必须（节点id，为前端流程图设计器生成）
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
		Document [] docs = rdb.getAllDocumentsBySql(sql);
		JSONArray list = new JSONArray();
		for(Document d : docs) {
			list.add(d.toJson());
		}
		resultJson.put("事件设置", list);
		
		sql = "select * from  BPM_ModProcessList where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
		doc = rdb.getDocumentBySql(sql);
		resultJson.put("归档设置", JSONObject.parse(doc.toJson()));
		
		if(nodeid.substring(0, 1).equals("T")) {
			nodeTableName = "BPM_MailConfig";
			sql = "select * from  "+ nodeTableName +" where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
			docs = rdb.getAllDocumentsBySql(sql);
			list.clear();
			for(Document d : docs) {
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
	 *   筛选条件：流程id AND 节点id
	 *   处理逻辑：保存时，只处理表中符合筛选条件的所有行内容，以及前端传入的eventRows行内容。
	 *      对于已存在的行（WF_OrUnid相同），做update，不存在的行，做insert，对比在表中，又不在eventRows中的，做delete处理。
	 *      即每次保存，都以前端传入的eventRows做为保存的准确内容。
	 * @param processid 流程id，必须
	 * @param nodeid 节点id，必须（节点id，为前端流程图设计器生成）
	 * @param eventRows 需要保存的事件明细列表。每行数据包含有 WF_OrUnid、eventid、rulenum、params、sortnum 等字段
	 * @return {"status","0/1","msg":"提示信息"}
	 */
	@Override
	public JSONObject saveFlowChartEventByNodeType(String processid, String nodeid, JSONArray eventRows) {
		Rdb rdb = BeanCtx.getRdb();
		JSONObject json = JSONObject.parseObject(eventRows.get(0).toString());
		int index = 0;
		if(eventRows.size()>0){
			for(int i=0;i<eventRows.size();i++){
				JSONObject job = eventRows.getJSONObject(i); // 遍历 jsonarray 数组，把每一个对象转成 json 对象
				String sql = "select * from BPM_EngineEventConfig where WF_OrUnid = '" + job.get("WF_OrUnid") + "'";
				Document doc = rdb.getDocumentBySql(sql);
				if(Tools.isBlank(doc.g("WF_OrUnid"))) {
					doc.s("WF_OrUnid",rdb.getNewUnid());
				}
				doc.s("Processid",processid);
				doc.s("Nodeid",nodeid);
				doc.s("Eventid",job.get("Eventid"));
				doc.s("RuleNum",job.get("RuleNum"));
				doc.s("Params",job.get("Params"));
				doc.s("SortNum",job.get("SortNum"));
				doc.s("WF_LastModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				doc.save();
				index++;
			}
		}
		JSONObject result = new JSONObject();
		if(index == eventRows.size()) {
			result.put("1", "事件保存成功！");
		}else {
			result.put("msg", "事件保存失败！");
		}
		return result;
	}


	/**
	 * 获取各节点类型的事件统一请求接口，事件统一到表 BPM_EngineEventConfig 中依据筛选条件进行获取
	 *   筛选条件：流程id AND 节点id
	 *   注意：这里不考虑分页的情况，即一次获取全部数据。一般情况下，流程事件不宜增加过多，1-2条为宜，最多不能超过10条。
	 *  前端获得数据后，依据eventRows的内容进行展示显示历史保存的事件。
	 * @param processid 流程id，必须
	 * @param nodeid 节点id，必须（节点id，为前端流程图设计器生成）
	 * @return {"status","0/1","msg":"提示信息","eventRows":[{row1},{row2}]}
	 *  eventRows 事件明细行（可能多行），依据sortnum和wf_lastmodified进行排序
	 *  row1 事件单行明细内容，字段包含有 WF_OrUnid、eventid、rulenum、params、sortnum、wf_lastmodified、wf_addname。
	 */
	@Override
	public JSONObject getFlowChartEventByNodeType(String processid, String nodeid) {
		Rdb rdb = BeanCtx.getRdb();
		String sql = "select * from BPM_EngineEventConfig where Processid = '"+processid+"' and Nodeid = '"+ nodeid +"'";
		Document [] docs = rdb.getAllDocumentsBySql(sql);
		JSONArray jsonarr = new JSONArray();
		for(Document doc : docs) {
			jsonarr.add(JSONObject.parse(doc.toJson()));
		}
		JSONObject json = new JSONObject();
		json.put("rows", jsonarr);
		return json;
	}

	/**
	 * 保存或更新人工活动节点配置的发送邮件信息到数据库表 BPM_MailConfig
	 *   筛选条件：流程id AND 节点id
	 *  注意：这里，如果是新建的数据，则insert一条，如果数据已经存在（WF_OrUnid判断）则进行update
	 * @param processid 流程id，必须
	 * @param nodeid 节点id，必须（节点id，为前端流程图设计器生成）
	 * @param formParmes 节点邮件配置表单F_S002_A025，中的所有字段信息组成JSON（包含 WF_OrUnid，新建时为空）
	 * @return {"status","0/1","msg":"提示信息"}
	 */
	@Override
	public JSONObject saveFlowChartMailConfigByNodeType(String processid, String nodeid, JSONObject formParmes) {
		Rdb rdb = BeanCtx.getRdb();
		String sql = "select * from BPM_MailConfig where WF_OrUnid = '"+formParmes.get("WF_OrUnid")+"'";
		Document doc = rdb.getDocumentBySql(sql);
		if(Tools.isBlank(doc.g("WF_OrUnid"))) {
			doc.s("WF_OrUnid",rdb.getNewUnid());
			doc.s("Processid",processid);
			doc.s("Nodeid",nodeid);
		}
		doc.s("SendTo",formParmes.get("SendTo"));
		doc.s("CopyTo",formParmes.get("CopyTo"));
		doc.s("MailTitle",formParmes.get("MailTitle"));
		doc.s("MailBody",formParmes.get("MailBody"));
		doc.s("Actionid",formParmes.get("Actionid"));
		doc.s("WF_LastModified",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		int i = doc.save();
		JSONObject json = new JSONObject();
        if (i > 0) {
        	json.put("ok", "保存成功");
        }
        else {
        	json.put("error", "保存失败");
        }
		return json;
	}


	/**
	 * 删除人工活动节点配置的发送邮件信息 表BPM_MailConfig
	 *   筛选条件：流程id AND 节点id
	 * @param docUnidList 需要删除行的 WF_OrUnid 键的值，多条记录使用逗号分隔
	 * @return {"status","0/1","msg":"提示信息"}
	 */
	@Override
	public JSONObject deleteFlowChartMailConfigByNodeType(String docUnidList) {
		String [] unids = docUnidList.split(",");
		Rdb rdb = BeanCtx.getRdb();
		int i = 0;
		for(String unid : unids) {
			String sql = "delete from BPM_MailConfig where WF_OrUnid = '"+unid+"'";
			if(rdb.execSql(sql)>0) {
				i++;
			}
		}
		JSONObject json = new JSONObject();
    	json.put("ok", "成功删除"+i+"条记录！");
		return json;
	}

	/**
	 * 获取人工活动节点配置的发送邮件信息 表BPM_MailConfig
	 *   筛选条件：流程id AND 节点id
	 * @param processid 流程id，必须
	 * @param nodeid 节点id，必须（节点id，为前端流程图设计器生成）
	 * @return {"status","0/1","msg":"提示信息","mailConfigRows":[{row1},{row2}]}
	 *  mailConfigRows 送邮件信息明细行（可能多行），依据wf_lastmodified进行升序排序，
	 *  row1 邮件信息单行明细内容，字段包含 表BPM_MailConfig 中的所有字段（包含WF_OrUnid）
	 */
	@Override
	public JSONObject getFlowChartMailConfigByNodeType(String processid, String nodeid) {
		Rdb rdb = BeanCtx.getRdb();
		String sql = "select * from BPM_MailConfig where Processid = '"+processid+"' and Nodeid = '"+ nodeid +"'";
		Document [] docs = rdb.getAllDocumentsBySql(sql);
		JSONArray jsonarr = new JSONArray();
		for(Document doc : docs) {
			jsonarr.add(JSONObject.parse(doc.toJson()));
		}
		JSONObject json = new JSONObject();
		json.put("rows", jsonarr);
		return json;
	}

	/**
	 * 获取人工活动节点配置的发送邮件信息 表BPM_MailConfig
	 *   筛选条件：流程id AND 节点id
	 * @param unid 数据id，必须
	 * @return {"status","0/1","msg":"提示信息","mailConfig":{row1}}
	 *  mailConfigRows 送邮件信息明细行，依据wf_lastmodified进行升序排序，
	 *  row1 邮件信息单行明细内容，字段包含 表BPM_MailConfig 中的所有字段（包含WF_OrUnid）
	 */
	@Override
	public JSONObject getFlowChartMailConfigByUnid(String unid) {
		Rdb rdb = BeanCtx.getRdb();
		String sql = "select * from BPM_MailConfig where WF_OrUnid = '"+unid+"'";
		Document doc = rdb.getDocumentBySql(sql);
		JSONObject json = new JSONObject();
		json = (JSONObject) JSONObject.parse(doc.toJson());
		return json;
	}
	
    /**
     * 存盘流程过程属性
     */
    public JSONObject saveProcess(String processid, String nodeid, String extNodeType, JSONObject formParmes) {
        String nodeTableName = Tools.getNodeTableName(processid, nodeid); //节点所在数据库表
    	String sql = "select * from " + nodeTableName + " where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
        Rdb rdb = BeanCtx.getRdb();
        Document nodeDoc = rdb.getDocumentBySql(sql);
        
//        nodeDoc.appendFromRequest(BeanCtx.getRequest());
        //录入数据
        for(String key : formParmes.keySet()) {
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
        }
        else {
        	json.put("error", "保存失败");
        }
        return json;
    }

	/**
	 * 流程保存前操作，如缺省保存，删除节点，校验
	 * @param processid 流程ID
	 * @param nodeid    节点ID
	 * @param nodeList  节点ID的字符串，逗号隔开
	 * @param action    CheckNodeAttr、SaveAllDefaultNode、DeleteNode
	 * @param nodeType   节点类型
	 * @param startNodeid  开始节点ID
	 * @param endNodeid    结束节点ID
	 * @return 操作结果
	 */
    @Override
    public JSONObject actionFlowChartGraphic(String processid, String nodeid, String nodeList, String action, String nodeType, String startNodeid, String endNodeid) {
    	if (action.equals("CheckNodeAttr")) {
    		return checkAllNodeAttr(processid,nodeList);
        }
        else if (action.equals("SaveAllDefaultNode")) {
        	return saveAllDefaultNodeAttr(processid,nodeid,nodeType,startNodeid,endNodeid);
        }
        else if (action.equals("DeleteNode")) {
        	return deleteNode(processid,nodeid);
        }
    	return null;
    }
    
    /**
     * 存盘通用节点
     */
    public JSONObject saveNode(String processid, String nodeid, String extNodeType, JSONObject formParmes) {
         String nodeTableName = Tools.getNodeTableName(processid, nodeid); //节点所在数据库表
         String sql = "select * from " + nodeTableName + " where Processid='" + processid + "' and Nodeid='" + nodeid + "'";
         Rdb rdb = BeanCtx.getRdb();
         Document nodeDoc = rdb.getDocumentBySql(sql);
         if(Tools.isBlank(nodeDoc.g("WF_OrUnid"))) {nodeDoc.s("WF_OrUnid", rdb.getNewUnid());}
         nodeDoc.s("ExtNodeType", extNodeType);
         nodeDoc.s("Processid", processid);
         nodeDoc.s("Nodeid", nodeid);
         for(String key : formParmes.keySet()) {
         	nodeDoc.s(key, formParmes.get(key));
         }
         nodeDoc.removeItem("QryNodeType");
         int i = nodeDoc.save();
         JSONObject json = new JSONObject();
         if (i > 0) {
         	json.put("ok", "保存成功");
         }
         else {
         	json.put("error", "保存失败");
         }
//         BeanCtx.userlog(processid, "修改流程节点", "修改流程节点(" + nodeDoc.g("NodeName") + ")");
    	return json;
    }

	/**
	 * 删除流程事件
	 * @param processid 流程ID
	 * @param nodeid     节点类型
	 * @param docUnidList  删除的记录id，多个以逗号隔开
	 * @return 处理结果 "ok", "成功删除"+i+"条记录！"
	 */
	@Override
    public JSONObject deleteFlowChartEventByNodeType(String processid, String nodeid, String docUnidList ) {
		String [] unids = docUnidList.split(",");
		Rdb rdb = BeanCtx.getRdb();
		int i = 0;
		for(String unid : unids) {
			String sql = "delete from BPM_EngineEventConfig where WF_OrUnid = '"+unid+"'";
			if(rdb.execSql(sql)>0) {
				i++;
			}
		}
		JSONObject json = new JSONObject();
    	json.put("ok", "成功删除"+i+"条记录！");
		return json;
	}

	/**
	 * 搜索流程
	 * @param searchStr 搜索字段，为空时返回所有流程
	 * @return 返回搜索或所有流程信息
	 */
    public JSONObject getProcessList(String searchStr) {
    	String sql = "select * from bpm_modprocesslist";
    	if(Tools.isNotBlank(searchStr)) {
    		sql += " where NodeName like '%"+searchStr+"%'";
    	}

    	sql += " Order by WF_DocCreated DESC";

        Rdb rdb = BeanCtx.getRdb();
        Document [] docs = rdb.getAllDocumentsBySql(sql);
        JSONArray jsonarr = new JSONArray();
		for(Document doc : docs) {
			jsonarr.add(JSONObject.parse(doc.toJson()));
		}
		JSONObject json = new JSONObject();
		json.put("rows", jsonarr);
        return json;
    	
    }


	/**
	 * 删除流程设计
	 * @param processids 流程ID(用逗号","隔开)
	 * @return 返回删除结果 "ok", "成功删除"+i+"条记录！"
	 */
    @Override
    public JSONObject deleteProcessList(String processids) {
		String [] processidArr = processids.split(",");
		Rdb rdb = BeanCtx.getRdb();
		int i = 0;
		for(String processid : processidArr) {
			String sql = "delete from bpm_modprocesslist where WF_OrUnid = '"+processid+"'";
			if(rdb.execSql(sql)>0) {
				i++;
			}
		}
		JSONObject json = new JSONObject();
    	json.put("ok", "成功删除"+i+"条记录！");
		return json;
	}

	/**
	 * 表单字段配置
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
	 * @param formConfig 表单字段配置JSON字符串
	 * @param processid  对应流程ID
	 * @return {"status","0/1","msg":"提示信息"}
	 */
	@Override
	public JSONObject setFormConfig(String formConfig, String processid) {

		String sql = "select * from bpm_modprocesslist where Processid = '" + processid + "'";

		Rdb rdb = BeanCtx.getRdb();
		Document doc = rdb.getDocumentBySql(sql);

		if(Tools.isBlank(doc.g("WF_OrUnid"))){
			doc.s("WF_OrUnid",rdb.getNewUnid());
		}

		doc.s("formConfig",formConfig);

		doc.save();

		JSONObject returnJSON = new JSONObject();
		returnJSON.put("status","1");
		returnJSON.put("msg","操作成功~");

		return returnJSON;
	}

	/**
	 * 通用删除表单记录方法
	 * add by alibao 202010
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
		if(i>0){
			returnJSON.put("status","1");
			returnJSON.put("msg","删除成功~");
		}else{
			returnJSON.put("status","0");
			returnJSON.put("msg","删除出错！");
		}
		return returnJSON;
	}

	/**
	 * 获取节点操作按钮配置
	 * add by alibao 202010
	 * @param processid  流程ID
	 * @param nodeid     节点ID
	 * @return 操作按钮配置JSON
	 */
	@Override
	public JSONObject getButtonConfig(String processid, String nodeid) {

		String sql = "select nodeButtonConfig from bpm_modtasklist where Processid = '" + processid + "' and Nodeid='" + nodeid + "'";
		Rdb rdb = BeanCtx.getRdb();
		String nodeButtonConfig = rdb.getValueBySql(sql);

		if(Tools.isBlank(nodeButtonConfig)){ nodeButtonConfig="{total:0,rows:[]}";}

		return JSONObject.parseObject(nodeButtonConfig);

	}

	/**
	 * 配置节点操作按钮
	 * add by alibao 202010
	 * @param buttonConfig 节点操作按钮配置
	 * @param processid  对应流程ID
	 * @param nodeid      对应用节点id
	 * @return {"status","0/1","msg":"提示信息"}
	 */
	@Override
	public JSONObject setButtonConfig(String buttonConfig, String processid, String nodeid) {

		String sql = "select * from bpm_modtasklist where Processid='" + processid + "' and Nodeid='" + nodeid + "'";

		Rdb rdb = BeanCtx.getRdb();
		Document doc = rdb.getDocumentBySql(sql);

		if(Tools.isBlank(doc.g("WF_OrUnid"))){
			doc.s("WF_OrUnid",rdb.getNewUnid());
		}

		doc.s("nodeButtonConfig",buttonConfig);

		doc.save();

		JSONObject returnJSON = new JSONObject();
		returnJSON.put("status","1");
		returnJSON.put("msg","操作成功~");

		return returnJSON;

	}


	/**
	 * 获得所有节点按钮的动作信息
	 * add by alibao 202010
	 * [{"ActionName":"提交下一会签用户","Actionid":"GoToNextParallelUser"},{"ActionName":"收回文档","Actionid":"Undo"},{"ActionName":"结束当前环节并推进到下一环节","Actionid":"GoToNextNode"},{"ActionName":"回退首环节","Actionid":"GoToFirstNode"},{"ActionName":"尝试结束子流程节点","Actionid":"EndSubProcessNode"},{"ActionName":"回退上一环节","Actionid":"GoToPrevNode"},{"ActionName":"暂停","Actionid":"Pause"},{"ActionName":"标记为阅","Actionid":"EndCopyTo"},{"ActionName":"后台启动用户任务","Actionid":"StartUser"},{"ActionName":"办理完成","Actionid":"EndUserTask"},{"ActionName":"转他人处理","Actionid":"GoToOthers"},{"ActionName":"归档","Actionid":"GoToArchived"},{"ActionName":"后台结束用户任务","Actionid":"EndUser"},{"ActionName":"提交任意环节","Actionid":"GoToAnyNode"},{"ActionName":"回退上一用户","Actionid":"GoToPrevUser"},{"ActionName":"后台结束节点","Actionid":"EndNode"},{"ActionName":"回退任意环节","Actionid":"ReturnToAnyNode"},{"ActionName":"返回给转交者","Actionid":"BackToDeliver"},{"ActionName":"恢复","Actionid":"UnPause"},{"ActionName":"提交下一串行用户","Actionid":"GoToNextSerialUser"},{"ActionName":"自动运行","Actionid":"AutoRun"},{"ActionName":"返回给回退者","Actionid":"BackToReturnUser"},{"ActionName":"传阅用户","Actionid":"CopyTo"},{"ActionName":"同步任务","Actionid":"SyncUserTask"}]
	 * @return 返回所有按钮动作 JSON信息
	 */
	@Override
	public JSONArray getALLActionConfig() {

		String sql = "select ActionName,Actionid from bpm_engineactionconfig";

		Rdb rdb = BeanCtx.getRdb();
		Document[] docs = rdb.getAllDocumentsBySql(sql);
		String atcionJsonStr = Tools.dc2json(docs,"",false);

		return JSONArray.parseArray(atcionJsonStr);
	}



	/**
	 * 新增流程id
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
     * @param processid
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
     */
    public JSONObject saveAllDefaultNodeAttr(String processid, String nodeid, String nodeType, String startNodeid, String endNodeid ) {
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
            }
            else if (nodeType.equals("EndNode")) {
                //结束节点
                doc.s("NodeName", "结束");
                doc.s("ExtNodeType", "endEvent");
                doc.s("Terminate", "1");
                doc.s("EndBusinessName", "已结束");
                doc.s("EndBusinessid", "1");
                doc.setTableName("BPM_ModEventList");
            }
            else if (nodeType.equals("StartNode")) {
                //开始节点
                doc.s("NodeName", "开始");
                doc.s("ExtNodeType", "startEvent");
                doc.setTableName("BPM_ModEventList");
            }
            else if (nodeType.equals("Event")) {
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
    
}
