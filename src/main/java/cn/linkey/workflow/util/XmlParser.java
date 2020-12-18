package cn.linkey.workflow.util;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

public class XmlParser {

    /**
     * 获得一个节点下所有text文本内容
     *
     * @param item 节点对像
     * @return 返回节点下所有text文本内容
     */
    public static String getElementText(Element item) {
        String getText = "";
        List<Element> itemList = item.elements();
        for (Element subItem : itemList) {
            int subSize = subItem.elements().size();
            if (subSize > 0) {
                getText += getElementText(subItem);
            }
            else {
                getText = subItem.getText();
            }
        }
        return getText;
    }

    /**
     * 把xmldata字符串转换成为hashmap对像
     * @param xml xml格式的字符串{@code <Items><WFItem name=\"NewField\">linkey</WFItem><WFItem name=\"MeetingAddress\">新的&amp;会议室</WFItem></Items>}
     * @return 返回字段名和字段值的map对像
     */
    protected static HashMap<String, String> getXmlData(String xml) {
        String fdName, fdValue;
        HashMap<String, String> fdMap = new HashMap<String, String>();
        if (Tools.isBlank(xml)) {
            return fdMap;
        }
        Document doc = string2XmlDoc(xml);
        List<Element> list = doc.selectNodes("/Items/WFItem");
        for (Element item : list) {
            fdName = item.attribute("name").getValue();
            fdValue = item.getText();
            fdMap.put(fdName, fdValue);
        }
        return fdMap;
    }

    /**
     * string2Document 将字符串转为Document
     * @param s xml格式的字符串
     * @return 转换后的基于xml的document对象
     */
    public static Document string2XmlDoc(String s) {
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(s);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return doc;
    }

    /**
     * doc2String 将xml文档内容转为String
     * @param document 文档对象
     * @return 字符串
     */
    protected static String doc2String(Document document) {
        String s = "";
        try {
            // 使用输出流来进行转化
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 使用GB2312编码
            OutputFormat format = new OutputFormat("  ", true, "UTF-8");
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);
            s = out.toString("UTF-8");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return s;
    }

}
