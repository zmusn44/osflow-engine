package cn.linkey.orm.doc.impl;

import cn.linkey.orm.dao.Rdb;
import cn.linkey.orm.doc.Document;
import cn.linkey.orm.doc.SaveDoc;
import cn.linkey.orm.util.Tools;
import cn.linkey.workflow.factory.BeanCtx;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 存盘文档对像,针对PG SQL对像，表结构中需要有WF_OrUnid关键字段 如果有自增长列则必须在key参数中指定
 *
 * @author Administrator
 */
public class SaveDocPGSQL implements SaveDoc {
    /**
     * 存盘文档对像，多余字段存储在xmldata字段中
     *
     * @param doc 要存盘的文档
     * @param conn 指定数据库链接对像，如果为当前链接则可以传null空值
     * @return 返回非负数表示存盘成功
     */
    @Override
    public int save(Connection conn, Document doc, String tableName) throws Exception {
        return save(conn, doc, tableName, "xmldata");
    }

    /**
     * 存盘文档对像
     * @param conn 指定数据库链接对像，如果为当前链接则可以传null空值
     * @param doc  要存盘的文档
     * @param tableName 表名
     * @param extendTableName 扩展表名
     * @return 处理结果
     * @throws Exception 异常抛出
     */
    @Override
    public int save(Connection conn, Document doc, String tableName, String extendTableName) throws Exception {
        Rdb rdb = BeanCtx.getRdb();
        // 是否新文档标记
        boolean isNewDoc = true;
        String docUnid = doc.getDocUnid();
        String sql = "select " + doc.getKeyid() + " from " + tableName + " where " + doc.getKeyid() + "='" + docUnid + "'";

        if (rdb.hasRecord(conn, sql)) {
            isNewDoc = false;
        }
        HashMap<String, String> fdList = new HashMap<String, String>();
        if (tableName.equalsIgnoreCase(doc.getTableName())) {
            // 如果表名和文档是相等的情况下直接从文档对像中拿
            // 这里要保证一定能拿到文档所在数所库表的清单，否则报错,这里不能直接操作getColumnList()返回的hashSet对像，只以复制一份
            fdList.putAll(doc.getColumnList());
        }
        else {
            // 如果表名和文档对像中的表名不一至，说明是存扩展表，扩展表是没有缓存的
            // 这里要保证一定能拿到文档所在数所库表的清单，否则报错,这里不能直接操作getColumnList()返回的hashSet对像，只以复制一份
            fdList.putAll(rdb.getTableColumnName(conn, tableName));
        }
        // 把表的主键去掉，不然自增长列插不进去
        fdList.remove(doc.getAutoKeyid());
        if (isNewDoc) {
            // 如果是新文档则要设置默认字段
            if (Tools.isBlank(doc.g("wf_orunid"))) {
                doc.s("wf_orunid", docUnid);
            }
            if (doc.hasTableItem("wf_doccreated") && Tools.isBlank(doc.g("wf_doccreated"))) {
                doc.s("wf_doccreated", Tools.getNow());
            }
            if (doc.hasTableItem("wf_addname") && Tools.isBlank(doc.g("wf_addname"))) {
                doc.s("wf_addname", BeanCtx.getUserid());
            }
            if (doc.hasTableItem("wf_addname_cn") && Tools.isBlank(doc.g("wf_addname_cn"))) {
                doc.s("wf_addname_cn", BeanCtx.getUserName());
            }
        }
        // 设置最后更新时间
        if (doc.hasTableItem("wf_lastmodified")) {
            doc.s("wf_lastmodified", Tools.getNow());
        }
        // 设置最后更新者
        if (doc.hasTableItem("wf_lastupdateuser")) {
            doc.s("wf_lastupdateuser", BeanCtx.getUserid());
        }
        // 检测是否有多个WF_Appid只取第一个
        String appid = doc.g("wf_appid");
        if (appid.indexOf(",") != -1) {
            doc.s("wf_appid", appid.substring(0, appid.indexOf(",")));
        }
        String fdValue;
        int i = 0;
        StringBuilder insertSql = new StringBuilder();
        StringBuilder updateSql = new StringBuilder();
        HashSet<String> removeFdSet = new HashSet<String>();
        for (String fdName : fdList.keySet()) {
            if (!doc.hasItem(fdName) && !fdName.equalsIgnoreCase("XmlData")) {
                removeFdSet.add(fdName);
                continue;
            } // 如果字段在doc中不存在则跳过更新

            if (extendTableName.equals("xmldata") && fdName.equalsIgnoreCase("xmldata")) {
                // 扩展表名必须是xmldata时才需要进行存储
                fdValue = getXmlData(doc, fdList.keySet());
                fdValue = fdValue.replace("&amp;", "&");
            }
            else {
                fdValue = doc.g(fdName);
                // 替换单引号为两个单引号
                fdValue = fdValue.replace("'", "''");
                // 替换转义字符
                //fdValue = fdValue.replace("\\", "\\\\");

                // 看存盘时是否需要进行<>的编码
                //if (BeanCtx.getEnCodeStatus()) {
                    fdValue = fdValue.replace("<", "&lt;").replace(">", "&gt;");
                //}
            }
            // 根据字段类型获得分隔符
            String separator = getFieldSeparator(fdList.get(fdName));
            fdValue = fdValue.replace("©", "&copy;").replace("®", "&reg;").replace("™", "&trade;");
            if (isNewDoc) {
                // 组成insert 语句
                if (i == 0) {
                    insertSql.append(separator).append(fdValue).append(separator);
                    i = 1;
                }
                else {
                    insertSql.append(",").append(separator).append(fdValue).append(separator);
                    i = 1;
                }
            }
            else {
                // 组成update语句
                if (i == 0) {
                    updateSql.append(fdName).append("=").append(separator).append(fdValue).append(separator);
                    i = 1;
                }
                else {
                    updateSql.append(",").append(fdName).append("=").append(separator).append(fdValue).append(separator);
                }
            }
        }

        // 删除文档中没有的字段
        for (String fdName : removeFdSet) {
            fdList.remove(fdName);
        }
        // 再组成最后的SQL语句保存文档数据
        StringBuilder strSql;
        if (isNewDoc) {
            strSql = new StringBuilder(insertSql.length() + 200);
            strSql.append("insert into ").append(tableName).append("(" + Tools.join(fdList.keySet(), ",") + ") values(").append(insertSql).append(")");
        }
        else {
            strSql = new StringBuilder(updateSql.length() + 200);
            strSql.append("update ").append(tableName).append(" set ").append(updateSql).append(" where " + doc.getKeyid() + "='" + docUnid + "'");
        }
        strSql.trimToSize();
        //System.out.println("strSql:"+strSql.toString());
        // 如果扩展表名不是xmldata的情况下还要再调用一次来把数据存到扩展表中去
        if (!extendTableName.equalsIgnoreCase("xmldata")) {
            save(conn, doc, extendTableName, "xmldata");
        }

        try {
            if (conn == null) {
                // 执行sql语句保存文档
                return rdb.execSql(strSql.toString());
            }
            else {
                // 执行sql语句保存文档
                return rdb.execSql(conn, strSql.toString());
            }
        }
        catch (Exception e) {
            System.out.println("SQL=" + strSql.toString());
            return -1;
        }
    }

