package cn.linkey.rule.util;

import java.sql.*;

/**
 * @author Administrator
 */
public class JdbcUtil {

    private static String driver="com.mysql.jdbc.Driver";
    private static String url="jdbc:mysql://127.0.0.1:3306/osbpm2?useUnicode=yes&useSSL=false&amp;characterEncoding=UTF8&amp;serverTimezone = GMT";
    private static String username="root";
    private static String password="950601";

    /**
     * 获取连接对象
     * @return Connection连接对象
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            //加载驱动
            Class.forName(driver);
            conn = DriverManager.getConnection(url,username,password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 关闭连接（Connection连接对象必须在最后关闭）
     * @param conn Connection连接对象
     * @param st 编译执行对象
     * @param rs 结果集
     */
    public static void close(Connection conn, PreparedStatement st, ResultSet rs){
        try {
            if(rs != null){
                rs.close();
            }
            if(st != null){
                st.close();
            }
            if(conn != null){
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * 返回一个 ResultSet
     *
     * @param sql SQL 语句
     * @return 返回一个 ResultSet
     * @throws Exception 当执行sql出错时
     */
    public static ResultSet getResultSet(String sql) throws Exception {
        Statement stmt = getStatement();
        if (stmt == null) {
            return null;
        }
        try {
            return stmt.executeQuery(sql);
        }
        catch (SQLException ex) {
            System.out.println("JdbcUtil.getResultSet执行(" + sql + ")报错!");
        }
        return null;
    }
    /**
     * 获取一个新链接的同时,自动获取一个 Statement， 该 Statement 已经设置数据集 可以滚动,可以更新
     *
     * @return 如果获取失败将返回 null,调用时记得检查返回值
     */
    public static Statement getStatement() {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) {
                System.out.println("JdbcUtil.getStatement()时数据库链接对像Connection为null值!");
                return null;
            }
            else if (conn.isClosed()) {
                System.out.println("JdbcUtil.getStatement()时数据库链接对像conn.isClosed()!");
                return null;
            }
            // 设置数据集可以滚动,可以更新
            return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        }
        catch (Exception ex) {
            close(conn,null,null);
            System.out.println("JdbcUtil.getStatement()获得Statement对像时出现异常!");
        }
        return null;
    }
    /**
     * 获取一个 Statement 该 Statement 已经设置数据集 可以滚动,可以更新
     *
     * @param conn 数据库连接
     * @return 如果获取失败将返回 null,调用时记得检查返回值
     */
    public static Statement getStatement(Connection conn) {
        if (conn == null) {
            return null;
        }
        try {
            return conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);// 设置数据集可以滚动,可以更新
        }
        catch (SQLException e) {
            close(conn,null,null);
            System.out.println("Statement获取错误");
            return null;
        }
    }


}
