package cn.linkey.orm.dao;

import cn.linkey.orm.doc.Document;

import java.sql.*;
import java.util.*;

public interface Rdb {

  /**
   * 获得默认的线程数据库链接对像 获得本对像不要执行close()因为系统会自动关闭
   * @return 获得数据库连接对象
   * @throws Exception 如果此方法获取数据库连接失败
   */
  public Connection getConnection() throws Exception;

  /**
   * @param conn 数据库连接
   * */
  public void setConnection(Connection conn);

  /**
   * @param dbDriver 数据库驱动
   * @param dbUser 用户名
   * @param dbPwd 密码
   * @param dbUrl 数据库连接URL
   */
//  public void setDbConnection(String dbDriver, String dbUser, String dbPwd, String dbUrl);

  /**
   * 获得一个新的连接
   * @param dataSourceid 数据源的配置id 不管是否为default都会返回一个新的链接对像 链接对像在用完后一定要调用Rdb.close(conn);
   * @return 返回一个连接对像
   * @throws Exception 如果此方法获取数据库连接失败
   */
  public Connection getNewConnection(String dataSourceid) throws Exception;

  /**
   * 开始事务
   * @param autoCommit true：表示启动自动提交 false：表示禁用自动提交
   */
  public void setAutoCommit(boolean autoCommit);

  /**
   * 获得事务自动提交标识
   * @return true,false
   */
  public boolean getAutoCommit();
  /**
   * 提交事务
   */
  public void commit();
  /**
   * 提交事务
   * @param  conn 数据库连接
   */
  public void commit(Connection conn);

  /**
   * 回滚事务
   */
  public void rollback();

  /**
   * 分析SQl语句
   * @param sql 转入的sql语句
   * @return 返回重新分析过的sql语句
   */
  public String parserSql(String sql);

  /**
   * 从sql语句中分析出tablename
   * @param sql 要分析的sql语句
   * @return 返回表名称
   */
  public String getTableNameFromSql(String sql);

  /**
   * 通过sql获取Document对象
   * @param sql 要执行的sql语句,表名将自动从sql语句中通过form关键字分析得到 如果是多表的情况请使用getDocumentBySql(String tableName,String sql)方法
   * @return Document对像
   */
  public Document getDocumentBySql(String sql);

  /**
   * 通过sql获取Document对象
   * @param conn 数据库链接对像
   * @param sql 要执行的sql语句,表名将自动从sql语句中通过form关键字分析得到 如果是多表的情况请使用getDocumentBySql(String tableName,String sql)方法
   * @return Document对像
   */
  public Document getDocumentBySql(Connection conn, String sql);

  /**
   * 通过sql获取Document对象
   * @param sql 要执行的sql语句
   * @param tableName 数据所在表名 如果sql的select是多表或者只时选取不再回存时可以指定为空值
   * @return Document对像
   */
  public Document getDocumentBySql(String tableName, String sql);

  /**
   * 获得得文档集合数组
   * @param sql 可以返回一条或多条记录的sql语句，表名将自动从sql语句中通过form关键字分析得到
   * @return 返回文档数组
   */
  public Document[] getAllDocumentsBySql(String sql);



  /**
   * 获得得文档集合数组
   * @param tableName 数据库表名
   * @param sql 可以返回一条或多条记录的sql语句
   * @return 返回文档数组
   */
  public Document[] getAllDocumentsBySql(String tableName, String sql);

  /**
   * 指定数据库链接并获得得文档集合数组
   * @param conn 数据库链接对像
   * @param tableName 数据库表名 如果sql的select是多表或者只时选取不再回存时可以指定为空值
   * @param sql 可以返回一条或多条记录的sql语句
   * @return 返回文档数组
   */
  public Document[] getAllDocumentsBySql(Connection conn, String tableName, String sql);

  /**
   * 指定数据库链接并获得得文档集合数组
   * @param conn 数据库链接对像
   * @param tableName 数据库表名 如果sql的select是多表或者只时选取不再回存时可以指定为空值
   * @param sql 可以返回一条或多条记录的sql语句
   * @param cmdParams cmdParams
   * @return 返回文档数组
   */
  public Document[] getAllDocumentsBySql(Connection conn, String tableName, String sql, Object... cmdParams);

