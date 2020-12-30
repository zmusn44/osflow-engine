# osflow-engine

#### 介绍
独立的流程引擎jar，开箱即用。

流程定义工具：[osflow-designer](https://gitee.com/openEA/osflow-designer)


#### 使用教程

**1、开发环境**

(1)   IDEA 开发环境

(2)   MySQL数据库(推荐5.7)

(3)   Tomcat容器(推荐8.0)

(4)   Maven仓库环境

**2、使用步骤**

1、导入flowchart.sql文件到MySQL数据库中，导入方法：

```shell
#创建数据库flowchart
 CREATE DATABASE flowchart DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
#还原数据库
 mysql -u root -p flowchart < E:\flowchart.sql
```

2、“osflow-engine-1.0.jar”已经上传到Maven中央仓库，最新版本可将repository文件夹中的"cn"文件夹复制到Maven仓库根目录。

```xml
<dependency>
    <groupId>cn.linkey</groupId>
    <artifactId>osflow-engine</artifactId>
    <version>1.0</version>
</dependency>


===========================================
# 如果集成项目中已经采用了slf4j包，则需在引入时排除
<!-- 流程引擎jar -->
<dependency>
    <groupId>cn.linkey</groupId>
    <artifactId>osflow-engine</artifactId>
    <version>1.0</version>
    <!--排除这个slf4j-log4j12-->
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

注：maven镜像仓库地址，建议使用阿里云仓库，速度很快。

```
国外官网地址(慢)：
https://repo1.maven.org/maven2/
https://repo2.maven.org/maven2/

国内阿里云同步仓库(快)：
https://maven.aliyun.com/repository/central
```

3、 使用Tomcat部署流程图工具“tools/osflow-designer”，修改osflow-designer\META-INF\config.properties中的数据库配置信息，并确认画图工具可以正常打开。（注：osflow-designer只是用于流程定义，上线后可不必部署。）

#### 使用说明

1、使用osflow-designer画图工具进行流程定义；

2、参考wfdemo对osflow-engine引擎进行调用；接口详细用法以及调用步骤，参考“document/流程引擎测试文档20201029.xlsx”文档。

#### 接口说明

**1、获取所有流程信息 workFlow.getProcessMsg()**

```java
   /**
     * 获取所有流程信息
     *
     * @return 所有流程信息的JSONObject对象
     */
    public void getProcessMsg() {
        WorkFlow workFlow = new WorkFlowImpl(conn);
        JSONArray processMsg = workFlow.getProcessMsg();
        System.out.println(processMsg.toJSONString());
    }
```



**2、打开流程审批单,（获取表单显示模板和业务数据接口）workFlow.openProcess(docUnid, processid, userid)**

```java
   /**
     * 打开一个流程（获取表单显示模板和业务数据接口）
     * @param docUnid    文档ID
     * @param processid  流程ID
     * @param userid     用户ID
     */
    private void openProcess(String docUnid, String processid, String userid) {
        WorkFlow workFlow = new WorkFlowImpl(conn);
        JSONObject object = workFlow.openProcess(docUnid, processid, userid);
        System.out.println(object.toJSONString());
    }
```



**3、启动或提交流程workFlow.runProcess(processid, docUnid, taskid, action, currentNodeid,**
**nextNodeid, nextUserList, copyUserList, userid, remark, isBackFlag, reassignmentFlag, maindata);**

```java
 /**
     * 启动流程、提交流程、审批流程
     */
    private void submitProcess() {
        WorkFlow workFlow = new WorkFlowImpl(conn);
        String processid = "433c77e90c28204ed90a0d400d27774580ac";
        String docUnid = ""; //流程文档ID
        String taskid = "";  // 用户任务ID【可选，多实例时则需要传】
        String action = "EndUserTask"; // 【提交动作】 GoToFirstNode、GoToOthers、EndUserTask、BackToDeliver、ReturnToAnyNode、
        // BackToReturnUser、GoToAnyNode、GoToPrevUser、GoToPrevNode、GoToArchived、GoToNextParallelUser、GoToNextNode
        String currentNodeid = ""; // 当前节点
        String nextNodeid = "T00002";   // 下一个节点
        String nextUserList = "admin"; // 下一个审批处理人ID
        String copyUserList = "";  // 传阅用户ID
        String userid = "lili";
        String remark = "lili给admin";
        String isBackFlag = ""; // 标记为回退，当为回退任意环节时，isBackFlag值可以为2，表示回退后需要直接返回给回退者
        String reassignmentFlag = ""; // 转交时是否需要转交者返回的标记1表示不需要2表示需要
        JSONObject maindata = new JSONObject();

        // 表单数据
//        maindata.put("name","lili");
//        maindata.put("phone","7758258");
//        maindata.put("sex","1");
//        maindata.put("age","22");

        String msg = workFlow.runProcess(processid, docUnid, taskid, action, currentNodeid,
                nextNodeid, nextUserList, copyUserList, userid, remark, isBackFlag, reassignmentFlag, maindata);
        System.out.println("流程提交结果：" + msg);
    }
```



**4、获取审批过程信息workFlow.getApprovalInfo(docUnid)**

```java
   /**
     * 获取审批过程信息
     * @param docUnid 文档ID
     */
    private void getApprovalInfo(String docUnid) {
        WorkFlow workFlow = new WorkFlowImpl(conn);
        JSONObject object = workFlow.getApprovalInfo(docUnid);
        System.out.println(object.toJSONString());
    }
```



**5、获取用户待办workFlow.getUserToDoInfo(userid)**

```java
   /**
     * 获取用户待办
     *
     * @param userid
     */
    public void showToDo(String userid) {
        WorkFlow workFlow = new WorkFlowImpl(conn);
        JSONArray TODOJSONArr = workFlow.getUserToDoInfo(userid);
        System.out.println(TODOJSONArr.toJSONString());
    }
```



#### 开源声明

本项目采用双重许可模式(GPL协议 + 商业许可协议)

QQ交流群：823545910