package cn.linkey.flowchart.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author Mr.Yun
 * 流程图统一接口，所有与流程图相关的内容前后端处理，都将经过本接口
 * 一切只为成就更好的您
 *  1.0
 * 2020/07/31 13:06
 */
public interface FlowChart {

    /**
     * 保存流程图模型数据接口
     *  保存流程图到表：BPM_ModGraphicList中，保存前必须验证在BPM_ModProcessList中有对应processid的流程图配置信息
     * @param processid 流程id，必须
     * @param flowJSON 流程图模型数据，必须
     * @return {"status","0/1","msg":"提示信息"}
     */
    public JSONObject saveFlowChartGraphic(String processid, String flowJSON);

    /**
     * 获取流程图模型数据接口
     * 前端获得数据后，依据Processid和flowJSON的内容，重新将保存过的流程图模型进行渲染显示
     * @param processid 流程id，非必须，为空时表示新建流程
     * @return {"status","0/1","msg":"提示信息","Processid":"36位UUID","flowJSON":"流程图模型数据"}
     */
    public JSONObject getFlowChartGraphic(String processid);

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
    public JSONObject saveFlowChartModByNodeType(String processid, String nodeid, String extNodeType, JSONObject formParmes);

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
    public JSONObject getFlowChartModByNodeType(String processid, String nodeid);

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
    public JSONObject saveFlowChartEventByNodeType(String processid, String nodeid, JSONArray eventRows);

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
    public JSONObject getFlowChartEventByNodeType(String processid, String nodeid);

    /**
     * 保存或更新人工活动节点配置的发送邮件信息到数据库表 BPM_MailConfig
     *   筛选条件：流程id AND 节点id
     *  注意：这里，如果是新建的数据，则insert一条，如果数据已经存在（WF_OrUnid判断）则进行update
     * @param processid 流程id，必须
     * @param nodeid 节点id，必须（节点id，为前端流程图设计器生成）
     * @param formParmes 节点邮件配置表单F_S002_A025，中的所有字段信息组成JSON（包含 WF_OrUnid，新建时为空）
     * @return {"status","0/1","msg":"提示信息"}
     */
    public JSONObject saveFlowChartMailConfigByNodeType(String processid, String nodeid, JSONObject formParmes);

    /**
     * 删除人工活动节点配置的发送邮件信息 表BPM_MailConfig
     *   筛选条件：流程id AND 节点id
     * @param processid 流程id，必须
     * @param nodeid 节点id，必须（节点id，为前端流程图设计器生成）
     * @param docUnidList 需要删除行的 WF_OrUnid 键的值，多条记录使用逗号分隔
     * @return {"status","0/1","msg":"提示信息"}
     */
    public JSONObject deleteFlowChartMailConfigByNodeType(String processid, String nodeid, String docUnidList);

    /**
     * 获取人工活动节点配置的发送邮件信息 表BPM_MailConfig
     *   筛选条件：流程id AND 节点id
     * @param processid 流程id，必须
     * @param nodeid 节点id，必须（节点id，为前端流程图设计器生成）
     * @return {"status","0/1","msg":"提示信息","mailConfigRows":[{row1},{row2}]}
     *  mailConfigRows 送邮件信息明细行（可能多行），依据wf_lastmodified进行升序排序，
     *  row1 邮件信息单行明细内容，字段包含 表BPM_MailConfig 中的所有字段（包含WF_OrUnid）
     */
    public JSONObject getFlowChartMailConfigByNodeType(String processid, String nodeid);


    /**
     * 获取人工活动节点配置的发送邮件信息 表BPM_MailConfig
     *   筛选条件：流程id AND 节点id
     * @param unid 数据id，必须
     * @return {"status","0/1","msg":"提示信息","mailConfig":{row1}}
     *  mailConfigRows 送邮件信息明细行，依据wf_lastmodified进行升序排序，
     *  row1 邮件信息单行明细内容，字段包含 表BPM_MailConfig 中的所有字段（包含WF_OrUnid）
     */
    public JSONObject getFlowChartMailConfigByUnid(String unid);

    /**
     * 流程操作规则
     * @param processid 流程ID
     * @param nodeid  节点ID
     * @param nodeList 节点List，用于CheckNodeAttr
     * @param action 动作引擎
     * @param nodeType 节点类型
     * @param startNodeid 开始节点
     * @param endNodeid 结束节点
     * @return 返回操作结果
     */
    public JSONObject actionFlowChartGraphic(String processid, String nodeid, String nodeList, String action,String nodeType,String startNodeid,String endNodeid);

    /**
     * 查询流程列表
     * @param searchStr 搜索字符串
     * @return 返回流程JSON
     */
    public JSONObject getProcessList(String searchStr);

    /**
     * 删除流程
     * @param processids 流程ID(用逗号","隔开)
     * @return 返回操作结果
     */
    public JSONObject deleteProcessList(String processids);

    /**
     * 表单字段配置
     * add by alibao 202010
     * @param processid 流程ID
     * @return 表单字段配置JSON
     */
    public JSONObject getFormConfig(String processid);

    /**
     * 设置表单字段配置
     * add by alibao 202010
     * @param formConfig 表单字段配置JSON字符串
     * @param processid  对应流程ID
     * @return {"status","0/1","msg":"提示信息"}
     */
    public JSONObject setFormConfig(String formConfig,String processid);

    /**
     * 通用删除表单记录方法
     * add by alibao 202010
     * @param tableName 需要删除的表名称
     * @param wforunid  需要删除的唯一字段WF_Orunid
     * @return {"status","0/1","msg":"提示信息"}
     */
    public JSONObject delCommonTableRows(String tableName, String wforunid);


    /**
     * 获得WF_Orunid
     * @return 返回唯一ID
     */
    public JSONObject getUnid();
}