  /**
   * 指定数据库链接并分页获得得文档集合数组 性能慢于getAllDocumentsByPage但本方法的数据库兼容性比较好
   * @param sql 可以返回一条或多条记录的sql语句
   * @param pageNum 当前页数，第一页传1
   * @param pageSize 每页显示数如：25
   * @return 返回文档数组
   */
  public Document[] getAllDocumentsBySql(String sql, int pageNum, int pageSize);

  /**
   * 指定数据库链接并分页获得得文档集合数组 性能慢于getAllDocumentsByPage但本方法的数据库兼容性比较好
   * @param tableName 数据库表名 如果sql的select是多表或者只时选取不再回存时可以指定为空值
   * @param sql 可以返回一条或多条记录的sql语句
   * @param pageNum 当前页数，第一页传1
   * @param pageSize 每页显示数如：25
   * @return 返回文档数组
   */
  public Document[] getAllDocumentsBySql(String tableName, String sql, int pageNum, int pageSize);

  /**
   * 指定数据库链接并分页获得得文档集合数组 性能慢于getAllDocumentsByPage但本方法的数据库兼容性比较好
   * @param conn 数据库链接对像
   * @param tableName 数据库表名
   * @param sql 可以返回一条或多条记录的sql语句
   * @param pageNum 当前页数，第一页传1
   * @param pageSize 每页显示数如：25
   * @return 返回文档数组
   */
  public Document[] getAllDocumentsBySql(Connection conn, String tableName, String sql, int pageNum, int pageSize);

  /**
   * 获得得文档集合set,集合中可以保证sql语句获得的结果顺序
   * @param sql 可以返回一条或多条记录的sql语句
   * @return 返回set集合
   */
  public LinkedHashSet<Document> getAllDocumentsSetBySql(String sql);

  /**
   * 获得得文档集合LinkedHashSet，集合中可以保证sql语句获得的结果顺序
   * @param tableName 数据库表名
   * @param sql 可以返回一条或多条记录的sql语句
   * @return 返回set集合
   */
  public LinkedHashSet<Document> getAllDocumentsSetBySql(String tableName, String sql);

  /**
   * 指定数据库链接并获得得文档集合set，集合中可以保证sql语句获得的结果顺序
   * @param conn 数据库链接对像
   * @param tableName 数据库表名
   * @param sql 可以返回一条或多条记录的sql语句
   * @return 返回set集合
   */
  public LinkedHashSet<Document> getAllDocumentsSetBySql(Connection conn, String tableName, String sql);

  /**
   * 指定数据库链接并获得得文档集合set，集合中可以保证sql语句获得的结果顺序
   * @param conn 数据库链接对像
   * @param tableName 数据库表名
   * @param sql 可以返回一条或多条记录的sql语句
   * @param cmdParams SQL中带?号时的替换参数
   * @return 返回set集合
   */
  public LinkedHashSet<Document> getAllDocumentsSetBySql(Connection conn, String tableName, String sql, Object... cmdParams);

  /**
   * 指定数据库链接并分页获得得文档集合set，集合中可以保证sql语句获得的结果顺序
   * @param tableName 数据库表名
   * @param fieldList 要选择的字段全选可以*号
   * @param orderByStr 要排序的字段,必须传入参数,格式为:order by fd1,fd2 desc
   * @param whereSql 选择条件语句,格式为: where fd1=1 and fd2 &gt; 10
   * @param pageNum 当前页数第一页传1
   * @param pageSize 每页显示数如：25
   * @return 返回LinkedHashSet集合
   */
  public LinkedHashSet<Document> getAllDocumentsSetByPage(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize);

  /**
   * 指定数据库链接并分页获得得文档集合set，集合中可以保证sql语句获得的结果顺序
   * @param conn 数据库链接对像
   * @param tableName 数据库表名
   * @param fieldList 指定要选择的列全部返回传入*号
   * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
   * @param whereSql 选择条件格式为:where fd1=1 and fd2&gt;10
   * @param pageNum 当前页，第一页传1
   * @param pageSize 每页显示数如：25
   * @return 返回LinkedHashSet集合
   */
  public LinkedHashSet<Document> getAllDocumentsSetByPage(Connection conn, String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize);

