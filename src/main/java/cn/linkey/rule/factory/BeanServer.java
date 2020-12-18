package cn.linkey.rule.factory;

import cn.linkey.orm.dao.Rdb;
import cn.linkey.rule.rule.RuleConfig;
import cn.linkey.rule.util.JdbcUtil;
import cn.linkey.workflow.factory.BeanCtx;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class BeanServer {

	/**
	 * 根据规则编号返回规则文档对象
	 * 若clazz 为空，即从指定的路径中加载类
	 * @param ruleNum 规则编号
	 * @return 返回规则文档对象
	 */
	public static Object getBeanByRuleNum(String ruleNum) {
		// 要求所的的规则都在pakage=cn.linkey.rulelib的包和子包下
		String pakage = "cn.linkey.rulelib";
		// 类似 R_S003_B001，截取应用中间的应用编号
		String[] tmpArgs = ruleNum.split("_");
		if(tmpArgs.length > 0){
			pakage += "." + tmpArgs[1];
		}
		// 20200922 Mod & Add
		String clazz = pakage + "." + ruleNum;
		System.out.println("clazz="+clazz);
		// 这里应该调用一个可以做更多判断的函数，以支持更多的情况下获得规则数据的实例
		return getBeanByRuleNum(ruleNum, clazz);
	}

	/**
	 * 根据规则编号返回规则文档对象
	 * 若clazz不为空，则通过反射机制，直接获取规则对象
	 * 若clazz 为空，即源码开发状态，通过规则编号ruleNum获取规则对象
	 * @param ruleNum 规则编号
	 * @param clazz   类全限定名
	 * @return 返回规则文档对象
	 */
	public static Object getBeanByRuleNum(String ruleNum, String clazz) {
		Object obj = null;
		// 1.先从clazz类路径中加载
		if(StringUtils.isNotBlank(clazz)){
			obj = getBeanByClassName(clazz);
		}
		// 2.如果加载不到，再从数据库中找寻找规则列表匹配的规则文档对象
		if(obj == null) {
			// 2.1 这里先判断规则文档中的ClassPath类路径，看是否可以通过类路径能加载到
			RuleConfig ruleConfig = (RuleConfig) BeanCtx.getBean("RuleConfig");
			clazz = ruleConfig.getRuleDoc(ruleNum).g("ClassPath");
			if(StringUtils.isNotBlank(clazz)){
				clazz += "." + ruleNum;
				obj = getBeanByClassName(clazz);

				// 2.2 如果通过ClassPath类路径加载不到，表示未编译过，再从规则文档中的JavaCode去编译class文件再加载
				if(obj == null){
					obj = getBeanByRuleNum(ruleNum,clazz,"1","1");
				}
			}
		}
		// 3.如果还加载不到，最后从IDE 中的 Java 文件中去寻找
		if(obj == null) {
			String javaCode = getRuleCodeByRuleNum(ruleNum);
			String classPath = getFullClassName(javaCode);
			obj = getBeanByRuleNum(ruleNum,classPath,"1","1");
		}
		// 4.以上都加载不到，那就返回空吧
		return obj;
	}

	/**
	 * 类反射机制，根据类全限定名，实例模式返回对象
	 * @param className 如：cn.linkey.factory.BeanCtx
	 * @return Object
	 */
	public static Object getBeanByClassName(String className) {
		Object obj = null;
		try {
			Class cls = Class.forName(className);
			obj = cls.newInstance();
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("获取实例对象出现异常,请重新清空系统缓存后再重试,如有问题请联系系统维护人员!");
			return null;
		}
	}

	/**
	 * 获得规则对像
	 * @param ruleNum 规则编号
	 * @param classPath 规则编译生成的classpath路径  如：cn.linkey.factory.BeanCtx
	 * @param CompileFlag 是否在内存中实时编译运行 0表示每次编译，1表示使用已经编译好的类文件
	 * @param SysDeveloperMode 1表示开发者模式可实时加载规则
	 * @return 返回规则对象
	 */
	public static Object getBeanByRuleNum(String ruleNum, String classPath,String CompileFlag,String SysDeveloperMode) {
		Object obj = null;
		if (CompileFlag.equals("1")) {
			// 使用已经编译好的类文件classPath
			try {
				if (SysDeveloperMode.equals("1")) {
					// 说明是开发者模式每次都重新创建类并加重新加载
					String fullClassPath = FactoryRuleEngine.getCompilePath() + classPath.replace(".", "/") + ".class";
					File classFile = new File(fullClassPath);
					// 需要重新编译标记
					boolean reCompileFlag = false;
					if (!classFile.exists()) {
						reCompileFlag = true;
					}
					if (reCompileFlag == false) {
						// 不需要重新编译
						return FactoryRuleEngine.loadClassForDevModel(classPath);
					} else {
						// 编译一个新的类文件
						String ruleCode = getRuleCodeByRuleNum(ruleNum);
						return FactoryRuleEngine.javaCodeToObject(classPath, ruleCode, true);
					}
				} else {
					Class cls = Class.forName(classPath);
					obj = cls.newInstance();
					return obj;
				}
			} catch (Exception e) {

				System.out.println("BeanCtx.getBeanByRuleNum->规则转换为实例对像时出错,请检查类文件是否存在!(ruleNum=" + ruleNum
						+ ",classPath=" + classPath + ")!");
				return null;
			}
		} else if (CompileFlag.equals("0")) {
			// 每次实时编译运行
			String ruleCode = getRuleCodeByRuleNum(ruleNum);
			if (StringUtils.isBlank(ruleCode)) {
				System.out.println("(" +ruleNum + ")规则代码为空!");
				return null;
			} else {
				return FactoryRuleEngine.javaCodeToObject(classPath, ruleCode, false);
			}
		}
		return null;
	}

	/**
	 * 根据Java类名称查找Java文件，将Java代码转换成String字符串
	 * @param ruleNum 规则编号
	 * @return 返回Java代码的String字符串
	 */
	public static String getRuleCodeByRuleNum(String ruleNum){
		String path = getRuleCodePath(ruleNum);
		String ruleCode = "";
		File file = new File(path);
		try{
			FileInputStream in = new FileInputStream(file);
			int size = in.available();
			byte[] buffer = new byte[size];
			in.read(buffer);
			in.close();
			ruleCode = new String(buffer,"UTF-8");
		}catch (IOException e){
			e.printStackTrace();
		}
		return ruleCode;
	}

	/**
	 * 根据类名获取Java类绝对路径
	 * @param ruleNum 规则编号
	 * @return 返回类绝对路径
	 */
	public static String getRuleCodePath(String ruleNum){
		File proPath = new File(System.getProperty("user.dir"));
		List<String> list = new ArrayList<String>();
		scanJavaFile(proPath, ruleNum, list);
		if(list.size()>0){
			return list.get(0);
		}
		return "";
	}
	/**
	 * 递归扫描项目Java文件路径
	 * 符合条件的File路径放入list
	 * @param filePath 扫描的路径
	 * @param fileName fileName为需要获取的文件名,支持模糊查找
	 * @param list     list为获取到的符合条件的File路径
	 */
	public static void scanJavaFile(File filePath, String fileName, List<String> list) {
		File[] files = filePath.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isFile() && f.getName().indexOf(fileName) >= 0) {
					try {
						if (f.getName().contains("java")) {
							list.add(f.getCanonicalPath());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (f.isDirectory()) {
					scanJavaFile(f, fileName, list);
				}
			}
		}
	}

	/**
	 * 编译java代码
	 * @param javacode 源代码
	 * @param classPath 类路径与源代码中的class要一至 如：cn.linkey.rulelib.Synchron.R_Synchron_B001
	 * @param creatClass 是否生成类文件true表示是，false表示否
	 * @return 返回1表示成功否则返回错误消息
	 */
	public static String jmcode(String javacode, String classPath, boolean creatClass) {
		return FactoryRuleEngine.compileJavaCode(classPath, javacode, creatClass);
	}
	/**
	 * 直接通过JavaCode返回Java对像
	 * @param javaCode java代码字符串
	 * @param classPath  指定类路径
	 * @return 返回规则对象
	 */
	public static Object getBeanByCode(String javaCode, String classPath) {

		return FactoryRuleEngine.javaCodeToObject(classPath, javaCode, false);
	}

	/**
	 * 从Java源码中获取类的全名称
	 * @param javacode  Java源代码
	 * @return 返回类全称
	 */
	public static String getFullClassName(String javacode) {
		String className = "";
		Pattern pattern = Pattern.compile("package\\s+\\S+\\s*;");
		Matcher matcher = pattern.matcher(javacode);
		if (matcher.find()) {
			className = matcher.group().replaceFirst("package", "").replace(";", "").trim() + ".";
		}
		pattern = Pattern.compile("class\\s+\\S+\\s+\\{");

		matcher = pattern.matcher(javacode);
		if (matcher.find()) {
			className += matcher.group().replaceFirst("class", "").replace("{", "").trim();
		}else {
			pattern = Pattern.compile("class\\s+\\S+\\s+implements");
			matcher = pattern.matcher(javacode);
			if(matcher.find()){
				className += matcher.group().replaceFirst("class", "").replace("implements", "").trim();
			}

		}
		return className;
	}

	/**
	 * 从数据表中获取规则对象
	 * 若远程数据库表拿不到数据，则从默认数据库bpm_rulist中查找
	 * @param ruleNum 规则编号
	 * @param tableName 所在数据库表名称
	 * @return 返回规则对象
	 */
	public static Object getBeanFromTable(String ruleNum,String tableName){
		Object obj = null;Connection conn = null;ResultSet rs = null;
		String sql = "";
		try{
			//从存储引擎拿数据，返回一个Resultset
			rs = getRemoteResultSet(ruleNum,tableName);
			if(rs == null){
				sql = "select rulecode,classpath from "+ (StringUtils.isNotBlank(tableName)?tableName:"bpm_rulelist") +" WHERE rulenum ='"+ruleNum+"' ";
				rs = JdbcUtil.getResultSet(sql);
			}
			if(rs.next()){
				obj = getBeanByCode(rs.getString("rulecode"),rs.getString("classpath"));
			}
		}catch (Exception e) {
            System.out.println("获取指定表的规则代码时出错（"+sql+"）");
		}finally{
			JdbcUtil.close(conn,null,rs);
		}
		return obj;
	}

	/**
	 * 从远程数据库获取JavaCode
	 * @param ruleNum 规则编号
	 * @param tableName 数据库表
	 * @return 返回一个ResultSet
	 * @throws Exception 抛出异常
	 */
    public static ResultSet getRemoteResultSet(String ruleNum,String tableName) throws Exception {
		Rdb rdb = BeanCtx.getRdb();
		String sql = "select rulecode,classpath from "+ (StringUtils.isNotBlank(tableName)?tableName:"bpm_rulelist")
				+" WHERE rulenum ='"+ruleNum+"' ";
		return rdb.getResultSet(sql);
	}

}
