package cn.linkey.orm.util;

import org.apache.commons.lang3.StringUtils;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * 工具类
 * 
 * @author Administrator
 * 
 */
public class Tools {

	/**
	 * 字符串数组转字符串
	 * 
	 * @param array 数组对像
	 * @param key 分隔字符串
	 * @return 返回字符串
	 */
	public static String join(String[] array, String key) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			if (i == 0) {
				str.append(array[i]);
			} else {
				str.append(key + array[i]);
			}
		}
		return str.toString();
	}
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
	 * 获得现在的时间
	 *
	 * @return 返回标准的如：2013-01-03 12:09
	 */
	public static String getNow() {
		String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
		java.text.DateFormat insDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
		return (String) insDateFormat.format(new Date());
	}
}