  /**
   * 获得oracle的分页查询语句
   * @param tableName 数据库表名
   * @param fieldList 指定要选择的列全部返回传入*号
   * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
   * @param whereSql 选择条件格式为: where fd1=1 and fd2大于10
   * @param pageNum 当前页，第一页传1
   * @param pageSize 每页显示数如：25
   * @return Oracle分页查询结果
   */
  public String getOraclePageSql(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize);

  /**
   * 获得Ms SqlServer的分页查询语句
   * @param tableName 数据库表名
   * @param fieldList 指定要选择的列全部返回传入*号
   * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
   * @param whereSql 选择条件格式为:where fd1=1 and fd2大于10
   * @param pageNum 当前页，第一页传1
   * @param pageSize 每页显示数如：25
   * @return MSSQL分页查询结果
   */
  public String getMSSQLPageSql(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize);

  /**
   * 获得My SQL的分页查询语句
   * @param tableName 数据库表名
   * @param fieldList 指定要选择的列全部返回传入*号
   * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
   * @param whereSql 选择条件格式为:where fd1=1 and fd2大于10
   * @param pageNum 当前页，第一页传1
   * @param pageSize 每页显示数如：25
   * @return 返回分页查询结果
   */
  public String getMYSQLPageSql(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize);

  /**
   * 获得PostgreSQL的分页查询语句
   * @param tableName 数据库表名
   * @param fieldList 指定要选择的列全部返回传入*号
   * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
   * @param whereSql 选择条件格式为:where fd1=1 and fd2大于10
   * @param pageNum 当前页，第一页传1
   * @param pageSize 每页显示数如：25
   * @return 返回分页查询结果
   */
  public String getPGSQLPageSql(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize);

  /**
   * 通过sql获取Document对象
   * @param conn 指定数据库链接对像
   * @param sql 要执行的sql语句
   * @param tableName 数据所在表名
   * @return Document 返回文档对像,要记得关闭conn链接
   */
  public Document getDocumentBySql(Connection conn, String tableName, String sql);

  /**
   * 已有文档实例对像，再根据sql语句进行初始化数据到文档中去
   * @param doc 要进行初始化的文档对像
   * @param sql 要执行的sql语句
   * @return Document要进行数据初始化的文档对像
   */
  public Document appendDataFromSql(Document doc, String sql);

  /**
   * 已有文档实例对像，再根据sql语句进行初始化数据到文档中去
   * @param conn 指定数据库链接
   * @param doc 要进行初始化的文档对像
   * @param sql 要执行的sql语句
   * @return Document 返回初始化后的文档对像
   */
  public Document appendDataFromSql(Connection conn, Document doc, String sql);

  /**
   * 获得运行sql获取的第一条数据
   * @param sql sql语句
   * @return 返回运行sql获取的第一条数据
   */
  public String getValueTopOneBySql(String sql);

  /**
   * 直接通过sql语句返回得到一个字段的值
   * @param sql 要执行的sql语句、 只能是select一个字段如：select subject form tablename
   * @return 返回字符串，如果select 多条记录时字段值使用,逗号进地分隔返回
   */
  public String getValueBySql(String sql);

  /**
   * 直接通过sql语句返回一条记录或者多条记录的某个字段的值 如果字段中的值本身有逗号，则会把逗号分隔成set的成员
   * 本方法不保证sql语句获得记录的排序结果 如果要排序请使用getValueLinkedSetBySql()方法
   * @param sql 要执行的sql语句如：select subject form tablename
   * @return 返回set集合，会去掉重复值和空值
   */
  public HashSet<String> getValueSetBySql(String sql);

  /**
   * 直接通过sql语句返回一条记录或者多条记录的某个字段的值 如果字段中的值本身有逗号则会把逗号分隔成set的成员
   * 本方法不保证sql语句获得记录的排序结果 如果要排序请使用getValueLinkedSetBySql()方法
    * @param sql 要执行的sql语句如：select subject form tablename
   * @param splitComma 字段值中的的逗号是否进行split成多个值加入到set集合中 true：表示按逗号(,)split false：表示否
   * @return 返回set集合，会去掉重复值和空值
   */
  public HashSet<String> getValueSetBySql(String sql, boolean splitComma);

