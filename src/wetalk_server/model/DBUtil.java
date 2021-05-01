package wetalk_server.model;

import wetalk_server.utils.Global;

import java.sql.*;

public class DBUtil {
    private final String dbUrl;
    private Connection conn = null;
    private Statement stmt = null;

    public DBUtil() {
        this.dbUrl = Global.getInstance().getProperty("dbUrl");
        this.conn = createConnection();  // try to connect the sqlite database, if .db file does not exist, create a new one in the root path.
        this.stmt = createStatement();
    }

    private Connection createConnection() {
        Connection conn = null;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(this.dbUrl);
            conn.setAutoCommit(false);
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        return conn;
    }

    private Statement createStatement() {
        Statement stmt = null;

        try{
            stmt = this.conn.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stmt;
    }

    // return the last insert row id
    private int execWithoutReturnData(String sql) {
        int rowID = 0;
        try{
            this.stmt.executeUpdate(sql);
            rowID = this.stmt.getGeneratedKeys().getInt(1);
            this.conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rowID;
    }

    // return the result set
    private ResultSet execWithReturnData(String sql) {
        ResultSet rs = null;
        try{
            rs = this.stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    // Initialize tables into the database
    public void initTables() {
        // TODO: initTables (Reflection)
    }

    // return the number of rows effected
    public int insert(String sql) {
        return this.execWithoutReturnData(sql);
    }

    // return the number of rows effected
    public int update(String sql) {
        return this.execWithoutReturnData(sql);
    }

    // return the number of rows effected
    public int delete(String sql) {
        return this.execWithoutReturnData(sql);
    }

    // return the result set
    public ResultSet select(String sql) {
        return this.execWithReturnData(sql);
    }

    // Close Connection and Statement if not null
    public void close() {
        try {
            if(this.stmt != null) {
                this.stmt.close();
            }

            if(this.conn != null) {
                this.conn.close();
            }
        } catch (SQLException e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

}
