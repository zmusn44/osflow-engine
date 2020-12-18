package cn.linkey.workflow.factory;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;
import cn.linkey.orm.dao.Rdb;
import cn.linkey.orm.doc.Document;
import cn.linkey.orm.doc.impl.DocumentImpl;
import cn.linkey.rule.factory.BeanServer;
import cn.linkey.rule.rule.EventEngine;
import cn.linkey.rule.rule.ExecuteEngine;
import cn.linkey.workflow.util.Tools;
import cn.linkey.workflow.wf.ProcessEngine;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 本类为系统的容器类，所有对像应使用本类来进行创建和获取，而不要单独使用new来创建
 *
 * <p>本类主要功能为根据请求创建的类来判断容器中是否已经存在实例对像，<br>
 * 如果已经存在就直接返回对像实例 如果不存在则调用LinkeyObj去创建一个出来系统核心类进行缓存。
 * <p>而对于规则和用户自定义的类则不进行缓存 BeanCtx.init("admin",null,null);<br>
 * 这个需要在过虑器中进行初始化BeanCtx.close(); 本类为静态单例类。
 */
final public class BeanCtx {
	private static ThreadLocal<ThreadContext> context = new ThreadLocal<ThreadContext>(); // 线程全局对像,通过get
	private static Logger logger = LoggerFactory.getLogger(BeanCtx.class);

	/**
	 * 获得用户语言环境
	 * 这里 默认只支持 zh,CN
	 *
	 * @return 返回一个Locale对象
	 */
	public static Locale getUserLocale() {
		// zh,CN ,这里只
		Locale userLocale = new Locale("zh", "CN");
		return userLocale;
	}

	public static void removeContext() {
		context.remove();
	}

	/**
	 * 获得线程级别的变量对像
	 *
	 * @return 返回一个ThreadContext对象
	 */
	public static ThreadContext getContext() {
		// 初始化线程对像
		ThreadContext insThreadContext = context.get();
		if (insThreadContext == null) {
			//logger.info("不存在，初始化 ThreadContext ");
			insThreadContext = new ThreadContext();
			context.set(insThreadContext);
		} else {
			//logger.info("存在，ThreadContext");
		}
		return insThreadContext;
	}

	/**
	 * 设置线程级别的变量对像
	 *
	 * @param obj 传入一个ThreadContext对象
	 */
	public static void setContext(ThreadContext obj) {
		context.set(obj);
	}

	/**
	 * 设置全局的数据库链接对像
	 *
	 * @param conn 传入一个Connection对象
	 */
	public static void setConnection(Connection conn) {
		getContext().setConnection(conn);
	}

	public static void setRdb(Rdb rdb) {
		getContext().setRdb(rdb);
	}

	public static Rdb getRdb() {
		return getContext().getRdb();
	}

	/**
	 * 获得当前登录的用户id
	 *
	 * @return 返回当前登录的用户id
	 */
	public static String getUserid() {
		return getContext().getUserid();
	}

	/**
	 * 从用户和部门的组合字符串中分出用户id
	 *
	 * @param userStr 格式为userid#deptid 如果有多个请用逗号分隔
	 * @return 返回用户id
	 */
	public static String getUseridByMulStr(String userStr) {
		StringBuilder userList = new StringBuilder();
		String[] userArray = Tools.split(userStr);
		for (String userItem : userArray) {
			int spos = userItem.indexOf("#");
			String userid;
			if (spos != -1) {
				userid = userItem.substring(0, spos);
			} else {
				userid = userItem;
			}
			if (userList.length() > 0) {
				userList.append(",");
			}
			userList.append(userid);
		}
		return userList.toString();
	}

	/**
	 * 从用户和部门的组合字符串中分出部门id
	 *
	 * @param userStr 格式为userid#deptid 如果有多个请用逗号分隔
	 * @return 返回用户与部门的map对像
	 */
	public static HashMap<String, String> getDeptidByMulStr(String userStr) {
		HashMap<String, String> deptSet = new HashMap<String, String>();
		String[] deptArray = Tools.split(userStr);
		for (String deptItem : deptArray) {
			int spos = deptItem.indexOf("#");
			String deptid, userid;
			if (spos != -1) {
				deptid = deptItem.substring(spos + 1);
				userid = deptItem.substring(0, spos);
			} else {
				deptid = "";
				userid = deptItem;
			}
			deptSet.put(userid, deptid);
		}
		return deptSet;
	}

