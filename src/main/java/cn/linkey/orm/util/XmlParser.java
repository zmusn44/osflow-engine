package cn.linkey.orm.util;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

public class XmlParser {

    /**
     * string2Document 将字符串转为Document
     * @param s xml格式的字符串
     * @return 返回xml的Document对象
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
}