  /**
   * 直接通过sql语句返回得到一个字段的值 如果字段中的值本身有逗号则会把逗号分隔成set的成员
   * 本方法不保证sql语句获得记录的排序结果 如果要排序请使用getValueLinkedSetBySql()方法
   * @param sql 要执行的sql语句如：select subject form tablename
   * @param conn 指定数据库链接对像
   * @param splitComma 字段值中的的逗号是否进行split成多个值加入到set集合中 true：表示按逗号(,)split false：表示否
   * @return 返回set集合，会去掉重复值和空值
   */
  public HashSet<String> getValueSetBySql(Connection conn, String sql, boolean splitComma);

  /**
   * 直接通过sql语句返回一条记录或者多条记录的某个字段的值 如果字段中的值本身有逗号则会把逗号分隔成set的成员
   * 本方法可以保证sql语句查询结果按顺序保存到set集合中
   * @param sql 要执行的sql语句如：select subject form tablename
   * @return 返回set集合，会去掉重复值和空值
   */
  public LinkedHashSet<String> getValueLinkedSetBySql(String sql);

  /**
   * 直接通过sql语句返回一条记录或者多条记录的某个字段的值 如果字段中的值本身有逗号则会把逗号分隔成set的成员
   * 本方法可以保证sql语句查询结果按顺序保存到set集合中
   * @param sql 要执行的sql语句如：select subject form tablename
   * @param splitComma 字段值中的的逗号是否进行split成多个值加入到set集合中 true：表示按逗号(,)split false表示否
   * @return 返回set集合，会去掉重复值和空值
   */
  public LinkedHashSet<String> getValueLinkedSetBySql(String sql, boolean splitComma);

  /**
   * 直接通过sql语句返回得到一个字段的值,如果字段中的值本身有逗号则会把逗号分隔成set的成员 本方法可以保证sql语句查询结果按顺序保存到set集合中
   * @param conn 指定数据库链接对像
   * @param sql 要执行的sql语句如：select subject form tablename
   * @param splitComma 字段值中的的逗号是否进行split成多个值加入到set集合中true表示按,号split，false表示否
   * @return 返回set集合，会去掉重复值和空值
   */
  public LinkedHashSet<String> getValueLinkedSetBySql(
          Connection conn, String sql, boolean splitComma);

  /**
   * 直接通过sql语句返回得到一个由Text和Value组成的字符串如： 张三|U003
   * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename Subject为Text,id为值
   * @return 返回字符串，如果select 多条记录时字段值使用,逗号进地分隔返回
   */
  public String getValueListBySql(String sql);

  /**
   * 根据sql语句返回select标签所要求的字符串格式即text|value,test|value格式
   * @param sql 指定sql语句只能也必须选择两列 select text,value from tablename
   * @param defaultValue 指定默认选中的项的值
   * @return 返回字符串text|value,text1|value1.....
   */
  public String getValueForSelectTagBySql(String sql, String defaultValue);

  /**
   * 根据sql语句返回select标签所要求的字符串格式即text|value,test|value格式
   * @param conn 指定数据库链接对像
   * @param sql 指定sql语句只能也必须选择两列 select text,value from tablename
   * @param defaultValue 指定默认选中的项的值
   * @return 返回字符串text|value,text1|value1,text2|value2|selected.....
   */
  public String getValueForSelectTagBySql(Connection conn, String sql, String defaultValue);

  /**
   * 直接通过sql语句返回第一列和第二列的HashMap对像,select可以是多条结果记录 不保持sql语句记录的顺序,特点查询速度快
   * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename
   * @return 返回map对像
   */
  public HashMap<String, String> getMapDataBySql(String sql);

  /**
   * 直接通过sql语句返回第一列和第二列的HashMap对像,select可以是多条结果记录 不保持sql语句记录的顺序,特点查询速度快
   * @param conn 数据库链接对像,conn链接对像用完后需要自已关闭回收资源
   * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename Subject为Text,id为值
   * @return 返回map对像
   */
  public HashMap<String, String> getMapDataBySql(Connection conn, String sql);

  /**
   * 直接通过sql语句返回第一列和第二列的HashMap对像,select可以是多条结果记录 可以保持sql语句的记录顺序,特点可以保持数据的顺序
   * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename
   * @return 返回map对像
   */
  public LinkedHashMap<String, String> getLinkedMapDataBySql(String sql);

