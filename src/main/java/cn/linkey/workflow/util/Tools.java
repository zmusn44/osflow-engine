package cn.linkey.workflow.util;

import cn.linkey.orm.doc.Document;
import cn.linkey.workflow.factory.BeanCtx;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类
 * 
 * @author Administrator
 * 
 */
public class Tools {

	/**
	 * Set转字符串同时去掉空值
	 * 
	 * @param set 对像
	 * @param key 分隔字符串
	 * @return 返回逗号分隔的字符串
	 */
	public static String join(Set<String> set, String key) {
		StringBuilder fdNameList = new StringBuilder();
		int i = 0;
		set.remove("");
		for (String item : set) {
			if (i == 0) {
				fdNameList.append(item);
				i = 1;
			} else {
				fdNameList.append(key + item);
			}
		}
		return fdNameList.toString();
	}

	/**
	 * 把字符串按逗号分解成为字符串数组
	 * 
	 * @param str 要分析的字符串
	 * @return 字符串数组
	 */
	public static String[] split(String str) {
		return split(str, ",");
	}

	/**
	 * 把字符串分解成为字符串数组
	 * 
	 * @param str 要分析的字符串
	 * @param key 关键字符串
	 * @return 字符串数组
	 */
	public static String[] split(String str, String key) {
		if (key.length() > 1) {
			return StringUtils.splitByWholeSeparator(str, key); // key为多个字符时要作为一个整体进行分隔
		} else {
			return StringUtils.split(str, key);
		}
	}

	/**
	 * 把字符串的两端空格去掉
	 * 
	 * @param str 要去掉两端空格的字符串
	 * @return 去掉两端空格后的字符串
	 */
	public static String trim(String str) {
		return StringUtils.trim(str);
	}

	/**
	 * 替换字符串
	 * 
	 * @param text 要替找的字符串
	 * @param searchString 旧字符串
	 * @param replacement 新字符串
	 * @return 替换后的字符串
	 */
	public static String replace(String text, String searchString, String replacement) {
		return StringUtils.replace(text, searchString, replacement);
	}

	/**
	 * 替换字符串一次
	 * 
	 * @param text 要替找的字符串
	 * @param searchString 旧字符串
	 * @param replacement 新字符串
	 * @return 替换后的字符串
	 */
	public static String replaceOne(String text, String searchString, String replacement) {
		return StringUtils.replaceOnce(text, searchString, replacement);
	}

	/**
	 * 把字符串切分为字List集合对像
	 * 
	 * @param str 要切分的字符串
	 * @param key 关键字
	 * @return List集合
	 */
	public static List<String> splitAsList(String str, String key) {
		if (str == null) {
			str = "";
		}
		return Arrays.asList(StringUtils.split(str, key));
	}

	/**
	 * 把字符串按逗号切分为字List集合对像
	 * 
	 * @param str 要切分的字符串
	 * @return List集合
	 */
	public static List<String> splitAsList(String str) {
		return splitAsList(str, ",");
	}

	/**
	 * 把字符串按逗号切分为字Set集合对像同时会去掉重复值,这个函数有问题
	 * 
	 * @param str 要切分的字符串
	 * @return HashSet集合
	 */
	public static HashSet<String> splitAsSet(String str) {
		return splitAsSet(str, ",");
	}

	/**
	 * 把字符串切分为字Set集合对像同时会去掉重复值, 这个函数有问题，复杂字符串时会出错
	 * 
	 * @param str 要切分的字符串
	 * @param key 关键字
	 * @return HashSet集合
	 */
	public static HashSet<String> splitAsSet(String str, String key) {
		String[] strArray = StringUtils.split(str, key);
		HashSet<String> set = new HashSet<String>(strArray.length);
		for (String item : strArray) {
			set.add(item);
		}
		return set;
	}

	/**
	 * 把字符串切分为字LinkedHashSet集合对像同时会去掉重复值, 这个函数有问题，复杂字符串时会出错
	 * 
	 * @param str 要切分的字符串
	 * @return 切分后的LinkedHashSet集合
	 */
	public static LinkedHashSet<String> splitAsLinkedSet(String str) {
		return splitAsLinkedSet(str, ",");
	}

