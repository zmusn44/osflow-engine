package cn.linkey.rule.rule;

import cn.linkey.rule.factory.BeanServer;
import org.apache.commons.lang3.StringUtils;
import java.util.HashMap;

/**
 * 执行规则引擎
 */
public class ExecuteEngine {
    /**
     * 执行一个规则
     * @param ruleNum 规则编号（即类名）
            * @param params 执行规则时需传入的参数
     * @return 返回字符串
     * @throws Exception 异常抛出
     */
    public static String run(String ruleNum, HashMap<String, Object> params) throws Exception {
        if (StringUtils.isEmpty(ruleNum)) {
            throw new Exception("ExecuteEngine不能运行空规则,传入的规则编号为空值!");
        }
        // 执行规则
        String ruleMsg = "";
        LinkeyRule insLinkeyRule = (LinkeyRule) BeanServer.getBeanByRuleNum(ruleNum);
        if (insLinkeyRule != null) {
            ruleMsg = insLinkeyRule.run(params);
        }
        return ruleMsg;
    }

    /**
     * 执行一个规则
     * 根据ruleNum，找到对应的Java类，然后解析出javaCode和classPath
     * 此方法适用于源码开发
     * @param ruleNum 规则编号（即类名）
     * @return 字符串
     * @throws Exception 异常抛出
     */
    public static String run(String ruleNum) throws Exception {
        return run(ruleNum, new HashMap<String, Object>());
    }

    /**
     * 执行一个规则
     * 传入String格式的Java源码和类路径
     * @param javaCode java源码
     * @param classPath Java类路径
     * @return 规则执行结果
     * @throws Exception 异常抛出
     */
    public static String run(String javaCode, String classPath) throws Exception {
        String ruleMsg = "";
        LinkeyRule insLinkeyRule = (LinkeyRule) BeanServer.getBeanByCode(javaCode,classPath);
        if (insLinkeyRule != null) {
            ruleMsg = insLinkeyRule.run(new HashMap<String, Object>());
        }
        return ruleMsg;
    }

    /**
     * 执行一个规则
     * @param ruleNum 规则编号（即类名）
     * @param classPath 规则编译生成的classpath路径  如：cn.linkey.factory.ExecuteEngine
     * @param CompileFlag 是否在内存中实时编译运行 0表示每次编译，1表示使用已经编译好的类文件
     * @param SysDeveloperMode 1表示开发者模式可实时加载规则
     * @return 规则执行结果
     * @throws Exception 异常抛出
     */
    public static String  run(String ruleNum, String classPath, String CompileFlag, String SysDeveloperMode) throws Exception {
        if (StringUtils.isEmpty(ruleNum)) {
            throw new Exception("ExecuteEngine不能运行空规则,传入的规则编号为空值!");
        }
        String ruleMsg = "";
        LinkeyRule insLinkeyRule = (LinkeyRule) BeanServer.getBeanByRuleNum(ruleNum,classPath,CompileFlag,SysDeveloperMode);
        if (insLinkeyRule != null) {
            ruleMsg = insLinkeyRule.run(new HashMap<String, Object>());
        }
        return ruleMsg;

    }

    /**
     * 执行一个规则
     * 从数据库表中获取规则对象并执行
     * @param ruleNum 规则编号
     * @param tableName 指定数据库表
     * @param isTable 预留参数[暂无用]
     * @return 处理结果
     * @throws Exception 抛出异常
     */
    public static String run(String ruleNum,String tableName,boolean isTable) throws Exception {
        if (StringUtils.isEmpty(ruleNum)) {
            throw new Exception("ExecuteEngine不能运行空规则,传入的规则编号为空值!");
        }
        String ruleMsg = "";
        LinkeyRule insLinkeyRule = (LinkeyRule) BeanServer.getBeanFromTable(ruleNum,tableName);
        if (insLinkeyRule != null) {
            ruleMsg = insLinkeyRule.run(new HashMap<String, Object>());
        }
        return ruleMsg;
    }

    /**
     * 执行一个规则
     * clazz不为空，则通过反射机制，直接获取规则对象
     * clazz 为空，即源码开发状态，通过规则编号ruleNum获取规则对象
     * @param ruleNum 规则编号
     * @param clazzFile 类全限定名
     * @param params 规则参数
     * @return 处理结果
     * @throws Exception 抛出异常
     */
    public static String run(String ruleNum,String clazzFile, HashMap<String, Object> params) throws Exception {
        if (StringUtils.isEmpty(ruleNum)) {
            throw new Exception("ExecuteEngine不能运行空规则,传入的规则编号为空值!");
        }
        String ruleMsg = "";
        LinkeyRule insLinkeyRule = (LinkeyRule) BeanServer.getBeanByRuleNum(ruleNum,clazzFile);
        if (insLinkeyRule != null) {
            ruleMsg = insLinkeyRule.run(params);
        }
        return ruleMsg;

    }

}
