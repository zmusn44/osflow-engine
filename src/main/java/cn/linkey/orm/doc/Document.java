package cn.linkey.orm.doc;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Set;

public interface Document {

  /**
   * 把文档根据指定的表单转换为HTML代码
   * @param formNumber 指定的表单编号
   * @param isedit ture表示可编辑状态，false表示只读状态
   * @return 返回html字符串
   */
  public String toHtml(String formNumber, boolean isedit);

  /**
   * 文档转Json格式的字符串，使用数据库表中的字段类型
   * @return Json字符串
   */
  public String toJson();

  /**
   * 文档转Json格式的字符串<br>
   * 使用本系统中的配置表的字段名称进行输出，如果没有则按数据库表中的字段名进行输出
   * @param useTableConfig true 表示使用,false 表示使用数据库中的字段名
   * @return Json字符串
   */
  public String toJson(boolean useTableConfig);

  /**
   * 把xml格式中的数据转换存储为doc中的数据
   * 如果一次性要转入多个的xml文件请使用Documents.xmlfile2dc()方法
   * @param xml 字符串格式为&gt;Items&lt;&gt;WFItem name="字段名"&lt;字段值&gt;/WFItem&lt;&gt;/Items&lt;
   */
  @SuppressWarnings("unchecked")
  public void appendFromXml(String xml);

  /**
   * json字符串转为文档对像的字段
   * @param jsonStr 字符串格式为{"fieldName1":"字段值","fdName2":"value2"}
   */
  public void appendFromJsonStr(String jsonStr);

  /**
   * 文档转换成为xml字符串
   * @param isCDATA true表示使用cdata进行转义，否则将对字段值进行编码,并可以用Rdb.decode()进行解码
   * @return 返回解码后的XML字符串
   */
  public String toXmlStr(boolean isCDATA);

  /**
   * 文档对像转换成为xmlfile文件
   * @param filePath 要存盘的文件全路径
   * @param encType 默认utf-8编码输出，可以指定为gb2312
   * @return 返回true表示成功，false表示失败
   */
  public boolean saveToXmlfile(String filePath, String encType);

  /**
   * 按照数据流出配置信息把文档数据写入到外部数据源中去
   * @param configid 数据流程配置编号
   * @param formatRuleNum 对流出的数据进行二次格式化
   * @return 返回1表示流出成功
   * @throws Exception 连接外部数据源出错
   */
  public int saveToOutData(String configid, String formatRuleNum) throws Exception;

  /**
   * 根据文档unid,tableName，初始化字段到Document对像中
   * @param docUnid 32位文档unid对应数据库表中的WF_OrUNID字段
   */
  public void initByDocUnid(String docUnid);

  /**
   * 把sql语句中的字段数据初始化到当前文档中
   * @param sql 要执行的sql语句
   */
  public void initBySql(String sql);

  /**
   * 把Request中的参数初始化到文档对像中去
   * @param request HttpServletRequest请求对像
   * @param encode true 表示对request中的请求参数进行编码&lt;&gt;替换为转义字符; false 表示不需要进行编码,编码后可以防止脚本注入到计算域中而被执行
   */
  public void appendFromRequest(HttpServletRequest request, boolean encode);

  /**
   * 把Request中的参数初始化到文档对像中去
   * @param request HttpServletRequest请求对像
   */
  public void appendFromRequest(HttpServletRequest request);

  /**
   * 把Request中的参数初始化到文档对像中去
   */
  public void appendFromRequest();

  /**
   * 把新的map对像中的值一次性初始化到文档中
   * @param mapData 需要初始化的map对象数据
   */
  public void appendFromMap(HashMap<String, String> mapData);

  /**
   * 追加新的值到已有字段中去<br>
   * 旧值和新值都需要是用逗号进行分隔的值才可以，会自动去掉重复值和空值
   * @param fdName 字段名称
   * @param textList 要添加的新值多个用逗号分隔
   */
  public void appendTextList(String fdName, String textList);

  /**
   * 追加新的值到已有字段中去<br>
   * 旧值和新值都需要是用逗号进行分隔的值才可以，会自动去掉重复值和空值
   * @param fdName 字段名称
   * @param textList 要添加的新值为set集合
   */
  public void appendTextList(String fdName, Set<String> textList);

  /**
   * 附加ResultSet的数据到文档中
   * @param rs 输入rs对像把rs中的数据初始化到文档的map中去
   */
  public void appendFromResultSet(ResultSet rs);

  /**
   * 判断文档是否处于锁定状态,未锁定返回空值,已锁定返回锁定者的用户名
   * @param userid 要判断的用户名，需要判断的主文档UNID
   * @return 未锁定返回空值,已锁定返回锁定者的用户名
   */
  public String getLockStatus(String userid);

  /**
   * 解锁当前文档
   * @return 返回true解锁成功,false解锁失败
   */
  public boolean unlock();

