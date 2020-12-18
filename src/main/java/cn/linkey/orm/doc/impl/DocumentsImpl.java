package cn.linkey.orm.doc.impl;

import cn.linkey.orm.doc.Document;
import cn.linkey.orm.doc.Documents;
import cn.linkey.orm.util.Tools;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class DocumentsImpl implements Documents{

    /**
     * 删除文档集合并把文档放入到回收站中去
     * 
     * @param dc 数组，文档集合
     * @return 返回成功删除的文档数 int
     */
    @Override
    public int remove(Document[] dc) {
        int i = 0;
        for (Document doc : dc) {
            int j = doc.remove(true);
            if (j > 0) {
                i++;
            }
        }
        return i;
    }

    /**
     * 删除文档集合并把文档放入到回收站中去
     * 
     * @param dc HashSet 文档集合
     * @return 返回成功删除的文档数
     */
    @Override
    public int remove(HashSet<Document> dc) {
        int i = 0;
        for (Document doc : dc) {
            int j = doc.remove(true);
            if (j > 0) {
                i++;
            }
        }
        return i;
    }

    /**
     * 存盘集合中的所有文档对像到数据库中
     * 
     * @param dc 文档数组
     * @return 返回非负数为表示存盘成功
     */
    @Override
    public int saveAll(Document[] dc) {
    	int i = 0;
        for (Document doc : dc) {
            int j = doc.save();
            if (j > 0) {
                i++;
            }
        }
        return i;
    }

    /**
     * 存盘集合中的所有文档对像到数据库中
     * 
     * @param dc 文档set集合
     * @return 返回非负数为表示存盘成功
     */
    @Override
    public int saveAll(Set<Document> dc) {
    	int i = 0;
        for (Document doc : dc) {
            int j = doc.save();
            if (j > 0) {
                i++;
            }
        }
        return i;
    }

    /**
     * 文档数组对像转为json字符串
     * 
     * @param dc 文档集合使用Rdb.GetAllDocumentsBySql()获得
     * @param key 是否需要使用key关键字进行输出如传入rows输出结果为 {"rows":[{json1},{json2}]},否则传入空值标准输出为 [{json1},{json2}]
     * @return 返回json格式字符串，格式有与是否传入key有关
     */
    @Override
    public String dc2json(Document dc[], String key) {
        return dc2json(dc, key, false);
    }

    /**
     * 文档数组对像转为json字符串
     * 
     * @param dc 文档集合使用Rdb.GetAllDocumentsBySql()获得
     * @param key 是否需要使用key关键字进行输出如传入rows输出结果为 {"rows":[{json1},{json2}]} 否则传入空值标准输出为 [{json1},{json2}]
     * @param useTableConfig true 表示使用配置表中的字段信息进行输出,false 表示使用数据库表中的字段信息进行输出
     * @return 返回json字符串，格式由是否有key决定
     */
    @Override
    public String dc2json(Document dc[], String key, boolean useTableConfig) {
        StringBuilder jsonStr = new StringBuilder();
        if (Tools.isBlank(key)) {
            jsonStr.append("[");
        }
        else {
            jsonStr.append("{\"" + key + "\":[");
        }
        int i = 0;
        for (Document doc : dc) {
            if (i == 0) {
                i = 1;
            }
            else {
                jsonStr.append(",");
            }
            jsonStr.append(doc.toJson(useTableConfig));
        }
        if (Tools.isBlank(key)) {
            jsonStr.append("]");
        }
        else {
            jsonStr.append("]}");
        }
        return jsonStr.toString();
    }

    /**
     * 文档集合转为json字符串
     * 
     * @param dc 文档集合使用Rdb.GetAllDocumentsSetBySql()获得
     * @param key 是否需要使用key关键字进行输出如传入rows输出结果为{"rows":[{json1},{json2}]} 否则传入空值标准输出为 [{json1},{json2}]
     * @return 返回json格式字符串，格式有是否有key决定
     */
    @Override
    public  String dc2json(Set<Document> dc, String key) {
        return dc2json(dc, key, false);
    }

    /**
     * 文档集合转为json字符串
     * 
     * @param dc 文档集合使用Rdb.GetAllDocumentsSetBySql()获得
     * @param key 是否需要使用key关键字进行输出如传入rows输出结果为{"rows":[{json1},{json2}]} 否则传入空值标准输出为 [{json1},{json2}]
     * @param useTableConfig true表示使用配置表中的字段信息进行输出,false表示使用数据库表中的字段信息进行输出
     * @return 返回json格式字符串，格式由是否有key决定
     */
    @Override
    public  String dc2json(Set<Document> dc, String key, boolean useTableConfig) {
        StringBuilder jsonStr = new StringBuilder();
        if (Tools.isBlank(key)) {
            jsonStr.append("[");
        }
        else {
            jsonStr.append("{\"" + key + "\":[");
        }
        int i = 0;
        for (Document doc : dc) {
            if (i == 0) {
                i = 1;
            }
            else {
                jsonStr.append(",");
            }
            jsonStr.append(doc.toJson(useTableConfig));
        }
        if (Tools.isBlank(key)) {
            jsonStr.append("]");
        }
        else {
            jsonStr.append("]}");
        }
        return jsonStr.toString();
    }

    /**
     * 文档数组对像转为xml字符串
     * 
     * @param dc 文档集合使用Rdb.GetAllDocumentsBySql()获得
     * @param isCDATA 术语 CDATA 指的是不应由 XML 解析器进行解析的文本数据（Unparsed Character Data）
     * @return 返回xml格式的字符串
     */
    @Override
    public  String dc2XmlStr(Document[] dc, boolean isCDATA) {
        StringBuilder xmlStr = new StringBuilder();
        xmlStr.append("<documents>");
        for (Document doc : dc) {
            xmlStr.append(doc.toXmlStr(isCDATA));
        }
        xmlStr.append("</documents>");
        return xmlStr.toString();
    }

    /**
     * 文档Set集合转为xml字符串
     * 
     * @param dc 文档集合使用Rdb.GetAllDocumentsSetBySql()获得
     * @param isCDATA 术语 CDATA 指的是不应由 XML 解析器进行解析的文本数据（Unparsed Character Data）
     * @return xml格式的字符串
     */
    @Override
    public  String dc2XmlStr(Set<Document> dc, boolean isCDATA) {
        StringBuilder xmlStr = new StringBuilder();
        xmlStr.append("<documents>");
        for (Document doc : dc) {
            xmlStr.append(doc.toXmlStr(isCDATA));
        }
        xmlStr.append("</documents>");
        return xmlStr.toString();
    }

    /**
     * 导出dc为xml并写入到硬盘中 如果导出去的xml文件要能再导回来，则文档中必须要有WF_OrTableName字段
     * 
     * @param dc 文档数组
     * @param fileName 要生成的文件名称，要全路径
     * @param isCDATA 是否使用cdata标记
     * @return 返回true表示成功
     */
    @Override
    public  boolean dc2Xmlfile(Document[] dc, String fileName, boolean isCDATA) {

        return false;
    }

    /**
     * 导出dc为xml并写入到硬盘中 如果导出去的xml文件要能再导回来，则文档中必须要有WF_OrTableName字段
     * 
     * @param dc 文档集合
     * @param fileName 要生成的文件名称，要全路径
     * @param isCDATA 是否使用cdata标记
     * @return 返回true表示成功
     */
    @Override
    public  boolean dc2Xmlfile(Set<Document> dc, String fileName, boolean isCDATA) {
       return false;
    }

    /**
     * 操作的是缺省数据源中的表 指定xmlfilepath的路径，把xml中的内容转换为文档集合对像,xml文档必须是标准导出的xml文档
     *
     * @param xmlFilePath 如：d:\doc.xml
     * @return 返回文档集合对像
     */
    @Override
    public  LinkedHashSet<Document> xmlfile2dc(String xmlFilePath) {
       return  null;
    }

    /**
     * json字符串转换为文档集合对像
     * 
     * @param jsonStr json字符串格式为[{fdName:fdValue,fdName2:fdValue2},{fdName:fdValue1,fdName2:fdValue3}]
     * @return 返回文档集合
     */
    @Override
    public  LinkedHashSet<Document> jsonStr2dc(String jsonStr) {
        return jsonStr2dc(jsonStr, "");
    }

    /**
     * json字符串转换为文档集合对像
     * 
     * @param jsonStr json字符串格式为[{fdName:fdValue,fdName2:fdValue2},{fdName:fdValue1,fdName2:fdValue3}]
     * @param tableName 指定文档所依附的sql数据库表名,如果没有传空字符串
     * @return 返回文档集合
     */
    @Override
    public  LinkedHashSet<Document> jsonStr2dc(String jsonStr, String tableName) {
        LinkedHashSet<Document> dc = new LinkedHashSet<Document>();
        JSONArray jsonArr = JSON.parseArray(jsonStr);
        for (int i = 0; i < jsonArr.size(); i++) {
            JSONObject jsonobj = (JSONObject) jsonArr.get(i);
            Document doc = new DocumentImpl(tableName);
            for (String fdName : jsonobj.keySet()) {
                doc.s(fdName, jsonobj.getString(fdName));
            }
            dc.add(doc);
        }
        return dc;
    }

    /**
     * 追加文档到文档数组中去
     * @param dc 被追加的数组文档
     * @param doc 追加的文档对象
     * @return 返回文档数组
     */
    @Override
    public  Document[] addDoc(Document[] dc, Document doc) {
        Document[] newDc = new Document[dc.length + 1];
        System.arraycopy(dc, 0, newDc, 0, dc.length);
        newDc[dc.length] = doc;
        return newDc;
    }

    /**
     * Excel文件转为文档集合dc
     * 
     * @param filePath excel文件所在全路径
     * @param columnName excel每列对应的字段名必须与Excel列数一样多否则出错 如果不指定columnName则excel的第一行必须为字段名称
     * @param tableName 可以为空，如果导入的文档要存入到某一张表中去则可以直接指定表名
     * @return 返回文档集合 LinkedHashSet
     */
    @Override
    public  LinkedHashSet<Document> excel2dc(String filePath, String columnName, String tableName) {

        return null;
    }

    /**
     * 文档数组转换为Excel文件
     * 
     * @param dc 要转换的文档数组
     * @param filePath 指定Excel文件路径
     * @return 返回true表示成功,false表示失败
     */
    @Override
    public  boolean dc2Excel(Document[] dc, String filePath) {
        return false;
    }

}