  /**
   * 直接通过sql语句返回第一列和第二列的HashMap对像,select可以是多条结果记录 可以保持sql语句的记录顺序,特点可以保持数据的顺序
   * @param conn 数据库链接对像,conn链接对像用完后需要自已关闭回收资源
   * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename Subject为Text,id为值
   * @return 返回map对像
   */
  public LinkedHashMap<String, String> getLinkedMapDataBySql(Connection conn, String sql);

  /**
   * 直接通过sql语句返回第一列和第二列的HashMap对像,select只能返回唯一一条的记录
   * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename where id=1
   * @return 返回map对像
   */
  public HashMap<String, String> getOneMapDataBySql(String sql);

  /**
   * 直接通过sql语句返回第一列和第二列的HashMap对像,select只能返回唯一一条的记录
   * @param conn 数据库链接对像,conn链接对像用完后需要自已关闭回收资源
   * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename where id=1
   * @return 返回map对像
   */
  public HashMap<String, String> getOneMapDataBySql(Connection conn, String sql);

  /**
   * 注意：HashMap是不能重复key的，所以 getAllmapDataBysql返回的值的key是sql中指定的字段，值是是所有记录的用逗号相加的结果
   * 循环sql语句中的字段，并把所有记录的字段值用逗号进行相加，存入到map对像中返回
   * @param sql select语句支持返回多条记录的sql语句如：select id,name from table
   * @return 返回key为字段名的map对像,map对像中的key为select语句中指定的字段名称，而不是数据库表中的字段名称
   */
  public HashMap<String, String> getAllMapDataBySql(String sql);

  /**
   * 注意：HashMap是不能重复key的，所以 getAllmapDataBysql返回的值的key是sql中指定的字段，值是是所有记录的用逗号相加的结果
   * 循环sql语句中的字段，并把所有记录的字段值用逗号进行相加，存入到map对像中返回
   * @param conn 数据库链接对像
   * @param sql select语句支持返回多条记录的sql语句如：select id,name from table
   * @return 返回key为字段名的map对像,map对像中的key为select语句中指定的字段名称，而不是数据库表中的字段名称
   */
  public HashMap<String, String> getAllMapDataBySql(Connection conn, String sql);





  /**
   * 判断记录是否存在
   * @param sql 要判断是否存在的sql语句
   * @return true表示存在,false表示不存在
   */
  public boolean hasRecord(String sql);

  /**
   * 判断记录是否存在
   * @param conn 指定数据库链接
   * @param sql 要判断是否存在的sql语句
   * @return true表示存在,false表示不存在
   */
  public boolean hasRecord(Connection conn, String sql);

  /**
   * 获得指定sql语句返回的记录数,常用于判断记录是否存在
   * @param sql sql语句如：select * from table where id=1
   * @return 0表示记录不存在 否则返回记录数
   */
  public int getCountBySql(String sql);

  /**
   * 获得指定sql语句返回的记录数
   * @param conn 指定数据库链接，链接用完后要手动关闭
   * @param sql sql语句如：select * from table where id=1
   * @return 0表示记录不存在 否则返回记录数
   */
  public int getCountBySql(Connection conn, String sql);

  /**
   * 指定数据库链接并直接通过sql语句返回得到一个字段的值
   * @param conn 指定链接
   * @param sql 要执行的sql语句，只能是select一个字段如：select subject form tablename where id=1
   * @return 返回字符串，如果select 多条记录时字段值使用,逗号进地分隔返回
   */
  public String getValueBySql(Connection conn, String sql);

  /**
   * 直接通过sql语句返回得到一个由Text和Value组成的字符串如： 张三|U003
   * @param conn 指定链接
   * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename Subject为Text,id为值
   * @return 返回字符串，如果select 多条记录时字段值使用,逗号进地分隔返回
   */
  public String getValueListBySql(Connection conn, String sql);

  /**
   * 字段读出时进行解码的函数
   * @param fdValue 要解码的字段值
   * @param isxml 是否是xml字段中的数据，true表示是，false表示否
   * @return 返回解码后的字符串
   */
  public String deCode(String fdValue, boolean isxml);