	/**
	 * 设置当前的用户id
	 *
	 * @param userid 用户id
	 * @return String
	 */
	public static String setUserid(String userid) {
		return getContext().setUserid(userid);
	}

	/**
	 * 获得当前登录的用户中文名称
	 *
	 * @return 返回当前登录的用户中文名称
	 */
	public static String getUserName() {
		return getContext().getUserName();
	}

	public static String setUserName(String userName) {
		return getContext().setUserName(userName);
	}

	/**
	 * 获得缺省的工作流引擎对像
	 *
	 * @return 返回ProcessEngine对象
	 */
	public static ProcessEngine getDefaultEngine() {
		if (getContext().getLinkeywf() == null) {
			getContext().setLinkeywf(new ProcessEngine());
			return getContext().getLinkeywf();
		} else {
			return getContext().getLinkeywf();
		}
	}

	/**
	 * 获得当前工作流引擎对像
	 *
	 * @return 返回当前工作流引擎对像
	 */
	public static ProcessEngine getLinkeywf() {
		return getContext().getLinkeywf();
	}

	/**
	 * 设置当前工作流引擎对像
	 *
	 * @param linkeywf 工作流引擎对像
	 */
	public static void setLinkeywf(ProcessEngine linkeywf) {
		getContext().setLinkeywf(linkeywf);
	}

	/**
	 * 获得回滚标记
	 *
	 * @return true表示需要回滚, false表示不需要
	 */
	public static boolean isRollBack() {
		return getContext().isRollBack();
	}

	/**
	 * 设置回滚标记
	 *
	 * @param rollBack true表示需要回滚，false表示不需要
	 */
	public static void setRollback(boolean rollBack) {
		// log("D", "数据库链接被设置为需要回滚!");
		getContext().setRollback(rollBack);
	}

	/**
	 * 设置存盘时需要对文档的内容进行编码,默认就是编码的无需设置
	 */
	public static void setDocEncode() {
		BeanCtx.setCtxData("WF_NoEncode", "0");
	}

	/**
	 * 设置存盘时不对文档的内容进行编码
	 */
	public static void setDocNotEncode() {
		BeanCtx.setCtxData("WF_NoEncode", "1");
	}