	/**
	 * 把字符串切分为字LinkedHashSet集合对像同时会去掉重复值, 这个函数有问题，复杂字符串时会出错
	 * 
	 * @param str 要切分的字符串
	 * @param key 关键字
	 * @return LinkedHashSet集合
	 */
	public static LinkedHashSet<String> splitAsLinkedSet(String str, String key) {
		String[] strArray = StringUtils.split(str, key);
		LinkedHashSet<String> set = new LinkedHashSet<String>(strArray.length);
		for (String item : strArray) {
			set.add(item);
		}
		return set;
	}

	/**
	 * 获得一个给定长度的随机字符串
	 * 
	 * @param length 随机字符串的长度
	 * @return 随机字符串
	 */
	public static String getRandom(int length) {
		String allChar = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			sb.append(allChar.charAt(random.nextInt(allChar.length())));
		}
		return sb.toString();
	}

	/**
	 * josn字符串转为hashmap对像,json字符串格式为{"fieldName1":"字段值","fdName2":"value2"}
	 * 
	 * @param jsonStr json字符串
	 * @return HashMap对象
	 */
	public static HashMap<String, String> jsonStr2Map(String jsonStr) {
		com.alibaba.fastjson.JSONObject jsonobj = com.alibaba.fastjson.JSON.parseObject(jsonStr);
		HashMap<String, String> map = new HashMap<String, String>(jsonobj.size());
		for (String fdName : jsonobj.keySet()) {
			map.put(fdName, jsonobj.getString(fdName));
		}
		return map;
	}

	/**
	 * 对字符串进行json格式的编码
	 * 
	 * @param str 要编码的json值
	 * @return 编码后的json字符串
	 */
	public static String encodeJson(String str) {
		//20181009 修改替换
		str = str.replace("\\", "\\\\");
	    str = str.replace("/", "\\/");
	    str = str.replace("\"", "\\\"");
	    str = str.replace("\n", "\\n");
	    str = str.replace("\r", "\\r");
	    str = str.replace("\b", "\\b");
	    str = str.replace("\f", "\\f");
	    str = str.replace("\t", "\\t");
	    return str;
	}

	/**
	 * 对字符串进行utf-8解码,只能解码utf-8格式的编码
	 * 
	 * @param str 要解码的字符串
	 * @return 解码后的字符串，若抛出异常，返回空字符串
	 */
	public static String decode(String str) {
		try {
			return java.net.URLDecoder.decode(str, "utf-8");
		} catch (Exception e) {
			//BeanCtx.log(e, "E", "字符串解码失败(" + str + ")");
			return "";
		}
	}

	/**
	 * 兼容解码方式，先判断是不是ISO-8859-1编码，否则以utf-8进行解码
	 * @param str 要解码的字符串
	 * @return 解码后的字符串，若抛出异常，返回空字符串
	 */
	public static String decodeAll(String str) {
		//20180428 修改解码方法，兼容Oracle/MySQL数据库，使用Tools.encode(fileName)进行解码
		try {
			if (str.equals(new String(str.getBytes("ISO-8859-1"), "ISO-8859-1"))) {
				str = Tools.decodeUrl(str);
			} else {
				str = Tools.decode(str);
			}
		} catch (UnsupportedEncodingException e1) {
			//BeanCtx.log(e1, "E", "字符串解码失败(" + str + ")");
			return "";
		}
		return str;
	}

	/**
	 * 对字符串进行utf-8编码
	 * 
	 * @param str 要编码的字符串
	 * @return 返回编码后的字符串
	 */
	public static String encode(String str) {
		try {
			String code = java.net.URLEncoder.encode(str, "utf-8");
			code = code.replace("+", "%20");
			return code;
		} catch (Exception e) {
			//BeanCtx.log(e, "E", "字符串编码失败(" + str + ")");
			return str;
		}
	}

	/**
	 * 把字符串编码为base64格式的
	 * 
	 * @param str 要编码的字符串
	 * @return 返回编码后的字符串
	 */
	public static String base64(String str) {
		if (Tools.isBlank(str))
			return "";
		try {
			return new String(org.apache.commons.codec.binary.Base64.encodeBase64(str.getBytes()), "UTF-8");
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 解码base64格式的字符串
	 * 
	 * @param str 要解码的字符串
	 * @return 返回解码后的字符串
	 */
	public static String unBase64(String str) {
		if (Tools.isBlank(str)) {
			return "";
		}
		try {
			return new String(org.apache.commons.codec.binary.Base64.decodeBase64(str.getBytes("UTF-8")));
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 判断字符串是否为null或"null"或长度为0
	 * 
	 * @param string 要判断的字符串
	 * @return 判断结果，true为空，false为非空
	 */
	private static boolean isEmpty(String string) {
		return (string == null) || (string.equals("null")) || (string.length() == 0);
	}

	/**
	 * 判断字符串是否为空值或去掉前后空格长度为0
	 * 
	 * @param string 要判断的字符串
	 * @return 判断结果，true为空，false为非空
	 */
	public static boolean isBlank(String string) {
		return (isEmpty(string)) || (string.trim().length() == 0);
	}

	/**
	 * 判断字符串是否不为空值
	 * 
	 * @param string 要判断的字符串
	 * @return 判断结果，true为非空，false为空
	 */
	public static boolean isNotBlank(String string) {
		return !isBlank(string);
	}

	/**
	 * 判断字符串是否由字母数字和下划线组成
	 * 
	 * @param str 要判断的字符串
	 * @return 返回true表示成立，false表示不成立
	 */
	public static Boolean isString(String str) {
		if (Tools.isBlank(str)) {
			return true;
		}
		Boolean bl = false;
		// 首先,使用Pattern解释要使用的正则表达式，其中^表是字符串的开始，$表示字符串的结尾。
		Pattern pt = Pattern.compile("^[0-9a-zA-Z_.]+$");
		Matcher mt = pt.matcher(str);
		if (mt.matches()) {
			bl = true;
		}
		return bl;

	}

	/**
	 * 根据Document文档对像对字符串中的{}变量进行文档中的字段值替换
	 * 
	 * @param doc 数据文档
	 * @param str 带有 {字段名}的字符串
	 * @return 返回字符串
	 */
	public static String parserStrByDocument(Document doc, String str) {
		if (Tools.isBlank(str)) {
			return "";
		}
		str = str.replace("{Userid}", BeanCtx.getUserid());
		String startCode = "{";
		String endCode = "}";
		int spos = str.indexOf(startCode);
		if (spos == -1) {
			return str;
		} // 没有{符号直接返回
		StringBuilder newHtmlStr = new StringBuilder(str.length());
		while (spos != -1) {
			int epos = str.indexOf(endCode);
			String fdName = str.substring(spos + 1, epos);
			String lStr = str.substring(0, spos);
			str = str.substring(epos + 1, str.length());
			newHtmlStr.append(lStr);
			newHtmlStr.append(doc.g(fdName));
			spos = str.indexOf(startCode);
		}
		newHtmlStr.append(str);
		return newHtmlStr.toString();
	}

	/**
	 * 把标准的Xml字符串转换成为hashmap对像
	 * 
	 * @param xml xml格式的字符串{@code <Items><WFItem name=\"NewField\">linkey</WFItem><WFItem name=\"MeetingAddress\">新的&amp;会议室</WFItem></Items>}
	 * @return 返回字段名和字段值的map对像
	 */
	public static HashMap<String, String> xmlStr2Map(String xml) {
		HashMap<String, String> fdMap = new HashMap<String, String>();
		int max = 0;
		String startCode = "<WFItem name=\"";
		int spos = xml.indexOf(startCode);
		if (spos == -1) {
			return fdMap;
		}
		while (spos != -1) {
			max++;
			if (max > 10000) {
				break;
			}
			spos = spos + 14;
			int epos = xml.indexOf("\"", spos);
			String fdName = xml.substring(spos, epos);
			String endStr = xml.substring(epos + 1, epos + 3);
			if (!endStr.equals("/>")) {
				spos = xml.indexOf(">", epos);
				epos = xml.indexOf("</WFItem>");
				String fdValue = xml.substring(spos + 1, epos);
				if (fdValue.startsWith("<![CDATA[")) {
					fdValue = fdValue.substring(9, fdValue.length() - 3);
				}
				xml = xml.substring(epos + 9);
				fdMap.put(fdName, fdValue);
			} else {
				fdMap.put(fdName, ""); // 加入一个空值字段,这样doc.hasitem()时才会生效，不然动态表格中判断不准确
				xml = xml.substring(epos + 3);
			}
			spos = xml.indexOf(startCode);
		}
		return fdMap;
	}

	/**
	 * 解码url中传的中文字符串,url中的中文字符串必须要用utf-8格式进行编码 js可以用encodeURIComponent()函数，java可以用Tools.encode()方法
	 * 
	 * @param qry 要解码的url
	 * @return 返回解码后的字符串
	 */
	public static String decodeUrl(String qry) {
		try {
			qry = new String(qry.getBytes("ISO-8859-1"), "UTF-8");
		} catch (Exception e) {
			//BeanCtx.log(e, "E", "Query string decode error");
		}
		return qry;
	}

	/**
	 * 格式化成标准的json响应字符串
	 * 
	 * @param status 状态：ok error ....
	 * @param msg 要返回的提示消息
	 * @return 格式化后的json字符串
	 */
	public static String jmsg(String status, String msg) {
		return "{\"Status\":\"" + status + "\",\"msg\":\"" + msg + "\"}";
	}

	/**
	 * 异常信息转为字符串
	 * 
	 * @param e 异常对像
	 * @return 返回字符串
	 */
	public static String getErrorMsgFromException(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			return sw.toString();
		} catch (Exception e2) {
			return "错误的异常对像";
		}
	}

	/**
	 * 从wf_num中获得应用编号
	 * 
	 * @param wf_num 设计元素的编号
	 * @return 返回应用编号appid
	 */
	public static String getAppidFromElNum(String wf_num) {
		int spos = wf_num.indexOf("_");
		String appid = "";
		if (spos != -1) {
			appid = wf_num.substring(spos + 1); // 获得应用appid
			spos = appid.indexOf("_");
			if (spos != -1) {
				appid = appid.substring(0, spos);
			}
		}
		return appid;
	}

	/**
	 * md5加密函数
	 * 
	 * @param str 要加密的字符串
	 * @return 加密后的字符串
	 */
	public static String md5(String str) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					buf.append("0");
				}
				buf.append(Integer.toHexString(i));
			}
			re_md5 = buf.toString();
		} catch (Exception e) {
			//BeanCtx.log(e, "E", "md5加密错误(" + str + ")");
		}
		return re_md5;
	}

	/**
	 * 把InputStream转换为字符串
	 * 
	 * @param is InputStream对像
	 * @param charset 如：UTF-8等
	 * @return 从流中得到的字符串
	 * @throws Exception 输入流异常
	 */
	public static String streamToString(InputStream is, String charset) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 获取流程单号（新增加
	 * @return yyMMddHHmmss+三位数字随机数
	 */
	public static String getNewSerialNo(){
		String allChar = "0123456789";
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			sb.append(allChar.charAt(random.nextInt(allChar.length())));
		}
		return DateUtil.getDateTimeNum().substring(2) + sb.toString();
	}


	/**
	 * 根据节点id获得节点所在数据库表名
	 *
	 * @param processid 流程ID
	 * @param nodeid 节点ID
	 * @return 数据库表名，例如人工活动存储表，网关存储表，事件存储表等
	 */
	public static String getNodeTableName(String processid, String nodeid) {
		String nodeType = getNodeType(processid, nodeid);
		if (Tools.isBlank(nodeType)) {
			return "";
		}
		return "BPM_Mod" + nodeType + "List";
	}


	/**
	 * 根据节点id获得节点的基本类型名称
	 *
	 * @param processid 流程ID
	 * @param nodeid 节点ID
	 * @return 返回节点类型，如Task，SequenceFlow，Gateway，Process，Event等
	 */
	public static String getNodeType(String processid, String nodeid) {
		// 根据节点首字母得到，速度最快
		if (Tools.isBlank(nodeid)) {
			return "";
		}
		String nodeType = nodeid.substring(0, 1);
		if (nodeType.equals("T")) {
			nodeType = "Task"; // 任务
		}
		else if (nodeType.equals("R")) {
			nodeType = "SequenceFlow"; // 路由
		}
		else if (nodeType.equals("G")) {
			nodeType = "Gateway"; // 网关
		}
		else if (nodeType.equals("P")) {
			nodeType = "Process"; // 流程
		}
		else if (nodeType.equals("E")) {
			nodeType = "Event"; // 事件
		}
		else if (nodeType.equals("S")) {
			nodeType = "SubProcess"; // 子流程
		}
		else {
			return "";
		}
		return nodeType;

		// 从视图中去得到，速度慢一点
		/*
		 * String sql="select NodeType from BPM_AllModNodeList where Processid='"+processid+"' and Nodeid='"+nodeid+"'"; String nodeType=Rdb.getValueTopOneBySql(sql); return nodeType;
		 */
	}

}