  /**
   * 判断当前文档是否是一个新文档
   * @return 返回ture表示是新文档,false表示否
   */
  public boolean isNewDoc();

  /**
   * 判断当前文档是否为空文档对像
   * @return true 表示是空文档， false 表示不是空文档
   */
  public boolean isNull();

  /**
   * 判断当前文档是否为空文档对像
   */
  public void setIsNull();

  /**
   * 获得数据库表名
   * @return 返回数据表表名
   */
  public String getTableName();

  /**
   * 设置文档所在数据库表名
   * 表名可以不存在，如果存在请使用setTableName()方法 只有数据库表名不存在时才使用本方法
   * @param tableName 任意名称
   */
  public void setTableNameOnly(String tableName);

  /**
   * 设置文档所在数据库表
   * 设置后系统会自动计算数据库表的所有字段到文档的tableFdConfig属性中
   * @param tableName 数据库表名，数据库表一定要真实存在，否则报错 如果表确实不存在可以使用setTableNameOnly()方法
   */
  public void setTableName(String tableName);

  /**
   * 设置文档所在数据库表
   * 设置后系统会自动计算数据库表的所有字段到文档的tableFdConfig属性中
   * @param conn 数据库链接对像
   * @param tableName 数据库表名，数据库表一定要真实存在，否则报错 如果表确实不存在可以使用setTableNameOnly()方法
   */
  public void setTableName(Connection conn, String tableName);

  /**
   * 获得文档的创建时间,如果是新文档则返回当前时间
   * @return 返回创建时间
   */
  public String getCreated();

  /**
   * 获得当前文档的32位唯一id号
   * 若当前文档没有唯一的docunid，那么底层将通过Rdb.getNewid()获取一个新的32位唯一id
   * @return 返回文档的32位id号
   */
  public String getDocUnid();

  /**
   * 获得文档所的字段列表
   * @return 返回Set对像
   */
  public Set<String> getAllItemsName();

  /**
   * 获得所有字段的map对像
   * key为字段名 value为字段值 示例:HashMap&gt;String,String&lt; fieldMap=doc.getAllItems();
   * @return 返回map对像
   */
  public HashMap<String, String> getAllItems();

  /**
   * 清空文档
   */
  public void clear();

  /**
   * 拷贝一个文档的到指定的表中去
   * @param tableName 目的数据库表名
   * @return 返回新的目标文档对像
   */
  public Document copyTo(String tableName);

  /**
   * 拷贝一个文档的到指定的表中去
   * @param conn 目的数据库链接对像
   * @param tableName 目的数据库表名
   * @return 返回新的目标文档对像
   */
  public Document copyTo(Connection conn, String tableName);

  /**
   * 拷贝一个文档的所有字段到另一个文档中去
   * @param targetDocument 目的文档对像
   * @return 返回新的目标文档对像
   */
  public Document copyAllItems(Document targetDocument);

  /**
   * 拷贝一个文档的所有字段到另一个文档中去,不包含WF_开头的字段内容
   * @param targetDocument 目的文档对象
   * @param noSystemField true表示不拷贝WF_开头的系统字段，false表示全部
   * @return 返回新的目标文档对像
   */
  public Document copyAllItems(Document targetDocument, boolean noSystemField);

  /**
   * 设置文档的字段值，有则覆盖，没有则新增,不区分大小写
   * @param fdName 字段名
   * @param fdValue 字段值
   */
  public void s(String fdName, Object fdValue);

  /**
   * 移除文档的字段值
   * @param fdName 字段名
   */
  public void r(String fdName);

  /**
   * 设置文档的字段值，有则覆盖，没有则新增
   * @param fdName 字段名
   * @param fdValue set集合值会自动转换为用逗号分隔的字符串存储到fdName字段中去
   */
  public void s(String fdName, Set<String> fdValue);

  /**
   * 获取字段值，如果字段不存在则返回 "" 空值，不区分大小写
   * @param fdName 要获取的字段名
   * @return 存在返回对应的值，不存在返回为空
   */
  public String g(String fdName);

  /**
   * 在当前文档中查找已经存在的字段名称，不区分大小写
   * @param fdName 需要查找的字段名
   * @return 返回对应的值，不存在返回null
   */
  public String getExistFdNameInDoc(String fdName);



  /**
   * 获得此文档所在数据库表的所有字段名称列表集合,使用默认数据源
   * @return 返回数据库表的所有字段Set集合
   */
  public HashMap<String, String> getColumnList();

  /**
   * 获得此文档所在数据库表的所有字段名称集合
   * @param conn 指定数据库链接对像
   * @return 返回数据库表的所有字段Set集合
   */
  public HashMap<String, String> getColumnList(Connection conn);

  /**
   * 删除文档中的一个字段,不区分大小写
   * @param fdName 要删除的字段名称
   */
  public void removeItem(String fdName);

