package cn.linkey.workflow.factory;

import java.sql.Connection;
import java.util.HashMap;
import cn.linkey.orm.dao.Rdb;
import cn.linkey.workflow.wf.ProcessEngine;
import com.alibaba.fastjson.JSONObject;

/**
 * 线程线别的共享对像类,本类为多例类，每一个http线程都应该持有一个本类的实例对像 本类不进行数据的初始化，初始化在BeanCtx中完成
 *
 * @author Administrator 本类为多实例类
 */

public class ThreadContext {
    private String userid; // 用户英文id
    private String userName;// 用户中文名
    private Connection conn; // 数据库链接对像
    private Rdb rdb; // 数据库链接对像
    private ProcessEngine linkeywf; // 全局引擎对像
    private boolean rollback; // true表示整个线程需要回滚,false表示提交
    private HashMap<String, Object> ctxMap = new HashMap<String, Object>(); // 设置一个全局的交换变量,可以在线程级别中进行使用和跨Bean交换数据
    private String appid;// 当前访问设计所属的应用id,如果是访问流程引擎中的设计则为流程所属应用
    private String wfnum; // 当前设计的编号
    private JSONObject mainData;//主文档字段和值的键值对

    /**
     * 获得当前工作流引擎对像
     *
     * @return 返回工作流引擎对像
     */
    protected ProcessEngine getLinkeywf() {
        return linkeywf;
    }

    /**
     * 设置当前工作流引擎对像
     *
     * @param linkeywf 工作流引擎对像
     */
    protected void setLinkeywf(ProcessEngine linkeywf) {
        this.linkeywf = linkeywf;
    }

    /**
     * 获得线程级别的数据库链接对像
     *
     * @return 返回数据库连接对象
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * 设置线程级别的数据库链接对像
     *
     * @param conn 设置数据库连接
     */
    protected void setConnection(Connection conn) {
        this.conn = conn;
    }


    public Rdb getRdb() {
        return rdb;
    }
    public void setRdb(Rdb rdb) {
        this.rdb = rdb;
    }

    /**
     * 获得当前用户的userid
     *
     * @return 返回当前用户userid
     */
    protected String getUserid() {
        return userid;
    }

    /**
     * 设置线程用户ID
     * @param userid 用户ID
     * @return 用户ID
     */
    protected String setUserid(String userid) {
        return this.userid = userid;
    }

    /**
     * 获得当前登录用户的中文名
     *
     * @return 用户中文名
     */
    protected String getUserName() {
        if (userName == null) {
            return getUserid();
        }
        else {
            return userName;
        }
    }

    /**
     * 设置线程用户中文名字
     * @param userName 用户中文名
     * @return 用户中文名
     */
    protected String setUserName(String userName) {
        return this.userName = userName;
    }

    /**
     * 获取全局变量对像，按照Object进行返回
     * @param key 存储变量名
     * @return 返回对象
     */
    protected Object getCtxData(String key) {
        return ctxMap.get(key);
    }

    /**
     * 返回全局变量对像按字符串返回
     * 底层封装HashMap存储
     * @param key 全局线程变量Key
     * @return 返回变量所对应的Value
     */
    protected String getCtxDataStr(String key) {
        Object obj = ctxMap.get(key);
        if (obj == null) {
            return "";
        }
        else {
            return (String) obj;
        }
    }

    /**
     * 设置全局变量对像
     * @param key 变量key
     * @param obj 存储的对象值
     */
    protected void setCtxData(String key, Object obj) {
        this.ctxMap.put(key, obj);
    }

    /**
     * 是否需要回滚
     *
     * @return true表示是需要回滚
     */
    protected boolean isRollBack() {
        return rollback;
    }

    /**
     * 设置回滚标记
     *
     * @param rollBack 是否回滚
     */
    protected void setRollback(boolean rollBack) {
        this.rollback = rollBack;
    }

    /**
     * 获取主表单数据
     * @return 主表单JSON
     */
    protected JSONObject getMainData() {
        return mainData;
    }

    /**
     * 设置主表单数据
     * @param mainData 主表单数据JSON
     */
    protected void setMainData(JSONObject mainData) {
        this.mainData = mainData;
    }

    protected String getAppid() {
        return appid;
    }

    protected void setAppid(String appid) {
        this.appid = appid;
    }

    protected String getWfnum() {
        return wfnum;
    }

    protected void setWfnum(String wf_num) {
        this.wfnum = wf_num;
    }

    protected void close() {
        Connection conn = getConnection();
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            else {
                BeanCtx.out("TE1.0 "+BeanCtx.getWfnum()+"=ThreadContext.close()链接不存在="+conn);
            }
        }
        catch (Exception e) {
            System.out.println("ThreadContext.close()数据库链接关闭出错!");
            e.printStackTrace();
            BeanCtx.log(e, "E", "Context级别的数据库链接关闭出错!");
        }
        this.conn = null;
        if (linkeywf != null) {
            this.linkeywf.clear();
        }
        if (this.ctxMap != null) {
            this.ctxMap.clear();
        }
    }

}