	/**
	 * 获取当前文档是否编码的状状态
	 *
	 * @return 返回true表示文档存盘时需要编码 返回false表示文档存盘时不进行编码
	 */
	public static boolean getEnCodeStatus() {
		String encode = (String) BeanCtx.getCtxData("WF_NoEncode");
		if (encode != null && encode.equals("1")) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 获取全局变量对像,返回Object对像
	 *
	 * @param key key
	 * @return Object对像
	 */
	public static Object getCtxData(String key) {
		return getContext().getCtxData(key);
	}

	/**
	 * 获取全局变量对像，返回字符串
	 *
	 * @param key key
	 * @return 字符串
	 */
	public static String getCtxDataStr(String key) {
		return getContext().getCtxDataStr(key);
	}

	/**
	 * 设置全局线程变量对像
	 *
	 * @param key key
	 * @param obj obj
	 */
	public static void setCtxData(String key, Object obj) {
		getContext().setCtxData(key, obj);
	}

	/**
	 * 获得Document对像专用方法
	 *
	 * @param tableName 数据库表名
	 * @return 返回一个Document对象
	 */
	public static Document getDocumentBean(String tableName) {
		return new DocumentImpl(tableName);
	}

	/**
	 * 获得多语言消息
	 *
	 * @param lang   语言包的名称
	 * @param key    语言包中配置的关键字
	 * @param params 要格式化消息的字符串参数
	 * @return 返回字符串
	 */
	public static String getMsg(String lang, String key, Object... params) {
		try {
			ResourceBundle messages = ResourceBundle.getBundle("cn.linkey.workflow.lang." + lang, getUserLocale());
			return MessageFormat.format(messages.getString(key), params);
		} catch (Exception e) {
			BeanCtx.log("W", "请确认语言包(" + "cn.linkey.workflow.lang." + lang + ")和关键字(" + key + ")是否存在!");
			return key;
		}
	}

	/**
	 * 获得当前用户所在语言环境的国家关键字
	 *
	 * @return 中文返回CN, 英文返回US
	 */
	public static String getCountry() {
		return getUserLocale().getCountry();
	}

	/**
	 * 获得事件引擎对像，用来触发和执行事件
	 *
	 * @return 事件引擎对像
	 */
	public static EventEngine getEventEngine() {
		return (EventEngine) BeanCtx.getBean("EventEngine");
	}

	/**
	 * 获得执行引擎对像,用来执行规则
	 *
	 * @return 执行引擎对像
	 */
	public static ExecuteEngine getExecuteEngine() {
		return (ExecuteEngine) BeanCtx.getBean("ExecuteEngine");
	}

	/**
	 * 获得脚本引擎对像,可以执行路由条件
	 *
	 * @return ScriptEngine
	 *//*
	public static ScriptEngine getScriptEngine() {
		return (ScriptEngine) BeanCtx.getBean("ScriptEngine");
	}
	*/

	/**
	 * 获得系统通用配置的参数
	 *
	 * @param configid configid
	 * @return 返回配置值字符串
	 */
	public static String getSystemConfig(String configid) {
		String configValue = "";
		// 20200922，这里已不支持表单，不可能出现获取这些id的配置值的
		String configids = "AppFormHtmlHeader,AppGridHtmlHeader,AppPageHtmlHeader,AppPageHtmlHeader_theme,ProcessFormHtmlHeader,DesignerHtmlHeader,UI_theme";
		if (configids.contains(configid)) {
			//20180904 添加为空判断，Oracle为空时返回null字符
			if (Tools.isNotBlank(getSystemConfig2(configid))) {
				configValue = getSystemConfig2(configid);
			}
		} else {
			//获取获得系统通用配置的参数实际在这里，上面是对主题切换的特殊处理
			configValue = getSystemConfig2(configid);
		}
		return configValue;
	}

	/**
	 * @Description: 获得系统通用配置的参数
	 * @param: configid
	 * @return：返回配置值字符串
	 * @author: alibao
	 * @date: 2018年7月18日 下午4:15:48
	 */
	private static String getSystemConfig2(String configid) {
		String configValue = "";
		if (Tools.isBlank(configValue) && Tools.isNotBlank(configid)) {
			configValue = BeanCtx.getRdb()
					.getValueBySql("select ConfigValue from BPM_SystemConfig where Configid='" + configid + "'"); // 直接到sql表中找
		}
		return configValue;
	}

	/**
	 * 根据类名返回实例对像，需要强制类型转换
	 *
	 * @param beanid 实例ID
	 * @return Object对象
	 */
	public static Object getBean(String beanid) {
		HashMap<String, String> configMap = BeanConfig.getClassPath(beanid);
		String className = configMap.get("classPath");
		String singleton = configMap.get("singleton"); // 1表示单例模式需要从缓存中查找,0表示多实例模式，每次创建即可
		configMap = null;
		// p("beanid="+beanid+"--classname="+className+"---singleton="+singleton);
		if (className.startsWith("cn.linkey.rulelib.")) {
			// 从规则中获取对像
			int spos = className.lastIndexOf(".");
			String ruleNum = className.substring(spos + 1);
			return getBeanByRuleNum(ruleNum);
		} else {
			return getBeanByClassName(beanid, className, singleton);
		}
	}

	/**
	 * 直接根据类id,路径返、实例模式来返回对像
	 *
	 * @param beanid
	 * @param className
	 * @param singleton
	 * @return Object
	 */
	private static Object getBeanByClassName(String beanid, String className, String singleton) {
		Object obj = null;
		try {
			if (singleton.equals("0")) { // 多例模式
				Class cls = Class.forName(className);
				obj = cls.newInstance();
			} else {
				// singleton=1单例模式
				// 说明池中还没有，创建一个新实例并放入其中
				Class cls = Class.forName(className);
				obj = cls.newInstance();
			}
			return obj;
		} catch (Exception e) {
			if (!beanid.equals("InsNode")) {
				log(e, "E", "BeanCtx获得id为" + beanid + "的bean失败,请检查(" + className + ")是否存在!");
			} else {
				log("E", "获取流程解析器时出现异常,请重新清空系统缓存后再重试,如有问题请联系系统维护人员!");
			}
			return null;
		}
	}

	/**
	 * 根据规则编号返回规则文档对像
	 *
	 * @param ruleNum 规则编号
	 * @return 返回规则对像
	 */
	public static Object getBeanByRuleNum(String ruleNum) {
		// 调用规则引擎来解析规则并执行
		return BeanServer.getBeanByRuleNum(ruleNum);
	}

	/**
	 * 获得主文档字段和值的键值对
	 *
	 * @return mainData JSONObject key=字段，val=字段值
	 */
	public static JSONObject getMainData() {
		return getContext().getMainData();
	}

	/**
	 * 设置主文档字段和值的键值对
	 *
	 * @param mainData JSONObject key=字段，val=字段值
	 */
	public static void setMainData(JSONObject mainData) {
		getContext().setMainData(mainData);
	}

	/**
	 * 获取应用ID
	 *
	 * @return APPID 应用ID
	 */
	public static String getAppid() {
		return getContext().getAppid();
	}

	/**
	 * 设置AppId
	 *
	 * @param appid 应用ID
	 */
	public static void setAppid(String appid) {
		getContext().setAppid(appid);
	}

	/**
	 * @return 返回文档标识Id
	 */
	public static String getWfnum() {
		return getContext().getWfnum();
	}

	/**
	 * @param wf_num 设置文档标识Id
	 */
	public static void setWfnum(String wf_num) {
		getContext().setWfnum(wf_num);
	}

	/**
	 * 记录系统日记,统一的log处理函数
	 *
	 * @param e     为异常对像
	 * @param level 错误级别 level分为5个等级 D-DEBUG、I-INFO、W-WARN、E-ERROR、F-FATA
	 * @param msg   消息内容
	 */
	public static void log(Exception e, String level, String msg) {
		String errorMsg = Tools.getErrorMsgFromException(e);
		writeSystemLog(level, errorMsg + "\n" + msg);
	}

	/**
	 * 记录系统日记,统一的log处理函数
	 *
	 * @param level 错误级别 level分为5个等级 D-DEBUG、I-INFO、W-WARN、E-ERROR、F-FATA
	 * @param msg   消息内容
	 */
	public static void log(String level, String msg) {
		writeSystemLog(level, msg);
	}

	/**
	 * 写入系统日记到数据库中去 SystemLogWriteType= 1只输出到控制台中 2输出到控制台和log文件中 3只输出到log文件中
	 * PrintDebugLog=0不输出BeanCtx.out();中debug级别的调试信息,其他表示输出
	 *
	 * @param logLevel 错误级别 level分为5个等级 D-DEBUG、I-INFO、W-WARN、E-ERROR、F-FATA
	 * @param msg      消息内容
	 */
	private static void writeSystemLog(String logLevel, String msg) {

	}

	/**
	 * 记录系统日记,统一的log处理函数（新增）
	 *
	 * @param level 错误级别 level分为5个等级 D-DEBUG、I-INFO、W-WARN、E-ERROR、F-FATA
	 * @param className 类名
	 * @param msg 消息内容
	 */
	public static void log(String level, String className, String msg) {
		writeSystemLog(level, className, msg);
	}

	/**
	 * 写入系统日记到数据库中去 SystemLogWriteType= 1只输出到控制台中 2输出到控制台和log文件中 3只输出到log文件中
	 * PrintDebugLog=0不输出BeanCtx.out();中debug级别的调试信息,其他表示输出（新增）
	 *
	 * @param logLevel 错误级别 level分为5个等级 D-DEBUG、I-INFO、W-WARN、E-ERROR、F-FATA
	 * @param msg 消息内容
	 */
	private static void writeSystemLog(String logLevel, String className, String msg) {
		if("I".equalsIgnoreCase(logLevel) || "INFO".equalsIgnoreCase(logLevel) || "I-INFO".equalsIgnoreCase(logLevel)){
			logger.info(className + "-->" + msg);
		} else if("W".equalsIgnoreCase(logLevel) || "WARN".equalsIgnoreCase(logLevel) || "W-WARN".equalsIgnoreCase(logLevel)){
			logger.warn(className + "-->" + msg);
		} else if("E".equalsIgnoreCase(logLevel) || "ERROR".equalsIgnoreCase(logLevel) || "E-ERROR".equalsIgnoreCase(logLevel)){
			logger.error(className + "-->" + msg);
		} else if("D".equalsIgnoreCase(logLevel) || "DEBUG".equalsIgnoreCase(logLevel) || "D-DEBUG".equalsIgnoreCase(logLevel)) {
			logger.debug(className + "-->" + msg);
		} else {
			logger.info(logLevel + "." + className + "-->" + msg);
		}
	}

	/**
	 * 输出字符串到控制台
	 *
	 * @param msg 输出到控制台的信息
	 */
	public static void out(Object msg) {
		if (msg != null) {
			writeSystemLog("D", msg.toString());
		} else {
			System.out.println("BeanCtx.out()不能输出对像为null的日记信息!");
		}
	}
}