  /**
   * 看文档中是否存在此字段,区分大小写
   * @param itemName 表字段名
   * @return true表示有，false表示没有
   */
  public boolean hasItem(String itemName);

  /**
   * 判断数据库表中是否存在此字段，不区分大小写
   * @param fdName 表字段名
   * @return true表示有，false表示没有
   */
  public boolean hasTableItem(String fdName);

  /**
   * 获得文档设定的自增长列名称，默认为空值，不允许数据库表有自增长列
   * @return 有则返回字段名称
   */
  public String getAutoKeyid();

  /**
   * 设定文档的自增长列名称
   * 默认为空值 如果有自增长列则必须用此方法指定，否则存盘时因无法更新自增长列而报错
   * @param autoKeyid 列名称
   */
  public void setAutoKeyid(String autoKeyid);

  /**
   * 获得本文档所在数据库表的关键字段名称
   * @return 返回keyid，默认为WF_OrUnid
   */
  public String getKeyid();

  /**
   * 设置本文档所在数据库表的关键字段名称,默认为WF_OrUnid
   * @param keyid 默认为WF_OrUnid
   */
  public void setKeyid(String keyid);

  /**
   * 设置本文档所在数据库表的关键字段名称,默认为WF_OrUnid
   * @param keyid 关键字段，默认为WF_OrUnid
   * @param keyVaue 字段的值
   */
  public void setKeyid(String keyid, String keyVaue);

  /**
   * 存盘到当前数据源中
   * @return 返回非负数表示存盘成功，否则存盘失败
   */
  public int save();

  /**
   * 存盘到当前数据源指定的表中
   * @param tableName 数据库表名
   * @return 返回非负数表示存盘成功，否则存盘失败
   */
  public int save(String tableName);

  /**
   * 指定数据库链接对像和表名把文档存储到指定的表中去
   * @param conn Connection 连接对象
   * @return 返回非负数表示存盘成功，否则存盘失败
   */
  public int save(Connection conn);

  /**
   * 指定数据库链接对像和表名把文档存储到指定的表中去
   * @param conn Connection 连接对象
   * @param tableName 表名
   * @return 返回非负数表示存盘成功，否则存盘失败
   */
  public int save(Connection conn, String tableName);

  /**
   * 指定数据库链接对像和表名把文档存储到指定的表中去
   * @param tableName 表名
   * @param extendTableName 扩展表名必须是xmldata时才需要进行存储
   * @return 返回非负数表示存盘成功，否则存盘失败
   */
  public int saveToExtendTable(String tableName, String extendTableName);

  /**
   * 指定数据库链接对像和表名把文档存储到指定的表中去
   * @param conn Connection 连接对象
   * @param tableName 表名
   * @param extendTableName 扩展表名必须是xmldata时才需要进行存储
   * @return 返回非负数表示存盘成功，否则存盘失败
   */
  public int save(Connection conn, String tableName, String extendTableName);

  /**
   * 清除已有的动态表格字段的值
   * @param fdList 多个字段用逗号分隔，字段名称不需要带_1数字编号
   * @return 返回删除的字段个数
   */
  public int removeEditorField(String fdList);

  /**
   * 把文档移动一份到回收站中去 注意：本方法不会拷贝附件到回收站中去
   * @return i大于0 表示处理成功 i小于0 表示异常
   */
  public int removeToTrash();

  /**
   * 把文档移动一份到回收站中去
   * @param conn 数据库链接 注意：本方法不会拷贝附件到回收站中去
   * @return i大于0 表示处理成功 i小于0 表示异常
   */
  public int removeToTrash(Connection conn);

  /**
   * 删除当前文档,返回非负数表示删除成功，否则删除失败
   * @param trash true表示删除时copy一份到回收站中，false表示强制删除不可恢复
   * @return 返回受影响的行数
   */
  public int remove(boolean trash);

  /**
   * 删除当前文档,返回非负数表示删除成功，否则删除失败
   * @param conn 数据库链接对像
   * @param trash true 表示删除时copy一份到回收站中，false 示强制删除不可恢复
   * @return 返回受影响的行数
   */
  public int remove(Connection conn, boolean trash);
  /**
   * 获得当前文档所对应实体表的字段配置信息包含字段名和字段类型的map对像
   * @return key为字段名,value为字段类型的map对像
   */
  public HashMap<String, String> getTableFdConfig();

  /**
   * 设置文档对应实体表的字段配置信息包含字段名称和字段类型的map对像
   * @param tableFdConfig 一个包含文档信息的map对象，
   */
  public void setTableFdConfig(HashMap<String, String> tableFdConfig);

  /**
   * 获得当前文档来源的数据库链接的类型(MSSQL\MYSQL\ORACLE)
   * @return 返回数据库类型
   */
  public String getDbType();

  /**
   * 当doc.save(conn)指定链接存盘时，系统会自动重新计算新链接的数据库类型
   * @param dbType 数据库类型
   */
  public void setDbType(String dbType);
}
