#  流程设计器组件FlowDesigner



##  前言

​	FlowDesigner来源于Linkey BPM中的流程设计器，作用于流程运行过程中的图形描述。它的操作简捷轻巧，能快速绘制出流程图。组件单独也可以使用，并能嵌入到任何需要该组件的系统中。



##  安装及运行

1. 将压缩包解压到WebRoot/linkey/bpm/目录下

   ![02](https://images.gitee.com/uploads/images/2019/0226/164554_e980571e_2066540.png "02.png")

2. 安装流程设计器组件应用

   ![03](https://images.gitee.com/uploads/images/2019/0226/164612_ea793b44_2066540.png "03.png")

3. 成功安装完成后，统一的入口在应用的视图中，通过该视图进入流程设计器，如下图所示。

   ![01](https://images.gitee.com/uploads/images/2019/0226/164626_a6cf5c68_2066540.gif "01.gif")



##  使用

​	进入流程设计器后，不要先着急画图，我们需要先指定当前流程的过程属性，换句话说也就是配置该流程在BPM系统中的几个必要配置，如该流程的名称等等。我们可以用鼠标右击作图区，在出现的菜单中点击第一项"过程属性"即可进入过程属性的编辑表单。

![05](https://images.gitee.com/uploads/images/2019/0226/164640_be5f0721_2066540.gif "05.gif")

在过程属性编辑表单中我们有几个必填的项：

1. 基本属性中的流程名称、流程编号、所属部门。如果不是从应用中的流程建模中进入流程设计器，还需指定所属应用。
2. 表单及模板中绑定流程主表单，也就是绑定你在应用中编写的流程主表单
3. 归档设置，设置归档时的流程分类

当我们设置完成后，点击左上角的保存退出，自此画流程前的准备工作就完成了。

​	绘制流程就是对节点的拖拉拽，简单易用。重要的是告诉大家如何修改节点的属性，我们可以通过双击需要编辑的节点快速进入节点属性编辑表单，也可以和之前编辑过程属性一样，在需要编辑的节点上右击，在出现的菜单中点击第一项"节点属性"即可进入节点属性的编辑表单，编辑节点名称后按保存退出。

![06](https://images.gitee.com/uploads/images/2019/0226/164654_53a12f16_2066540.gif "06.gif")



##  目录结构

```
newFlow
└───config
│   │
│	└───config.json # 配置文件
│	│
│	└───config配置说明.txt
│
└───css # 样式文件
│
└───helpDoc # 帮助文档
│
└───images # 图片资源
│
└───js # 第三方js资源
│
└───layui # 前端框架
│
└───myjs # 主要的js资源
│	│
│	└───FlowMonitorReady.js # 流程监控入口文件
│	│
│	└───FlowSimReady.js # 流程仿真入口文件
│	│
│	└───GlobalVar.js # 全局变量
│	│
│	└───MyFunction.js # 主要的函数文件，大部分逻辑都在这个文件中
│	│
│	└───MyLayui.js # layer弹出层相关代码
│	│
│	└───Ready.js # 程序入口文件
│	│
│	└───RightMenu.js # 右键菜单文件
│	│
│	└───ShowFlowReady.js # 查看流程图入口文件
│
└───plugings # 插件
│
└───Test # 所有的页面
│
└───util # 工具
│	│
│	└───GraphlibUtils.js # 主要是利用graphlib库中的相关方法实现图形描述的工具函数。如校验图形，存储图形等。
│	│
│	└───JsPlumbUtils.js # 主要是利用jsplumb库中的相关方法实现画图的工具函数。如连接节点、删除节点等。
│	│
│	└───LayuiUtils.js # 主要是利用layui库中的相关方法实现与layui相关的工具函数，如打开窗口等。
│	│
│	└───StringUtils.js # 一些常用的工具函数都放在这，例如生成uuid、十六进制颜色转Rgba颜色等。
```



##  数据结构说明

​	每一个图形其实都对应一段json数据，这里笔者以下图所示的流程图为例，简要说明数据存储的结构。

![04](https://images.gitee.com/uploads/images/2019/0226/164711_0feae317_2066540.png "04.png")

​	上述图形对应的数据结构如下：

```json
{
  "nodeDataArray": [
    {
      "text": "开始",
      "key": "E00001",
      "nodeType": "start",
      "locTop": 267,
      "locLeft": 442,
      "nodeHeight": "60px",
      "nodeWidth": "60px",
      "bgColor": "#78dc6b"
    },
    {
      "text": "申请",
      "key": "T00001",
      "nodeType": "comm",
      "locTop": 267,
      "locLeft": 652,
      "nodeHeight": "60px",
      "nodeWidth": "100px",
      "bgColor": "#6babdc"
    },
    {
      "text": "审批",
      "key": "T00002",
      "nodeType": "comm",
      "locTop": 267,
      "locLeft": 943,
      "nodeHeight": "60px",
      "nodeWidth": "100px",
      "bgColor": "#6babdc"
    },
    {
      "text": "结束",
      "key": "E00002",
      "nodeType": "end",
      "locTop": 267,
      "locLeft": 1201.9375,
      "nodeHeight": "60px",
      "nodeWidth": "60px",
      "bgColor": "#dc6b6b"
    }
  ],
  "linkDataArray": [
    {
      "from": "E00001",
      "to": "T00001",
      "routerId": "R00001",
      "label": "",
      "sourceAnchors": ["Bottom", "Right", "Top", "Left"],
      "targetAnchors": ["Bottom", "Right", "Top", "Left"]
    },
    {
      "from": "T00001",
      "to": "T00002",
      "routerId": "R00002",
      "label": "文本",
      "sourceAnchors": ["Bottom", "Right", "Top", "Left"],
      "targetAnchors": ["Bottom", "Right", "Top", "Left"]
    },
    {
      "from": "T00002",
      "to": "E00002",
      "routerId": "R00003",
      "label": "",
      "sourceAnchors": ["Bottom", "Right", "Top", "Left"],
      "targetAnchors": ["Bottom", "Right", "Top", "Left"]
    }
  ]
}
```

​	在JSON数据中，第一层有两个对象数组，nodeDataArray 和 linkDataArray。前者用来存储所有的节点对象，后者用来存储所有的路由线对象。我们先来看看节点对象，**以申请节点为例：**

```json
{
      "text": "申请",
      "key": "T00001",
      "nodeType": "comm",
      "locTop": 267,
      "locLeft": 652,
      "nodeHeight": "60px",
      "nodeWidth": "100px",
      "bgColor": "#6babdc"
}
```

 	1. text：节点显示的文本信息，不超过五个字全部显示，超过五个字的部分显示为"..." 
 	2. key：节点、泳道id。人工节点、自动节点的前缀为T，开始、结束、事件节点的前缀为E，网关节点的前缀为G，子流程节点的前缀为S，路由线的前缀为R，泳道的前缀为L。id是根据内部算法自动生成的，不能随意修改。 注：路由线id不存储在节点对象的key属性中，而是存储在路由线对象的routerId中。
 	3. nodeType：节点类型。系统根据不同的节点类型选取不同的节点样式和属性，关于节点对应的节点类型如下表所示。

|    节点    |      类型      |
| :--------: | :------------: |
|  开始节点  |     start      |
|  结束节点  |      end       |
|  人工节点  |      comm      |
|  自动节点  |    freedom     |
|    网关    |    gateway     |
|    事件    |     event      |
| 内部子流程 | innerChildFlow |
| 外部子流程 | outerChildFlow |
|  横向泳道  | broadwiseLane  |
|  纵向泳道  | directionLane  |

4. locTop：节点在画布中的纵向位置
5. locLeft：节点在画布中的横向位置 
6. nodeHeight：节点的高度，由于节点缩放属性未开启，该属性是固定的。
7. nodeWidth：节点的宽度，由于节点缩放属性未开启，该属性是固定的。
8. bgColor：节点的背景色，由于屏蔽了设置背景色功能，该属性是固定的。



**我们以申请节点到审批节点的路由线对象为例：**

```json
{
      "from": "T00001",
      "to": "T00002",
      "routerId": "R00002",
      "label": "文本",
      "sourceAnchors": ["Bottom", "Right", "Top", "Left"],
      "targetAnchors": ["Bottom", "Right", "Top", "Left"]
}
```

1. from：源节点(Source Node)的id，从申请节点连线至审批节点，申请节点就是源节点。
2. to：目标节点(Target Node)的id，从申请节点连线至审批节点，审批节点就是目标节点。
3. routerId：路由线id。id是根据内部算法自动生成的，不能随意修改。
4. label：路由线上显示的文本信息。
5. sourceAnchors：源节点的锚点数组，敏捷版流程设计器会根据算法决定最合适的路径绘制路由线，两点确定一条路由线，这两点称之为端点，锚点就是端点能自动选择的位置，默认为节点四条边的中点处，也可以通过设置中修改锚点，敏捷版流程设计器内置的锚点如下。默认锚点为Bottom、Right、Top、Left

|    锚点     |   描述   |
| :---------: | :------: |
|   Bottom    | 下边中点 |
|    Right    | 右边中点 |
|     Top     | 上边中点 |
|    Left     | 左边中点 |
|  TopRight   |  右上角  |
| BottomRight |  右下角  |
| BottomLeft  |  左下角  |
|   TopLeft   |  左上角  |

6. targetAnchors：目标节点的锚点数组，上同。



​	**加载过程**：敏捷版流程设计器获取到JSON数据后加载到画布中分为两个步骤，第一步遍历nodeDataArray节点对象数组将所有节点渲染到画布中，包括大小、位置、形状、属性等。第二步遍历linkDataArray路由线对象数组绘制所有的路由线。



##  常见问题

* 组件应用中提供了rest接口却无法使用？

  请确保系统是否升级过Rest接口，若没有升级，可以使用压缩包中提供的升级方案。

* 为什么我新建的流程在启动时报错，后台报空指针异常？

  请确保缺省保存的url是正确的，建议由流程设计器中修改url，不要通过组件应用中的视图去修改，否则系统会将'&'字符转义成'`&apm;`'而导致缺省保存失败。

