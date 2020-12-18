package cn.linkey.orm.dao.impl;

import cn.linkey.orm.dao.Rdb;
import cn.linkey.orm.doc.Document;
import cn.linkey.orm.doc.impl.DocumentImpl;
import cn.linkey.orm.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

public class RdbImpl implements Rdb {

    private static Logger logger = LoggerFactory.getLogger(RdbImpl.class);
    //数据库连接对象
    private Connection conn;

    /**
     * 获得数据库连接对象
     *
     * @return 数据库连接对象
     * @throws Exception 如果此方法获取数据库连接失败
     */
    @Override
    public Connection getConnection() throws Exception {
        if (conn == null) {
            logger.error("conn is null");
        }
        return conn;
    }

    /**
     * 初始化数据库链接对象
     *
     * @param conn 数据库连接对象
     */
    @Override
    public void setConnection(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Connection getNewConnection(String dataSourceid) throws Exception {
        return null;
    }

    /**
     * 开始事务
     *
     * @param autoCommit true：表示启动自动提交；false：表示禁用自动提交
     */
    @Override
    public void setAutoCommit(boolean autoCommit) {
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(autoCommit);
        } catch (Exception e) {
            System.out.println("事务开启出错!");
        }
    }

    /**
     * 获得事务自动提交标识
     *
     * @return true, false
     */
    @Override
    public boolean getAutoCommit() {
        try {
            Connection conn = getConnection();
            if (conn == null) {
                return true;
            } else {
                return conn.getAutoCommit();
            }
        } catch (Exception e) {
            System.out.println("Rdb.getAutoCommit()获得事务自动提交标识时出错!");
            return true;
        }
    }

    /**
     * 提交事务
     */
    @Override
    public void commit() {
        try {
            Connection conn = getConnection();
            if (conn != null && !conn.isClosed()) {
                conn.commit();
            }
        } catch (Exception e) {
            System.out.println("事务提交出错!");
        }
    }

