package io.github.jamyspencer.argo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DataCloser {
    private Logger log = LoggerFactory.getLogger(DataCloser.class);
    protected void close(Connection con){
        try {
            if (con != null && !con.isClosed()){
                con.setAutoCommit(true);
                con.close();
            }
        } catch (SQLException e) {
            log.error("Failed to close connection: {}", e);
        }
    }
    protected void close(PreparedStatement ps){
        try {
            if (ps != null && !ps.isClosed()){
                ps.close();
            }
        } catch (SQLException e) {
            log.error("Failed to close prepared statement: {}", e);
        }
    }
    protected void close(ResultSet rs){
        try {
            if (rs != null && !rs.isClosed()){
                rs.close();
            }
        } catch (SQLException e) {
            log.error("Failed to close result set: {}", e);
        }
    }
    protected void close(Connection con, PreparedStatement ps){
        close(con);
        close(ps);
    }
    protected void close(Connection con, PreparedStatement ps, ResultSet rs){
        close(con);
        close(ps);
        close(rs);
    }
}