  /**
   * 获得分页的sql语句
   * @param tableName 数据库表名
   * @param fieldStr 要选择的字段
   * @param whereSql 选择条件
   * @param orderField 排序字段，必须字段如果不传入排字段则报错
   * @param direction 排序方向
   * @param beginRow 开始行
   * @param addSize 每行显示行
   * @return 返回分页的sql语句
   */
  public String getPageSql(
          String tableName,
          String fieldStr,
          String whereSql,
          String orderField,
          String direction,
          long beginRow,
          int addSize);

  /**
   * 获取一个新链接的同时,自动获取一个 Statement， 该 Statement 已经设置数据集 可以滚动,可以更新
   * @return 如果获取失败将返回 null,调用时记得检查返回值
   */
  public Statement getStatement();

  /**
   * 获取一个 Statement 该 Statement 已经设置数据集 可以滚动,可以更新
   * @param conn 数据库连接
   * @return 如果获取失败将返回 null,调用时记得检查返回值
   */
  public Statement getStatement(Connection conn);

  /**
   * 获取一个带参数的 PreparedStatement 该 PreparedStatement 已经设置数据集 可以滚动,可以更新
   * @param sql 需要 ? 参数的 SQL 语句
   * @param cmdParams SQL 语句的参数表
   * @return 如果获取失败将返回 null,调用时记得检查返回值
   */
  public PreparedStatement getPreparedStatement(String sql, Object... cmdParams);

  /**
   * 获取一个带参数的 PreparedStatement 该 PreparedStatement 已经设置数据集 可以滚动,可以更新
   * @param conn 数据库连接
   * @param sql 需要 ? 参数的 SQL 语句
   * @param cmdParams SQL 语句的参数表
   * @return 如果获取失败将返回 null,调用时记得检查返回值
   */
  public PreparedStatement getPreparedStatement(Connection conn, String sql, Object... cmdParams);

  /**
   * 执行 SQL 语句,返回结果为整型 主要用于执行非查询语句如果执行select语句将报错
   * @param sql SQL 语句
   * @return 非负数:正常执行; -1:执行错误; -2:连接错误
   */
  public int execSql(String sql);

  /**
   * 执行 SQL 语句,返回结果为整型 主要用于执行非查询语句 ,如果执行select语句将报错
   * @param conn 指定数据库连接
   * @param sql SQL 语句
   * @return 非负数:正常执行; -1:执行错误; -2:连接错误
   */
  public int execSql(Connection conn, String sql);

  /**
   * 执行 SQL 语句,返回结果为整型 主要用于执行非查询语句 ,如果执行select语句将报错
   * @param sql 需要 ? 参数的 SQL 语句
   * @param cmdParams SQL 语句的参数表
   * @return 非负数:正常执行; -1:执行错误; -2:连接错误
   */
  public int execSql(String sql, Object... cmdParams);

  /**
   * 执行 SQL 语句,返回结果为整型 主要用于执行非查询语句 ,如果执行select语句将报错
   * @param conn 数据库连接
   * @param sql 需要 ? 参数的 SQL 语句
   * @param cmdParams SQL 语句的参数表
   * @return 非负数:正常执行; -1:执行错误; -2:连接错误
   */
  public int execSql(Connection conn, String sql, Object... cmdParams);

  /**
   * 返回一个 ResultSet
   * @param sql SQL 语句
   * @return 返回一个 ResultSet
   * @throws Exception 当执行sql出错时
   */
  public ResultSet getResultSet(String sql) throws Exception;

  /**
   * 返回一个 ResultSet
   * @param conn 指定数据库连接
   * @param sql SQL 语句
   * @return 返回一个 ResultSet
   * @throws Exception 当执行sql出错时
   */
  public ResultSet getResultSet(Connection conn, String sql) throws Exception;

  /**
   * 返回一个 ResultSet
   * @param sql 需要 ? 参数的 SQL 语句
   * @param cmdParams SQL 语句的参数表
   * @return 返回一个 ResultSet
   * @throws Exception 当执行sql出错时
   */
  public ResultSet getResultSet(String sql, Object... cmdParams) throws Exception;

  /**
   * 返回一个 ResultSet
   * @param conn 一个指定的数据库连接
   * @param sql 需要 ? 参数的 SQL 语句
   * @param cmdParams SQL 语句的参数表
   * @return 返回一个 ResultSet
   * @throws Exception 当执行sql出错时
   */
  public ResultSet getResultSet(Connection conn, String sql, Object... cmdParams) throws Exception;