    @Override
    public void commit(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.commit();
            } else {
                commit();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * 回滚事务
     */
    @Override
    public void rollback() {
        if (!getAutoCommit()) {
            try {
                Connection conn = getConnection();
                if (conn != null) {
                    System.out.println("准备回滚数据库!");
                    conn.rollback();
                } else {
                    System.out.println("试图回滚一个已经关闭的线程链接!");
                }
            } catch (Exception e) {
                System.out.println("事务回滚出错!");
            }
        } else {
            System.out.println("Rdb.setAutoCommit()数据库被设置为自动提交，不能进行数据库回滚操作!");
        }
    }

    /**
     * 分析SQl语句
     *
     * @param sql 转入的sql语句
     * @return 返回重新分析过的sql语句
     */
    @Override
    public String parserSql(String sql) {
       /* String debugSql = (String) BeanCtx.getCtxData("DEBUGSQL");
        if (debugSql != null && debugSql.equals("1")) {
            // 标识需要调试sql语句
            BeanCtx.log("D", sql);
        }*/
        return sql;
    }

    /**
     * 从sql语句中分析出tablename
     *
     * @param sql 要分析的sql语句
     * @return 返回表名称
     */
    @Override
    public String getTableNameFromSql(String sql) {
        int spos = sql.toLowerCase().indexOf("from");
        String endSql = Tools.trim(sql.substring(spos + 4));
        spos = endSql.indexOf(" ");
        String tableName = spos == -1 ? endSql : endSql.substring(0, spos);
        return tableName;
    }

    /**
     * 通过sql获取Document对象
     * 如果是多表的情况请使用getDocumentBySql(String tableName,String sql)方法
     *
     * @param sql 要执行的sql语句,表名将自动从sql语句中通过form关键字分析得到
     * @return Document对像
     */
    @Override
    public Document getDocumentBySql(String sql) {
        return getDocumentBySql(null, getTableNameFromSql(sql), sql);
    }

    /**
     * 通过sql获取Document对象
     * 如果是多表的情况请使用getDocumentBySql(String tableName,String sql)方法
     *
     * @param conn 数据库链接对像
     * @param sql  要执行的sql语句,表名将自动从sql语句中通过form关键字分析得到
     * @return Document对像
     */
    @Override
    public Document getDocumentBySql(Connection conn, String sql) {
        return getDocumentBySql(conn, getTableNameFromSql(sql), sql);
    }

    /**
     * 通过sql获取Document对象
     * 如果sql的select是多表或者只时选取不再回存时可以指定为空值
     *
     * @param sql       要执行的sql语句
     * @param tableName 数据所在表名
     * @return Document对像
     */
    @Override
    public Document getDocumentBySql(String tableName, String sql) {
        return getDocumentBySql(null, tableName, sql);
    }

    /**
     * 获得得文档集合数组
     *
     * @param sql 可以返回一条或多条记录的sql语句，表名将自动从sql语句中通过form关键字分析得到
     * @return 返回文档数组
     */
    @Override
    public Document[] getAllDocumentsBySql(String sql) {
        return getAllDocumentsBySql(null, getTableNameFromSql(sql), sql);
    }

    /**
     * 获得得文档集合数组
     *
     * @param tableName 数据库表名
     * @param sql       可以返回一条或多条记录的sql语句
     * @return 返回文档数组
     */
    @Override
    public Document[] getAllDocumentsBySql(String tableName, String sql) {
        return getAllDocumentsBySql(null, tableName, sql);
    }

    /**
     * 指定数据库链接并获得得文档集合数组
     * 如果sql的select是多表或者只时选取不再回存时可以指定为空值
     *
     * @param conn      数据库链接对像
     * @param tableName 数据库表名
     * @param sql       可以返回一条或多条记录的sql语句
     * @return 返回文档数组
     */
    @Override
    public Document[] getAllDocumentsBySql(Connection conn, String tableName, String sql) {
        LinkedHashSet<Document> dc = getAllDocumentsSetBySql(conn, tableName, sql);
        Document[] newdc = new Document[dc.size()];
        dc.toArray(newdc);
        return newdc;
    }

    /**
     * 指定数据库链接并获得得文档集合数组
     * 如果sql的select是多表或者只时选取不再回存时可以指定为空值
     *
     * @param conn      数据库链接对像
     * @param tableName 数据库表名
     * @param sql       可以返回一条或多条记录的sql语句
     * @param cmdParams cmdParams
     * @return 返回文档数组
     */
    @Override
    public Document[] getAllDocumentsBySql(Connection conn, String tableName, String sql, Object... cmdParams) {
        LinkedHashSet<Document> dc = getAllDocumentsSetBySql(conn, tableName, sql, cmdParams);
        Document[] newdc = new Document[dc.size()];
        dc.toArray(newdc);
        return newdc;
    }

    /**
     * 指定数据库链接并分页获得得文档集合数组
     * 性能慢于getAllDocumentsByPage但本方法的数据库兼容性比较好
     *
     * @param sql      可以返回一条或多条记录的sql语句
     * @param pageNum  当前页数，第一页传1
     * @param pageSize 每页显示数如：25
     * @return 返回文档数组
     */
    @Override
    public Document[] getAllDocumentsBySql(String sql, int pageNum, int pageSize) {
        return getAllDocumentsBySql(null, "", sql, pageNum, pageSize);
    }

    /**
     * 指定数据库链接并分页获得得文档集合数组
     * 性能慢于getAllDocumentsByPage但本方法的数据库兼容性比较好
     * 如果sql的select是多表或者只时选取不再回存时可以指定为空值
     *
     * @param tableName 数据库表名
     * @param sql       可以返回一条或多条记录的sql语句
     * @param pageNum   当前页数，第一页传1
     * @param pageSize  每页显示数如：25
     * @return 返回文档数组
     */
    @Override
    public Document[] getAllDocumentsBySql(String tableName, String sql, int pageNum, int pageSize) {
        return getAllDocumentsBySql(null, tableName, sql, pageNum, pageSize);
    }

    /**
     * 指定数据库链接并分页获得得文档集合数组
     * 性能慢于getAllDocumentsByPage但本方法的数据库兼容性比较好
     *
     * @param conn      数据库链接对像
     * @param tableName 数据库表名
     * @param sql       可以返回一条或多条记录的sql语句
     * @param pageNum   当前页数，第一页传1
     * @param pageSize  每页显示数如：25
     * @return 返回文档数组
     */
    @Override
    public Document[] getAllDocumentsBySql(Connection conn, String tableName, String sql, int pageNum, int pageSize) {
        sql = parserSql(sql);
        Document[] docCollection;
        ResultSet rs = null;
        try {
            if (conn == null) {
                conn = getConnection();
            }
            PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int i = 0;
            if (pageNum < 1)
                pageNum = 1;
            int maxCount = pageNum * pageSize;
            int startNo = (pageNum - 1) * pageSize;
            pstmt.setMaxRows(maxCount); // 设置最大查询行数
            rs = pstmt.executeQuery();
            rs.last(); // 移动到最后一行为了获得记录总数用来计算实际输出总数，总数用来设置数组的大小
            int outNum = rs.getRow() - startNo; // 实际输出总数
            if (outNum < 0) {
                outNum = 0;
            }
            docCollection = new Document[outNum]; // 获得得到的实际行数作为数组个数
            // 为了兼容oracle改成如下格式
            if (startNo < 1) {
                rs.absolute(1); // 第一行数为1
                rs.previous();
            } else {
                rs.absolute(startNo); // 第一行数为1
            }
            // 兼容性修改结束
            while (rs.next()) {
                Document doc = new DocumentImpl(tableName, conn);
                doc.appendFromResultSet(rs);
                docCollection[i] = doc;
                i++;
            }
            return docCollection;
        } catch (Exception e) {
            System.out.println("getAllDocumentsBySql获得文档集合时出错(SQL=" + sql + ")!");
        } finally {
            closers(rs);
        }
        return null;
    }

    /**
     * 获得得文档集合set,集合中可以保证sql语句获得的结果顺序
     *
     * @param sql 可以返回一条或多条记录的sql语句
     * @return 返回set集合
     */
    @Override
    public LinkedHashSet<Document> getAllDocumentsSetBySql(String sql) {
        return getAllDocumentsSetBySql(null, getTableNameFromSql(sql), sql);
    }

    /**
     * 获得得文档集合LinkedHashSet，集合中可以保证sql语句获得的结果顺序
     *
     * @param tableName 数据库表名
     * @param sql       可以返回一条或多条记录的sql语句
     * @return 返回set集合
     */
    @Override
    public LinkedHashSet<Document> getAllDocumentsSetBySql(String tableName, String sql) {
        return getAllDocumentsSetBySql(null, tableName, sql);
    }

    /**
     * 指定数据库链接并获得得文档集合set，集合中可以保证sql语句获得的结果顺序
     *
     * @param conn      数据库链接对像
     * @param tableName 数据库表名
     * @param sql       可以返回一条或多条记录的sql语句
     * @return 返回set集合
     */
    @Override
    public LinkedHashSet<Document> getAllDocumentsSetBySql(Connection conn, String tableName, String sql) {
        sql = parserSql(sql);
        LinkedHashSet<Document> docCollection = new LinkedHashSet<Document>();
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            while (rs.next()) {
                Document doc = new DocumentImpl(tableName, conn);
                doc.appendFromResultSet(rs);
                docCollection.add(doc);
            }
        } catch (Exception e) {
            System.out.println("获得文档集合时出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return docCollection;
    }

    /**
     * 指定数据库链接并获得得文档集合set，集合中可以保证sql语句获得的结果顺序
     *
     * @param conn      数据库链接对像
     * @param tableName 数据库表名
     * @param sql       可以返回一条或多条记录的sql语句
     * @param cmdParams SQL中带?号时的替换参数
     * @return 返回set集合
     */
    @Override
    public LinkedHashSet<Document> getAllDocumentsSetBySql(Connection conn, String tableName, String sql, Object... cmdParams) {
        sql = parserSql(sql);
        LinkedHashSet<Document> docCollection = new LinkedHashSet<Document>();
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql, cmdParams);
            } else {
                rs = getResultSet(conn, sql, cmdParams);
            }
            while (rs.next()) {
                Document doc = new DocumentImpl(tableName, conn);
                doc.appendFromResultSet(rs);
                docCollection.add(doc);
            }
        } catch (Exception e) {
            System.out.println("获得文档集合时出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return docCollection;
    }

    /**
     * 指定数据库链接并分页获得得文档集合set，集合中可以保证sql语句获得的结果顺序
     *
     * @param tableName  数据库表名
     * @param fieldList  要选择的字段全选可以*号
     * @param orderByStr 要排序的字段,必须传入参数,格式为:order by fd1,fd2 desc
     * @param whereSql   选择条件语句,格式为: where fd1=1 and fd2 &gt; 10
     * @param pageNum    当前页数第一页传1
     * @param pageSize   每页显示数如：25
     * @return 返回LinkedHashSet集合
     */
    @Override
    public LinkedHashSet<Document> getAllDocumentsSetByPage(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize) {
        return getAllDocumentsSetByPage(null, tableName, fieldList, orderByStr, whereSql, pageNum, pageSize);
    }

    /**
     * 指定数据库链接并分页获得得文档集合set，集合中可以保证sql语句获得的结果顺序
     *
     * @param conn       数据库链接对像
     * @param tableName  数据库表名
     * @param fieldList  指定要选择的列全部返回传入*号
     * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
     * @param whereSql   选择条件格式为:where fd1=1 and fd2&gt;10
     * @param pageNum    当前页，第一页传1
     * @param pageSize   每页显示数如：25
     * @return 返回LinkedHashSet集合
     */
    @Override
    public LinkedHashSet<Document> getAllDocumentsSetByPage(Connection conn, String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize) {
        LinkedHashSet<Document> docCollection = new LinkedHashSet<Document>();
        ResultSet rs = null;
        String sql = "";
        try {
            if (conn == null) {
                conn = getConnection();// 获得一个默认链接
                if (conn == null) {
                    return null; // 无法获得数据库链接对像
                }
            }
            if (Tools.isBlank(orderByStr)) {
                orderByStr = "order by WF_OrUnid";
            }
            if (getDbType(conn).equals("ORACLE")) {
                sql = getOraclePageSql(tableName, fieldList, orderByStr, whereSql, pageNum, pageSize);
            } else if (getDbType(conn).equals("MSSQL")) {
                sql = getMSSQLPageSql(tableName, fieldList, orderByStr, whereSql, pageNum, pageSize);
            } else if (getDbType(conn).equals("MYSQL")) {
                sql = getMYSQLPageSql(tableName, fieldList, orderByStr, whereSql, pageNum, pageSize);
            } else if (getDbType(conn).equals("PGSQL")) {
                sql = getPGSQLPageSql(tableName, fieldList, orderByStr, whereSql, pageNum, pageSize);
            }
            sql = parserSql(sql);
            rs = getResultSet(conn, sql);
            while (rs.next()) {
                Document doc = new DocumentImpl("", conn);
                doc.appendFromResultSet(rs);
                docCollection.add(doc);
            }
        } catch (Exception e) {
            System.out.println("Rdb.getAllDocumentsSetBySql()获得文档集合时出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return docCollection;
    }

    /**
     * 获得oracle的分页查询语句
     *
     * @param tableName  数据库表名
     * @param fieldList  指定要选择的列全部返回传入*号
     * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
     * @param whereSql   选择条件格式为: where fd1=1 and fd2大于10
     * @param pageNum 当前页，第一页传1
     * @param pageSize 每页显示数如：25
     * @return 分页查询结果
     */
    @Override
    public String getOraclePageSql(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize) {
        int beginRow = (pageNum - 1) * pageSize; // oracle这里不用+1
        int endRow = pageNum * pageSize;
        StringBuilder sql = new StringBuilder();
        if (fieldList.equals("*")) {
            fieldList = "a.*";
        }
        sql.append("select * from (select t.*,rownum rn from(select ").append(fieldList).append(" from ").append(tableName).append(" a ").append(whereSql).append("  ").append(orderByStr)
                .append(" ) t where rownum<").append(endRow).append(") where rn>=").append(beginRow);
        return sql.toString();
    }

    /**
     * 获得Ms SqlServer的分页查询语句
     *
     * @param tableName  数据库表名
     * @param fieldList  指定要选择的列全部返回传入*号
     * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
     * @param whereSql   选择条件格式为:where fd1=1 and fd2大于10
     * @param pageNum 当前页，第一页传1
     * @param pageSize 每页显示数如：25
     * @return 分页查询结果
     */
    @Override
    public String getMSSQLPageSql(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize) {
        orderByStr = "over(" + orderByStr + ")";
        int beginRow = (pageNum - 1) * pageSize + 1; // sql server这里要+1
        int endRow = pageNum * pageSize;
        if (fieldList.equals("*")) {
            fieldList = "t.*";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("select * from (select ");
        sql.append(fieldList);
        sql.append(",row_number() ");
        sql.append(orderByStr);
        sql.append(" as RowNumber from ");
        sql.append(tableName);
        sql.append(" as t ");
        sql.append(whereSql);
        sql.append(") a where RowNumber between ");
        sql.append(beginRow);
        sql.append(" and ");
        sql.append(endRow);
        return sql.toString();
    }

    /**
     * 获得My SQL的分页查询语句
     *
     * @param tableName  数据库表名
     * @param fieldList  指定要选择的列全部返回传入*号
     * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
     * @param whereSql   选择条件格式为:where fd1=1 and fd2大于10
     * @param pageNum 当前页，第一页传1
     * @param pageSize 每页显示数如：25
     * @return 分页查询结果
     */
    @Override
    public String getMYSQLPageSql(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize) {
        int beginRow = (pageNum - 1) * pageSize; // my sql这里不需要加1
        String sql = "select   " + fieldList + " from " + tableName + " " + whereSql + " " + orderByStr + " limit " + beginRow + "," + pageSize;
        return sql;
    }

    /**
     * 获得PostgreSQL的分页查询语句
     *
     * @param tableName  数据库表名
     * @param fieldList  指定要选择的列全部返回传入*号
     * @param orderByStr 必须传入参数,格式为:order by fd1,fd2 desc
     * @param whereSql   选择条件格式为:where fd1=1 and fd2大于10
     * @param pageNum    当前页，第一页传1
     * @param pageSize   每页显示数如：25
     * @return 分页查询结果
     */
    @Override
    public String getPGSQLPageSql(String tableName, String fieldList, String orderByStr, String whereSql, int pageNum, int pageSize) {
        int beginRow = (pageNum - 1) * pageSize;
        String sql = "select   " + fieldList + " from " + tableName + " " + whereSql + " " + orderByStr + " limit " + pageSize + " offset " + beginRow;
        return sql;
    }

    /**
     * 通过sql获取Document对象
     *
     * @param conn      指定数据库链接对像
     * @param sql       要执行的sql语句
     * @param tableName 数据所在表名
     * @return Document 返回文档对像,要记得关闭conn链接
     */
    @Override
    public Document getDocumentBySql(Connection conn, String tableName, String sql) {
        sql = parserSql(sql);
        Document doc = new DocumentImpl(tableName, conn);
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            if (rs == null) {
                System.out.println("Rdb.getDocumentBySql()获得ResultSet时出错(" + sql + ")!");
                return null;
            }
            if (rs.next()) {
                doc.appendFromResultSet(rs);
            } else {
                doc.setIsNull();
            }
        } catch (Exception e) {
            System.out.println("获得指定链接的文档时出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return doc;
    }

    /**
     * 已有文档实例对像，再根据sql语句进行初始化数据到文档中去
     *
     * @param doc 要进行初始化的文档对像
     * @param sql 要执行的sql语句
     * @return Document要进行数据初始化的文档对像
     */
    @Override
    public Document appendDataFromSql(Document doc, String sql) {
        return appendDataFromSql(null, doc, sql);
    }

    /**
     * 已有文档实例对像，再根据sql语句进行初始化数据到文档中去
     *
     * @param conn 指定数据库链接
     * @param doc  要进行初始化的文档对像
     * @param sql  要执行的sql语句
     * @return Document 返回初始化后的文档对像
     */
    @Override
    public Document appendDataFromSql(Connection conn, Document doc, String sql) {
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            if (rs.next()) {
                doc.appendFromResultSet(rs);
            } else {
                doc.setIsNull();
            }
        } catch (Exception e) {
            System.out.println("获得文档时出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return doc;
    }

    /**
     * 获得运行sql获取的第一条数据
     *
     * @param sql sql语句
     * @return 返回运行sql获取的第一条数据
     */
    @Override
    public String getValueTopOneBySql(String sql) {
        sql = parserSql(sql);
        ResultSet rs = null;
        try {
            rs = getResultSet(sql);
            if (rs.next()) {
                String valStr = rs.getString(1);
                if (valStr == null) {
                    return "";
                } else {
                    return valStr;
                }
            }
        } catch (Exception e) {
            System.out.println("Rdb.getValueTopOneBySql执行(" + sql + ")时出错!");
        } finally {
            closers(rs);
        }
        return "";
    }

    /**
     * 直接通过sql语句返回得到一个字段的值
     * 只能是select一个字段如：select subject form tablename
     *
     * @param sql 要执行的sql语句、
     * @return 返回字符串，如果select 多条记录时字段值使用,逗号进地分隔返回
     */
    @Override
    public String getValueBySql(String sql) {
        return getValueBySql(null, sql);
    }

    /**
     * 直接通过sql语句返回一条记录或者多条记录的某个字段的值
     * 如果字段中的值本身有逗号，则会把逗号分隔成set的成员
     * 本方法不保证sql语句获得记录的排序结果
     * 如果要排序请使用getValueLinkedSetBySql()方法
     *
     * @param sql 要执行的sql语句如：select subject form tablename
     * @return 返回set集合，会去掉重复值和空值
     */
    @Override
    public HashSet<String> getValueSetBySql(String sql) {
        return getValueSetBySql(null, sql, false);
    }

    /**
     * 直接通过sql语句返回一条记录或者多条记录的某个字段的值
     * 如果字段中的值本身有逗号则会把逗号分隔成set的成员
     * 本方法不保证sql语句获得记录的排序结果
     * 如果要排序请使用getValueLinkedSetBySql()方法
     *
     * @param sql        要执行的sql语句如：select subject form tablename
     * @param splitComma 字段值中的的逗号是否进行split成多个值加入到set集合中;true：表示按逗号(,)split;false：表示否
     * @return 返回set集合，会去掉重复值和空值
     */
    @Override
    public HashSet<String> getValueSetBySql(String sql, boolean splitComma) {
        return getValueSetBySql(null, sql, splitComma);
    }

    /**
     * 直接通过sql语句返回得到一个字段的值
     * 如果字段中的值本身有逗号则会把逗号分隔成set的成员
     * 本方法不保证sql语句获得记录的排序结果
     * 如果要排序请使用getValueLinkedSetBySql()方法
     *
     * @param sql        要执行的sql语句如：select subject form tablename
     * @param conn       指定数据库链接对像
     * @param splitComma 字段值中的的逗号是否进行split成多个值加入到set集合中;true：表示按逗号(,)split;false：表示否
     * @return 返回set集合，会去掉重复值和空值
     */
    @Override
    public HashSet<String> getValueSetBySql(Connection conn, String sql, boolean splitComma) {
        sql = parserSql(sql);
        HashSet<String> fdValue = new HashSet<String>();
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            while (rs.next()) {
                if (splitComma) {
                    fdValue.addAll(Tools.splitAsList(rs.getString(1), ","));
                } else {
                    fdValue.add(rs.getString(1));
                }
            }
        } catch (Exception e) {
            System.out.println("Rdb.getValueSetBySql执行(" + sql + ")时出错!");
        } finally {
            closers(rs);
        }
        fdValue.remove("");// 去掉空值
        return fdValue;
    }

    /**
     * 直接通过sql语句返回一条记录或者多条记录的某个字段的值
     * 如果字段中的值本身有逗号则会把逗号分隔成set的成员
     * 本方法可以保证sql语句查询结果按顺序保存到set集合中
     *
     * @param sql 要执行的sql语句如：select subject form tablename
     * @return 返回set集合，会去掉重复值和空值
     */
    @Override
    public LinkedHashSet<String> getValueLinkedSetBySql(String sql) {
        return getValueLinkedSetBySql(null, sql, false);
    }

    /**
     * 直接通过sql语句返回一条记录或者多条记录的某个字段的值
     * 如果字段中的值本身有逗号则会把逗号分隔成set的成员
     * 本方法可以保证sql语句查询结果按顺序保存到set集合中
     *
     * @param sql        要执行的sql语句如：select subject form tablename
     * @param splitComma 字段值中的的逗号是否进行split成多个值加入到set集合中; true：表示按逗号(,)split;false表示否
     * @return 返回set集合，会去掉重复值和空值
     */
    @Override
    public LinkedHashSet<String> getValueLinkedSetBySql(String sql, boolean splitComma) {
        return getValueLinkedSetBySql(null, sql, splitComma);
    }

    /**
     * 直接通过sql语句返回得到一个字段的值,如果字段中的值本身有逗号则会把逗号分隔成set的成员 本方法可以保证sql语句查询结果按顺序保存到set集合中
     *
     * @param conn       指定数据库链接对像
     * @param sql        要执行的sql语句如：select subject form tablename
     * @param splitComma 字段值中的的逗号是否进行split成多个值加入到set集合中true表示按,号split，false表示否
     * @return 返回set集合，会去掉重复值和空值
     */
    @Override
    public LinkedHashSet<String> getValueLinkedSetBySql(Connection conn, String sql, boolean splitComma) {
        sql = parserSql(sql);
        LinkedHashSet<String> fdValue = new LinkedHashSet<String>();
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            while (rs.next()) {
                if (splitComma) {
                    fdValue.addAll(Tools.splitAsList(rs.getString(1), ","));
                } else {
                    fdValue.add(rs.getString(1));
                }
            }
        } catch (Exception e) {
            System.out.println("Rdb.getValueSetBySql执行(" + sql + ")时出错!");
        } finally {
            closers(rs);
        }
        fdValue.remove("");// 去掉空值
        return fdValue;
    }

    /**
     * 直接通过sql语句返回得到一个由Text和Value组成的字符串如： 张三|U003
     *
     * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename Subject为Text,id为值
     * @return 返回字符串，如果select 多条记录时字段值使用,逗号进地分隔返回
     */
    @Override
    public String getValueListBySql(String sql) {
        return getValueListBySql(null, sql);
    }

    /**
     * 根据sql语句返回select标签所要求的字符串格式即text|value,test|value格式
     *
     * @param sql          指定sql语句只能也必须选择两列 select text,value from tablename
     * @param defaultValue 指定默认选中的项的值
     * @return 返回字符串text|value,text1|value1.....
     */
    @Override
    public String getValueForSelectTagBySql(String sql, String defaultValue) {
        return getValueForSelectTagBySql(null, sql, defaultValue);
    }

    /**
     * 根据sql语句返回select标签所要求的字符串格式即text|value,test|value格式
     *
     * @param conn         指定数据库链接对像
     * @param sql          指定sql语句只能也必须选择两列 select text,value from tablename
     * @param defaultValue 指定默认选中的项的值
     * @return 返回字符串text|value,text1|value1,text2|value2|selected.....
     */
    @Override
    public String getValueForSelectTagBySql(Connection conn, String sql, String defaultValue) {
        sql = parserSql(sql);
        StringBuilder vStr = new StringBuilder();
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            while (rs.next()) {
                String selectedStr = "";
                String text = rs.getString(1);
                String value = rs.getString(2);
                if (value.equals(defaultValue)) {
                    selectedStr = "|selected";
                }
                if (vStr.length() == 0) {
                    vStr.append(text + "|" + value + selectedStr);
                } else {
                    vStr.append("," + text + "|" + value + selectedStr);
                }
            }
        } catch (Exception e) {
            System.out.println("getMapDataBySql出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return vStr.toString();
    }

    /**
     * 直接通过sql语句返回第一列和第二列的HashMap对像,select可以是多条结果记录 不保持sql语句记录的顺序,特点查询速度快
     *
     * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename
     * @return 返回map对像
     */
    @Override
    public HashMap<String, String> getMapDataBySql(String sql) {
        return getMapDataBySql(null, sql);
    }

    /**
     * 直接通过sql语句返回第一列和第二列的HashMap对像,select可以是多条结果记录 不保持sql语句记录的顺序,特点查询速度快
     *
     * @param conn 数据库链接对像,conn链接对像用完后需要自已关闭回收资源
     * @param sql  要执行的sql语句，可以选择两个字段如：select subject,id form tablename Subject为Text,id为值
     * @return 返回map对像
     */
    @Override
    public HashMap<String, String> getMapDataBySql(Connection conn, String sql) {
        sql = parserSql(sql);
        HashMap<String, String> valueMap = new HashMap<String, String>();
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            while (rs.next()) {
                valueMap.put(rs.getString(1), rs.getString(2));
            }
        } catch (Exception e) {
            System.out.println("Rdb.getMapDataBySql出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return valueMap;
    }

    /**
     * 直接通过sql语句返回第一列和第二列的HashMap对像,select可以是多条结果记录 可以保持sql语句的记录顺序,特点可以保持数据的顺序
     *
     * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename
     * @return 返回map对像
     */
    @Override
    public LinkedHashMap<String, String> getLinkedMapDataBySql(String sql) {
        return getLinkedMapDataBySql(null, sql);
    }

    /**
     * 直接通过sql语句返回第一列和第二列的HashMap对像,select可以是多条结果记录 可以保持sql语句的记录顺序,特点可以保持数据的顺序
     *
     * @param conn 数据库链接对像,conn链接对像用完后需要自已关闭回收资源
     * @param sql  要执行的sql语句，可以选择两个字段如：select subject,id form tablename Subject为Text,id为值
     * @return 返回map对像
     */
    @Override
    public LinkedHashMap<String, String> getLinkedMapDataBySql(Connection conn, String sql) {
        sql = parserSql(sql);
        LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            while (rs.next()) {
                valueMap.put(rs.getString(1), rs.getString(2));
            }
        } catch (Exception e) {
            System.out.println("Rdb.getLinkedMapDataBySql出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return valueMap;
    }

    /**
     * 直接通过sql语句返回第一列和第二列的HashMap对像,select只能返回唯一一条的记录
     *
     * @param sql 要执行的sql语句，可以选择两个字段如：select subject,id form tablename where id=1
     * @return 返回map对像
     */
    @Override
    public HashMap<String, String> getOneMapDataBySql(String sql) {
        return getOneMapDataBySql(null, sql);
    }

    /**
     * 直接通过sql语句返回第一列和第二列的HashMap对像,select只能返回唯一一条的记录
     *
     * @param conn 数据库链接对像,conn链接对像用完后需要自已关闭回收资源
     * @param sql  要执行的sql语句，可以选择两个字段如：select subject,id form tablename where id=1
     * @return 返回map对像
     */
    @Override
    public HashMap<String, String> getOneMapDataBySql(Connection conn, String sql) {
        sql = parserSql(sql);
        HashMap<String, String> map = new HashMap<String, String>();
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            HashSet<String> fdList = getTableColumnName(rs.getMetaData(), true); // 需要使用select中指定的名称
            if (rs.next()) {
                for (String fdName : fdList) {
                    String fdValue = (String) rs.getString(fdName);
                    map.put(fdName, fdValue);
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("getOneMapDataBySql->获得MapData时出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return map;
    }

    /**
     * 注意：HashMap是不能重复key的，所以 getAllmapDataBysql返回的值的key是sql中指定的字段，值是是所有记录的用逗号相加的结果 循环sql语句中的字段，并把所有记录的字段值用逗号进行相加，存入到map对像中返回
     *
     * @param sql select语句支持返回多条记录的sql语句如：select id,name from table
     * @return 返回key为字段名的map对像, map对像中的key为select语句中指定的字段名称，而不是数据库表中的字段名称
     */
    @Override
    public HashMap<String, String> getAllMapDataBySql(String sql) {
        return getAllMapDataBySql(null, sql);
    }

    /**
     * 注意：HashMap是不能重复key的，所以 getAllmapDataBysql返回的值的key是sql中指定的字段，值是是所有记录的用逗号相加的结果 循环sql语句中的字段，并把所有记录的字段值用逗号进行相加，存入到map对像中返回
     *
     * @param conn 数据库链接对像
     * @param sql  select语句支持返回多条记录的sql语句如：select id,name from table
     * @return 返回key为字段名的map对像, map对像中的key为select语句中指定的字段名称，而不是数据库表中的字段名称
     */
    @Override
    public HashMap<String, String> getAllMapDataBySql(Connection conn, String sql) {
        sql = parserSql(sql);
        HashMap<String, String> map = new HashMap<String, String>();
        ResultSet rs = null;
        StringBuilder[] fdValue = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            HashSet<String> fdList = getTableColumnName(rs.getMetaData(), true); // 需要使用select中指定的名称
            fdValue = new StringBuilder[fdList.size()];
            int i = 0;
            while (rs.next()) {
                int x = 0;
                for (String fdName : fdList) {
                    if (fdValue[x] == null) {
                        fdValue[x] = new StringBuilder();
                    }
                    if (i == 0) {
                        fdValue[x].append(rs.getString(fdName));
                    } else {
                        fdValue[x].append("," + rs.getString(fdName));
                    }
                    x++;
                }
                i = 1;
            }
            // 把StringBuilder加入到map对像中去
            int y = 0;
            for (String fdName : fdList) {
                if (fdValue[y] != null) {
                    map.put(fdName, fdValue[y].toString());
                } else {
                    map.put(fdName, "");
                }
                y++;
            }
        } catch (Exception e) {
            System.out.println("getAllMapDataBySql->获得MapData时出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return map;
    }

    /**
     * 判断记录是否存在
     *
     * @param sql 要判断是否存在的sql语句
     * @return true表示存在, false表示不存在
     */
    @Override
    public boolean hasRecord(String sql) {
        return hasRecord(null, sql);
    }

    /**
     * 判断记录是否存在
     *
     * @param conn 指定数据库链接
     * @param sql  要判断是否存在的sql语句
     * @return true表示存在, false表示不存在
     */
    @Override
    public boolean hasRecord(Connection conn, String sql) {
        sql = parserSql(sql);
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            System.out.println("getCountBySql出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return false;
    }

    /**
     * 获得指定sql语句返回的记录数,常用于判断记录是否存在
     *
     * @param sql sql语句如：select * from table where id=1
     * @return 0表示记录不存在 否则返回记录数
     */
    @Override
    public int getCountBySql(String sql) {
        return getCountBySql(null, sql);
    }

    /**
     * 获得指定sql语句返回的记录数
     *
     * @param conn 指定数据库链接，链接用完后要手动关闭
     * @param sql  sql语句如：select * from table where id=1
     * @return 0表示记录不存在 否则返回记录数
     */
    @Override
    public int getCountBySql(Connection conn, String sql) {
        sql = parserSql(sql);
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            if (rs.next()) {
                rs.last();
                return rs.getRow();
            } else {
                return 0;
            }
        } catch (Exception e) {
            System.out.println("Rdb.getCountBySql出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return 0;
    }

    /**
     * 指定数据库链接并直接通过sql语句返回得到一个字段的值
     *
     * @param conn 指定链接
     * @param sql  要执行的sql语句，只能是select一个字段如：select subject form tablename where id=1
     * @return 返回字符串，如果select 多条记录时字段值使用,逗号进地分隔返回
     */
    @Override
    public String getValueBySql(Connection conn, String sql) {
        sql = parserSql(sql);
        StringBuilder fdValue = new StringBuilder();
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            int i = 0;
            while (rs.next()) {
                if (i == 0) {
                    fdValue.append(rs.getString(1));
                    i = 1;
                } else {
                    fdValue.append("," + rs.getString(1));
                }
            }
        } catch (Exception e) {
            System.out.println("getValueBySql出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return fdValue.toString();
    }

    /**
     * 直接通过sql语句返回得到一个由Text和Value组成的字符串如： 张三|U003
     *
     * @param conn 指定链接
     * @param sql  要执行的sql语句，可以选择两个字段如：select subject,id form tablename Subject为Text,id为值
     * @return 返回字符串，如果select 多条记录时字段值使用,逗号进地分隔返回
     */
    @Override
    public String getValueListBySql(Connection conn, String sql) {
        sql = parserSql(sql);
        StringBuilder fdList = new StringBuilder();
        ResultSet rs = null;
        int i = 0;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            while (rs.next()) {
                if (i == 0) {
                    fdList.append(rs.getString(1) + "|" + rs.getString(2));
                    i = 1;
                } else {
                    fdList.append("," + rs.getString(1) + "|" + rs.getString(2));
                }
            }
        } catch (Exception e) {
            System.out.println("getValueListBySql出错(" + sql + ")!");
        } finally {
            closers(rs);
        }
        return fdList.toString();
    }

    /**
     * 字段读出时进行解码的函数
     *
     * @param fdValue 要解码的字段值
     * @param isxml   是否是xml字段中的数据，true表示是，false表示否
     * @return 返回解码后的字符串
     */
    @Override
    public String deCode(String fdValue, boolean isxml) {
        if (isxml) {// 只有xmldata字段才有必要进行&解码
            fdValue = fdValue.replace("&amp;", "&");
        }
        fdValue = fdValue.replace("&lt;", "<");
        fdValue = fdValue.replace("&gt;", ">");
        return fdValue;
    }

    /**
     * 获得分页的sql语句
     *
     * @param tableName  数据库表名
     * @param fieldStr   要选择的字段
     * @param whereSql   选择条件
     * @param orderField 排序字段，必须字段如果不传入排字段则报错
     * @param direction  排序方向
     * @param beginRow   开始行
     * @param addSize    每行显示行
     * @return 返回分页的sql语句
     */
    @Override
    public String getPageSql(String tableName, String fieldStr, String whereSql, String orderField, String direction, long beginRow, int addSize) {
        String sql = "";
        if (Tools.isBlank(tableName)) {
            System.out.println("getPageSql数据表库表名不能为空!");
            return "";
        }
        if (Tools.isNotBlank(whereSql)) {
            if (whereSql.toLowerCase().indexOf("where") == -1) {
                whereSql = " where " + whereSql;
            }
        }
        String orderStr = "";
        if (Tools.isNotBlank(orderField)) {
            orderStr = "(order by " + orderField + " " + direction + ")";
        }
        // 组合开始到结束的数字
        long endRow;
        if (beginRow <= 1) {
            beginRow = 1;
            endRow = beginRow + addSize - 1;
        } else {
            beginRow = beginRow + 1;
            endRow = beginRow + addSize - 1;
        }
        if (fieldStr.equals("*")) {
            if (getDbType() == "ORACLE") {
                sql = "select * from (select t.*,row_number() over " + orderStr + " as RowNumber from " + tableName + " t " + whereSql + ") a where RowNumber between " + beginRow + " and " + endRow;
            } else {
                sql = "select * from (select t.*,row_number() over " + orderStr + " as RowNumber from " + tableName + " as t " + whereSql + ") a where RowNumber between " + beginRow + " and "
                        + endRow;
            }
        } else {
            sql = "select " + fieldStr + " from (select " + fieldStr + ",row_number() over " + orderStr + " as RowNumber from " + tableName + " " + whereSql + ") a where RowNumber between " + beginRow
                    + " and " + endRow;
        }
        return sql;
    }

    /**
     * 获取一个新链接的同时,自动获取一个 Statement， 该 Statement 已经设置数据集 可以滚动,可以更新
     *
     * @return 如果获取失败将返回 null,调用时记得检查返回值
     */
    @Override
    public Statement getStatement() {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) {
                System.out.println("Rdb.getStatement()时数据库链接对像Connection为null值!");
                return null;
            } else if (conn.isClosed()) {
                System.out.println("Rdb.getStatement()时数据库链接对像conn.isClosed()!");
                return null;
            }
            return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY); // 设置数据集可以滚动,可以更新
        } catch (Exception ex) {
            System.out.println("Rdb.getStatement()获得Statement对像时出现异常!");
            ex.printStackTrace();
            close(conn);
        }
        return null;
    }

    /**
     * 获取一个 Statement 该 Statement 已经设置数据集 可以滚动,可以更新
     *
     * @param conn 数据库连接
     * @return 如果获取失败将返回 null,调用时记得检查返回值
     */
    @Override
    public Statement getStatement(Connection conn) {
        if (conn == null) {
            return null;
        }
        try {
            return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);// 设置数据集可以滚动,可以更新
        } catch (SQLException e) {
            close(conn);
            System.out.println("Statement获取错误");
            return null;
        }
    }

    /**
     * 获取一个带参数的 PreparedStatement 该 PreparedStatement 已经设置数据集 可以滚动,可以更新
     *
     * @param sql       需要 ? 参数的 SQL 语句
     * @param cmdParams SQL 语句的参数表
     * @return 如果获取失败将返回 null,调用时记得检查返回值
     */
    @Override
    public PreparedStatement getPreparedStatement(String sql, Object... cmdParams) {
        PreparedStatement pstmt = null;
        try {
            if (conn == null) {
                return null;
            }
            pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int i = 1;
            for (Object item : cmdParams) {
                pstmt.setObject(i, item);
                i++;
            }
        } catch (Exception e) {
            close(conn);
            e.printStackTrace();
        }
        return pstmt;
    }

    /**
     * 获取一个带参数的 PreparedStatement 该 PreparedStatement 已经设置数据集 可以滚动,可以更新
     *
     * @param conn      数据库连接
     * @param sql       需要 ? 参数的 SQL 语句
     * @param cmdParams SQL 语句的参数表
     * @return 如果获取失败将返回 null,调用时记得检查返回值
     */
    @Override
    public PreparedStatement getPreparedStatement(Connection conn, String sql, Object... cmdParams) {
        if (conn == null) {
            return null;
        }
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int i = 1;
            for (Object item : cmdParams) {
                pstmt.setObject(i, item);
                i++;
            }
        } catch (SQLException e) {
            close(conn);
            e.printStackTrace();
        }
        return pstmt;
    }

    /**
     * 执行 SQL 语句,返回结果为整型 主要用于执行非查询语句如果执行select语句将报错
     *
     * @param sql SQL 语句
     * @return 非负数:正常执行; -1:执行错误; -2:连接错误
     */
    @Override
    public int execSql(String sql) {
        sql = parserSql(sql);
        Statement stmt = getStatement();
        if (stmt == null) {
            return -2;
        }
        int i;
        try {
            i = stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            System.out.println("Rdb.execSql(" + sql + ")时出错,请检查sql语句是否正确,注意本方法禁止执行select语句!");
            i = -1;
        } finally {
            closers(stmt);
        }
        return i;
    }

    /**
     * 执行 SQL 语句,返回结果为整型 主要用于执行非查询语句 ,如果执行select语句将报错
     *
     * @param conn 指定数据库连接
     * @param sql  SQL 语句
     * @return 非负数:正常执行; -1:执行错误; -2:连接错误
     */
    @Override
    public int execSql(Connection conn, String sql) {
        sql = parserSql(sql);
        Statement stmt = getStatement(conn);
        if (stmt == null) {
            return -2;
        }
        int i;
        try {
            i = stmt.executeUpdate(sql);
        } catch (SQLException ex) {
            System.out.println("Rdb.execSql(" + sql + ")时出错,请检查sql语句是否正确,注意本方法禁止执行select语句!");
            i = -1;
        } finally {
            closers(stmt);
        }
        return i;
    }

    /**
     * 执行 SQL 语句,返回结果为整型 主要用于执行非查询语句 ,如果执行select语句将报错
     *
     * @param sql       需要 ? 参数的 SQL 语句
     * @param cmdParams SQL 语句的参数表
     * @return 非负数:正常执行; -1:执行错误; -2:连接错误
     */
    @Override
    public int execSql(String sql, Object... cmdParams) {
        sql = parserSql(sql);
        PreparedStatement pstmt = getPreparedStatement(sql, cmdParams);
        if (pstmt == null) {
            return -2;
        }
        int i;
        try {
            i = pstmt.executeUpdate();
        } catch (SQLException ex) {
            //BeanCtx.log(ex, "E", "执行(" + sql + ")报错!");
            i = -1;
        } finally {
            closers(pstmt);
        }
        return i;
    }

    /**
     * 执行 SQL 语句,返回结果为整型 主要用于执行非查询语句 ,如果执行select语句将报错
     *
     * @param conn      数据库连接
     * @param sql       需要 ? 参数的 SQL 语句
     * @param cmdParams SQL 语句的参数表
     * @return 非负数:正常执行; -1:执行错误; -2:连接错误
     */
    @Override
    public int execSql(Connection conn, String sql, Object... cmdParams) {
        sql = parserSql(sql);
        PreparedStatement pstmt = getPreparedStatement(conn, sql, cmdParams);
        if (pstmt == null) {
            return -2;
        }
        int i;
        try {
            i = pstmt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("执行(" + sql + ")报错!");
            i = -1;
        } finally {
            closers(pstmt);
        }
        return i;
    }

    /**
     * 返回一个 ResultSet
     *
     * @param sql SQL 语句
     * @return 返回一个 ResultSet
     * @throws Exception 当执行sql出错时
     */
    @Override
    public ResultSet getResultSet(String sql) throws Exception {
        Statement stmt = getStatement();
        if (stmt == null)
            return null;
        try {
            return stmt.executeQuery(sql);
        } catch (SQLException ex) {
            System.out.println("Rdb.getResultSet执行(" + sql + ")报错!");
        }
        return null;
    }

    /**
     * 返回一个 ResultSet
     *
     * @param conn 指定数据库连接
     * @param sql  SQL 语句
     * @return 返回一个 ResultSet
     * @throws Exception 当执行sql出错时
     */
    @Override
    public ResultSet getResultSet(Connection conn, String sql) throws Exception {
        Statement stmt = getStatement(conn);
        if (stmt == null) {
            return null;
        }
        return stmt.executeQuery(sql);
    }

    /**
     * 返回一个 ResultSet
     *
     * @param sql       需要 ? 参数的 SQL 语句
     * @param cmdParams SQL 语句的参数表
     * @return 返回一个 ResultSet
     * @throws Exception 当执行sql出错时
     */
    @Override
    public ResultSet getResultSet(String sql, Object... cmdParams) throws Exception {
        PreparedStatement pstmt = getPreparedStatement(sql, cmdParams);
        if (pstmt == null) {
            return null;
        }
        return pstmt.executeQuery();
    }

    /**
     * 返回一个 ResultSet
     *
     * @param conn      一个指定的数据库连接
     * @param sql       需要 ? 参数的 SQL 语句
     * @param cmdParams SQL 语句的参数表
     * @return 返回一个 ResultSet
     * @throws Exception 当执行sql出错时
     */
    @Override
    public ResultSet getResultSet(Connection conn, String sql, Object... cmdParams) throws Exception {
        PreparedStatement pstmt = getPreparedStatement(conn, sql, cmdParams);
        if (pstmt == null) {
            return null;
        }
        return pstmt.executeQuery();
    }

    /**
     * 获得一个新的唯一ID号
     *
     * @return 返回一个新的唯一ID号
     */
    @Override
    public String getNewUnid() {
        String unid = UUID.randomUUID().toString();
        unid = unid.replace("-", "0");// .substring(0,32);
        return unid;
    }

    /**
     * 获得当前默认的数据库类型
     *
     * @return 返回数据库类型MSSQL DB2 ORACLE MYSQL
     */
    @Override
    public String getDbType() {
        String dbType = "";
        // 根据当前的链接取数据库类型
        try {
            dbType = getConnection().getMetaData().getDatabaseProductName();
            if (dbType.equals("Microsoft SQL Server")) {
                dbType = "MSSQL";
            } else if (dbType.equals("MySQL")) {
                dbType = "MYSQL";
            } else if (dbType.equals("PostgreSQL")) {
                dbType = "PGSQL";
            } else {
                dbType = "ORACLE";
            }
        } catch (Exception e) {
            return "MSSQL";
        }
        return dbType;
    }

    /**
     * 根据数据库链接对像获得数据库类型
     *
     * @param conn 数据库链接对像如果传入null值表示获得默认的数据库链接类型
     * @return 返回数据库类型MSSQL ORACLE MYSQL
     */
    @Override
    public String getDbType(Connection conn) {
        // 根据当前的链接取数据库类型
        try {
            if (conn == null) {
                conn = getConnection(); // 获得缺省的链接对像
            }
            String dbType = conn.getMetaData().getDatabaseProductName();
            if (dbType.equals("Microsoft SQL Server")) {
                dbType = "MSSQL";
            } else if (dbType.equals("MySQL")) {
                dbType = "MYSQL";
            } else if (dbType.equals("PostgreSQL")) {
                dbType = "PGSQL";
            } else {
                dbType = "ORACLE";
            }
            return dbType;
        } catch (Exception e) {
            System.out.println("Rdb.getDbType(conn)获取数据库类型时出错!");
            return "MSSQL";
        }
    }

    /**
     * 获得指定数据库表的所有字段名称列表，数据库表中必须要有WF_OrUnid字段，否则会报错
     *
     * @param tableName 数据库表名
     * @return 返回数据库表的所有字段组成的字符串数组, 返回数据库表中真实的名称而不是select中指定的字段名称
     */
    @Override
    public HashMap<String, String> getTableColumnName(String tableName) {
        return getTableColumnName(null, tableName);
    }

    /**
     * 获得指定数据库表的所有字段名称列表，数据库表中必须要有WF_OrUnid字段，否则会报错
     *
     * @param tableName 数据库表名
     * @param conn      指定数据库链接对像
     * @return 返回数据库表的所有字段组成Map 返回数据库表中真实的名称而不是select中指定的字段名称
     */
    @Override
    public HashMap<String, String> getTableColumnName(Connection conn, String tableName) {
        HashMap<String, String> columns = new HashMap<String, String>();
        String sql = "";
        // 直接从数据库中取字段的所有类型
        String dbType = getDbType(conn);
        if (dbType.equals("MSSQL")) {
            sql = "select top 1 * from " + tableName;
        } else if (dbType.equals("MYSQL")) {
            sql = "select * from " + tableName + " limit 1";
        } else if (dbType.equals("ORACLE")) {
            sql = "select * from " + tableName + " where rownum = 1";
        } else if (dbType.equals("PGSQL")) {
            sql = "select * from " + tableName + " limit 1 offset 0";
        }
        ResultSet rs = null;
        try {
            if (conn == null) {
                rs = getResultSet(sql);
            } else {
                rs = getResultSet(conn, sql);
            }
            int colCount = rs.getMetaData().getColumnCount();
            // 读取字段名到数组
            for (int i = 1; i <= colCount; i++) {
                columns.put(rs.getMetaData().getColumnName(i), rs.getMetaData().getColumnTypeName(i));
            }
        } catch (Exception e) {
            System.out.println("Rdb.getTableColumnName(" + sql + ")获得数据库表的字段列表时出错!");
        } finally {
            closers(rs);
        }
        return columns;
    }

    /**
     * 返回数据库表中真实的字段名称
     *
     * @param rsMetaData 一个ResultSetMetaData
     * @return 返回数据库表的所有字段组成的字符串数组
     */
    @Override
    public HashSet<String> getTableColumnName(ResultSetMetaData rsMetaData) {
        return getTableColumnName(rsMetaData, false);
    }

    /**
     * 获得指定数据库表的所有字段名称列表
     *
     * @param rsMetaData 一个ResultSetMetaData
     * @param label      true表示返回sql语句中指定的select中的 as的字段名，false表示返回数据库中真实的字段名称
     * @return 返回数据库表的所有字段组成的字符串数组
     */
    @Override
    public HashSet<String> getTableColumnName(ResultSetMetaData rsMetaData, boolean label) {
        // 基本上不花时间，速度很快
        if (rsMetaData == null) {
            System.out.println("rs无效，请检查执行的sql语句是否有错误!");
            return null;
        }
        HashSet<String> columns = null;
        try {
            int colCount = rsMetaData.getColumnCount();
            columns = new HashSet<String>(colCount);
            // 读取字段名到数组
            for (int i = 1; i <= colCount; i++) {
                if (label) {
                    columns.add(rsMetaData.getColumnLabel(i)); // select中指定的名称
                } else {
                    columns.add(rsMetaData.getColumnName(i)); // 真实名称
                }
            }
        } catch (Exception e) {
            System.out.println("获得数据库表的字段列表时出错!");
        }
        return columns;
    }

    /**
     * 判断数据库表是否存在
     *
     * @param conn      数据库连接对象
     * @param tableName 数据库表名
     * @return 返回true表示存在，false表示不存在
     */
    @Override
    public boolean isExistTable(Connection conn, String tableName) {
        boolean isExist = false;
        String sql = "";
        try {
            String dbType = getDbType(conn);
            if (dbType.equals("MSSQL")) {
                sql = "select name from sysobjects where id = object_id('" + tableName + "')";
                String sqltableName = getValueBySql(conn, sql);
                if (Tools.isNotBlank(sqltableName)) {
                    isExist = true;
                }
            } else if (dbType.equals("MYSQL")) {
                // mysql判断数据库表是否存在
                String dbName = "";
                if (conn == null) {
                    dbName = getConnection().getCatalog();// 取得默认的数据库链接对像
                } else {
                    dbName = conn.getCatalog();
                }
                sql = "select `TABLE_NAME` from `INFORMATION_SCHEMA`.`TABLES` where `TABLE_SCHEMA`='" + dbName + "' and `TABLE_NAME`='" + tableName + "'";
                String sqltableName = getValueBySql(conn, sql);
                if (Tools.isNotBlank(sqltableName)) {
                    isExist = true;
                }
            } else if (dbType.equals("ORACLE")) {
                sql = "select count(*) from tabs where table_name ='" + tableName.toUpperCase() + "'";
                String sqltableName = getValueBySql(conn, sql);
                if (!sqltableName.equals("0")) {
                    isExist = true;
                }
            } else if (dbType.equals("PGSQL")) {
                sql = "select relname from pg_stat_user_tables where relname = '" + tableName.toLowerCase() + "'";
                String sqltableName = getValueBySql(conn, sql);
                if (Tools.isNotBlank(sqltableName)) {
                    isExist = true;
                }
            }
        } catch (Exception e) {
            System.out.println("判断数据库表是否存在时出错！");
            System.out.println(sql);
        } finally {
            if (conn != null) {
                close(conn);// 只有是新建链接的时候才能关闭，否则把当前线程的链接给关了后面就会报错
            }
        }
        return isExist;
    }

    /**
     * 根据文档的WF_OrUnid和clob字段名称更新clob字段的数据
     *
     * @param docUnid   文档的WF_OrUnid字段值
     * @param tableName 数据表名
     * @param fdName    clob字段的id
     * @param fdValue   值
     * @return 返回true更新成功，返回false更新失败
     */
    @Override
    public boolean saveClobField(String docUnid, String tableName, String fdName, String fdValue) {
        try {
            return saveClobField(getConnection(), docUnid, tableName, fdName, fdValue);
        } catch (Exception e) {
            System.out.println("更新clob字段时出错!");
            return false;
        }
    }

    /**
     * 根据文档的WF_OrUnid和clob字段名称更新clob字段的数据
     *
     * @param conn      数据库链接
     * @param docUnid   文档的WF_OrUnid字段值
     * @param tableName 数据表名
     * @param fdName    clob字段的id
     * @param fdValue   值
     * @return 返回true更新成功，返回false更新失败
     */
    @Override
    public boolean saveClobField(Connection conn, String docUnid, String tableName, String fdName, String fdValue) {
        // 更新clob字段
        try {
            String usql = "select " + fdName + " from " + tableName + " where WF_OrUnid = '" + docUnid + "' for update";
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery(usql);
            if (rs.next()) {
                // 更新所有clob字段的内容
                Clob c = rs.getClob("XMLDATA");
                c.truncate(0);// clear
                c.setString(1, fdValue);
            }
            closers(rs);
            return true;
        } catch (Exception e) {
            System.out.println("删除文档时出错clob字段更新失败!");
            return false;
        }
    }

    /**
     * 格式化sql语句的参数，用来防止sql注入
     *
     * @param args 需要格式化的sql参数
     * @return 返回格式化好的参数字符串
     */
    @Override
    public String formatArg(String args) {
        if (args == null) {
            return null;
        }
        args = args.replace("'", "''");
        return args;
    }

    /**
     * 清空文档集合中的文档数据
     *
     * @param dc 文档集合
     */
    @Override
    public void cleardc(Set<Document> dc) {
        for (Document doc : dc) {
            doc.clear();
            doc = null;
        }
    }

    /**
     * 清空文档集合中的文档数据
     *
     * @param dc 文档集合
     */
    @Override
    public void cleardc(Document[] dc) {
        for (Document doc : dc) {
            doc.clear();
            doc = null;
        }
    }

    /**
     * 关闭指定的rs等数据库资源，而不是数据库链接，数据库链接需要用close()方法进行关闭
     *
     * @param obj 数据库资源(Statement/PreparedStatement/ResultSet)
     */
    @Override
    public void closers(Object obj) {
        if (obj == null) {
            return;
        }
        try {
            if (obj instanceof Statement) {
                ((Statement) obj).close();
            } else if (obj instanceof PreparedStatement) {
                ((PreparedStatement) obj).close();
            } else if (obj instanceof ResultSet) {
                try {
                    ((ResultSet) obj).getStatement().close();
                } catch (Exception e) {
                    System.out.println("ResultSet已经关闭，获取Result的Statement时出错，如果是mysql可以通过设置retainStatementAfterResultSetClose=true来解决");
                } finally {
                    ((ResultSet) obj).close();
                }
            }
        } catch (SQLException ex) {
            System.out.println("关闭数据库Result资源时出错!");
        }
    }

    /**
     * 关闭指定的数据库链接
     *
     * @param obj Connection Statement PreparedStatement三种类型的变量均可
     */
    @Override
    public void close(Object obj) {
        if (obj == null) {
            return;
        }
        try {
            if (obj instanceof Statement) {
                ((Statement) obj).getConnection().close();
            } else if (obj instanceof PreparedStatement) {
                ((PreparedStatement) obj).getConnection().close();
            } else if (obj instanceof ResultSet) {
                ((ResultSet) obj).getStatement().getConnection().close();
            } else if (obj instanceof Connection) {
                // 此处不再处理关闭conn链接，交给使用都来处理关闭
                // ((Connection) obj).close();
            }
        } catch (SQLException ex) {
            System.out.println("指定数据库链接对像关闭失败!");
        }
    }

    /**
     * 主动关闭线程链接对像
     */
    @Override
    public void close(Connection conn) {
        // 此处不再处理关闭conn链接，交给使用都来处理关闭
//        try {
//            if(conn!=null){
//                conn.close();
//            }
//        } catch (SQLException e) {
//            System.out.println("数据库链接关闭出错!");
//        }
    }

    /**
     * 通过id查询文档对象
     *
     * @param tableName 表名称
     * @param WF_OrUnid 唯一ID
     * @return 返回一个Document对象
     */
    @Override
    public Document getDocumentById(String tableName, String WF_OrUnid) {
        String sql = "select * from " + tableName + " where WF_ORUNID = '" + WF_OrUnid + "'";
        Document doc = getDocumentBySql(sql);
        return doc;
    }
}
