package org.example;

import cn.linkey.workflow.api.WorkFlow;
import cn.linkey.workflow.api.WorkFlowImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Mr.Yun
 * 流程引擎运转示例过程
 * 2020/09/24 09:50
 */
public class App {

    private String processid = null;
    private String docUnid = null;
    private String taskid = null;
    private String action = null;
    private String currentNodeid = null;
    private String nextNodeid = null;
    private String nextUserList = null;
    private String copyUserList = null;
    private String userid = null;
    private String remark = null;
    private String isBackFlag = null;
    private String reassignmentFlag = null;
    private JSONObject maindata = null;
    private Connection conn;

    public static void main(String[] args) {

        App app = new App();
        // 获取数据源
        Connection conn = app.getConn();

        // 打开一个流程（获取表单显示模板和业务数据接口），主要用于前端打开流程
//        app.openProcess("ff0c81f70938404056096810ecfa7baae53b","","bosimao");


        // 启动或提交一个流程
//        app.submitProcess();


        // 获取审批过程信息
//        app.getApprovalInfo();

        // 获取所有流程列表
//        app.getProcessMsg();


        System.out.println("\n\n\n========================所有用户待办=========================");
        // 获取用户待办列表
        app.showToDo("fupo");

        // 关闭数据源
        app.close();
    }

    /**
     * 获取用户待办
     *
     * @param userid 用户ID
     */
    public void showToDo(String userid) {

        WorkFlow workFlow = new WorkFlowImpl(conn);
        JSONArray TODOJSONArr = workFlow.getUserToDoInfo(userid);
        System.out.println(TODOJSONArr.toJSONString());
    }


    /**
     * 获取所有流程信息
     * 返回所有流程信息的JSONObject对象
     */
    public void getProcessMsg() {

        WorkFlow workFlow = new WorkFlowImpl(conn);
        JSONArray processMsg = workFlow.getProcessMsg();
        System.out.println(processMsg.toJSONString());
    }


    private Connection getConn() {
        Connection conn = null;
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://127.0.0.1:3306/flowchart";
        String username = "root";
        String password = "1234";
        try {
            //加载驱动
            Class.forName(driver);
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.conn = conn;
        return conn;
    }

    /**
     *
     * @param docUnid
     * @param processid
     * @param userid
     */
    private void openProcess(String docUnid, String processid, String userid) {
        WorkFlow workFlow = new WorkFlowImpl(conn);
        JSONObject object = workFlow.openProcess(docUnid, processid, userid);
        System.out.println(object.toJSONString());
    }

    private void getApprovalInfo() {
        WorkFlow workFlow = new WorkFlowImpl(conn);
        JSONObject object = workFlow.getApprovalInfo("8258008a02336043b00bf400e17b2b8a72da");
        System.out.println(object.toJSONString());
    }

    private void submitProcess() {
        WorkFlow workFlow = new WorkFlowImpl(conn);
        String processid = "433c77e90c28204ed90a0d400d27774580ac";
        String docUnid = ""; //流程文档ID
        String taskid = "";  // 用户任务ID【可选，多实例时则需要传】
        String action = "EndUserTask"; // 【提交动作】 GoToFirstNode、GoToOthers、EndUserTask、BackToDeliver、ReturnToAnyNode、
        // BackToReturnUser、GoToAnyNode、GoToPrevUser、GoToPrevNode、GoToArchived、GoToNextParallelUser、GoToNextNode
        String currentNodeid = ""; // 当前节点
        String nextNodeid = "T00002";   // 下一个节点
        String nextUserList = "fupo"; // 下一个审批处理人ID
        String copyUserList = "";  // 传阅用户ID
        String userid = "qionggui";
        String remark = "qg提交给fp";
        String isBackFlag = ""; // 标记为回退，当为回退任意环节时，isBackFlag值可以为2，表示回退后需要直接返回给回退者
        String reassignmentFlag = ""; // 转交时是否需要转交者返回的标记1表示不需要2表示需要
        JSONObject maindata = new JSONObject();

        // 表单数据
        maindata.put("name","lili");
        maindata.put("phone","7758258");
        maindata.put("sex","1");
        maindata.put("age","22");

        String msg = workFlow.runProcess(processid, docUnid, taskid, action, currentNodeid,
                nextNodeid, nextUserList, copyUserList, userid, remark, isBackFlag, reassignmentFlag, maindata);
        System.out.println("流程提交结果：" + msg);
    }


    private void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                conn = null;
                e.printStackTrace();
            }
        }
    }

}
