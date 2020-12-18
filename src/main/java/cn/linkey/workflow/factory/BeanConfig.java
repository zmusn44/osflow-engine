package cn.linkey.workflow.factory;

import cn.linkey.orm.dao.Rdb;
import cn.linkey.orm.doc.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;

/**
 * 本类主要根据beanid返回具体的类路径 <br>
 * 本类为单例静态类
 *
 * @author lch
 */
public class BeanConfig {

    //private   Rdb rdb = BeanCtx.getRdb();
    private static Logger logger = LoggerFactory.getLogger(BeanConfig.class);

    @SuppressWarnings("unchecked")
    public static HashMap<String, String> getClassPath(String beanid) {
        // 这里可能有大小写的问题 classPath,singleton在oracle数据库中时为大写
        HashMap<String, String> beancfg = new HashMap<String, String>();
        // 这里不再使用缓存，而是直接从数据库中进行查找
        String sql = "select classPath,singleton from BPM_BeanConfig where Beanid='" + beanid + "'";
        Rdb rdb = BeanCtx.getRdb();
        Document doc = rdb.getDocumentBySql(sql);
        beancfg.put("classPath", doc.g("classPath"));
        beancfg.put("singleton", doc.g("singleton"));
        if (beancfg == null || beancfg.get("classPath") == null || beancfg.size() == 0) {
            logger.error("在BPM_BeanConfig配置表中没有找到(" + beanid + ")的配置信息...");
        }
        return beancfg;
    }

}