    /**
     * 返回当前文档对像的XmlData所应该存的字符串,字段名称中不能包含wf_字样，不然就存不进xmldata数据中
     * @param doc 数据文档
     * @param fdList 为数据库表字段清单列表
     * @return 返回xml字符串
     */
    private String getXmlData(Document doc, Set<String> fdList) {
        boolean f = false;
        StringBuilder XmlData = new StringBuilder();
        XmlData.append("<Items>");
        // 得到所有字段列表
        Set<String> allItems = doc.getAllItemsName();
        for (String fdName : allItems) {
            // (表字段中不包含本字段 且 字段名称不能WF_开头)或者 字段名称以_show节尾
            // if ((!fdList.contains(fdName) && !fdName.toLowerCase().startsWith("wf_")) || fdName.endsWith("_show")) {
            //20181009 添加setContainsItem方法，替代!fdList.contains(fdName)，忽略字段大小写
            if (!(setContainsItem(fdName, fdList) && !fdName.toLowerCase().startsWith("wf_")) || fdName.endsWith("_show")) {
                // 字段不在数据库表中则全部放入到XmlData中去
                String fdValue = doc.g(fdName);
                if (Tools.isNotBlank(fdValue)) {
                    fdValue = "<![CDATA[" + fdValue.replace("'", "''") + "]]>";
                }
                XmlData.append("<WFItem name=\"" + fdName + "\">" + fdValue + "</WFItem>");
                f = true;
            }
        }
        XmlData.append("</Items>");
        if (f) {
            return XmlData.toString();
        }
        else {
            return "";
        }
    }

    /**
     * 根据字段类型获得字段的分隔符
     *
     * @return
     */
    private String getFieldSeparator(String typeName) {
        String separator = "'";
        // 从配置值中获取
        String fieldTypeList="int,bigint,float,NUMBER";
        String typeList = fieldTypeList.toLowerCase();
        if (typeList.indexOf(typeName.toLowerCase() + ",") != -1) {
            separator = "";
        }
        return separator;
    }

    /**
     *
     * @Description 判断Set<String> 集合是否包含某个fdName，忽略大小写[BPM 12.0新增]
     *
     * @param fdName 需要匹配的fName
     * @param fdList 需要在此Set集合中查找
     * @return true 存在 false 不存在
     */
    private boolean setContainsItem(String fdName, Set<String> fdList){

        boolean flag = false;

        for(String key : fdList){
            if(fdName.equalsIgnoreCase(key)){
                flag = true;
                break;
            }
        }
        return flag;
    }
}