  /**
   * 获得一个新的唯一ID号
   * @return 返回一个新的唯一ID号
   */
  public String getNewUnid();



  /**
   * 获得当前默认的数据库类型
   * @return 返回数据库类型MSSQL DB2 ORACLE MYSQL
   */
  public String getDbType();

  /**
   * 根据数据库链接对像获得数据库类型
   * @param conn 数据库链接对像如果传入null值表示获得默认的数据库链接类型
   * @return 返回数据库类型MSSQL ORACLE MYSQL
   */
  public String getDbType(Connection conn);

  /**
   * 获得指定数据库表的所有字段名称列表，数据库表中必须要有WF_OrUnid字段，否则会报错
   * @param tableName 数据库表名
   * @return 返回数据库表的所有字段组成的字符串数组,返回数据库表中真实的名称而不是select中指定的字段名称
   */
  public HashMap<String, String> getTableColumnName(String tableName);

  /**
   * 获得指定数据库表的所有字段名称列表，数据库表中必须要有WF_OrUnid字段，否则会报错
   * @param tableName 数据库表名
   * @param conn 指定数据库链接对像
   * @return 返回数据库表的所有字段组成Map 返回数据库表中真实的名称而不是select中指定的字段名称
   */
  public HashMap<String, String> getTableColumnName(Connection conn, String tableName);

  /**
   * 返回数据库表中真实的字段名称
   * @param rsMetaData 一个ResultSetMetaData
   * @return 返回数据库表的所有字段组成的字符串数组
   */
  public HashSet<String> getTableColumnName(ResultSetMetaData rsMetaData);

  /**
   * 获得指定数据库表的所有字段名称列表
   * @param rsMetaData 一个ResultSetMetaData
   * @param label true表示返回sql语句中指定的select中的 as的字段名，false表示返回数据库中真实的字段名称
   * @return 返回数据库表的所有字段组成的字符串数组
   */
  public HashSet<String> getTableColumnName(ResultSetMetaData rsMetaData, boolean label);

  /**
   * 判断数据库表是否存在
   * @param conn 数据库连接对象
   * @param tableName 数据库表名
   * @return 返回true表示存在，false表示不存在
   */
  public boolean isExistTable(Connection conn, String tableName);

  /**
   * 根据文档的WF_OrUnid和clob字段名称更新clob字段的数据
   * @param docUnid 文档的WF_OrUnid字段值
   * @param tableName 数据表名
   * @param fdName clob字段的id
   * @param fdValue 值
   * @return 返回true更新成功，返回false更新失败
   */
  public boolean saveClobField(String docUnid, String tableName, String fdName, String fdValue);

  /**
   * 根据文档的WF_OrUnid和clob字段名称更新clob字段的数据
   * @param conn 数据库链接
   * @param docUnid 文档的WF_OrUnid字段值
   * @param tableName 数据表名
   * @param fdName clob字段的id
   * @param fdValue 值
   * @return 返回true更新成功，返回false更新失败
   */
  public boolean saveClobField(
          Connection conn, String docUnid, String tableName, String fdName, String fdValue);



  /**
   * 格式化sql语句的参数，用来防止sql注入
   * @param args 需要格式化的sql参数
   * @return 返回格式化好的参数字符串
   */
  public String formatArg(String args);

  /**
   * 清空文档集合中的文档数据
   * @param dc 文档集合
   */
  public void cleardc(Set<Document> dc);

  /**
   * 清空文档集合中的文档数据
   * @param dc 文档集合
   */
  public void cleardc(Document[] dc);

  /**
   * 关闭指定的rs等数据库资源，而不是数据库链接，数据库链接需要用close()方法进行关闭
   * @param obj 数据库资源(Statement/PreparedStatement/ResultSet)
   */
  public void closers(Object obj);

  /**
   * 关闭指定的数据库链接
   * @param obj Connection Statement PreparedStatement三种类型的变量均可
   */
  public void close(Object obj);

  /**
   * 主动关闭线程链接对像
   * @param conn 数据库连接
   */
  public void close(Connection conn);

  /**
   * 通过id查询文档对象
   * @param tableName 表名称
   * @param WF_OrUnid 唯一ID
   * @return 返回一个Document对象
   */
  public Document getDocumentById(String tableName, String WF_OrUnid);
}
