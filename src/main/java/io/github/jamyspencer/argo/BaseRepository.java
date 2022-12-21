package io.github.jamyspencer.argo;

import io.micronaut.context.*;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Introspected
public abstract class BaseRepository<T> extends DataCloser implements QueryGenerator<T>, EntityAnnotationProcessor<T>{
    @Inject
    ApplicationContext applicationContext;
    private BeanIntrospection<T> beanIntrospection;
    private final FieldData<T> fieldData;
    private String tableName;
    private final String baseInsertQuery;
    private final String insertParameters;
    private final static Logger log = LoggerFactory.getLogger(BaseRepository.class);
    public BaseRepository(Class<T> entityType) {
        beanIntrospection = BeanIntrospection.getIntrospection(entityType);
        fieldData = processEntityData(beanIntrospection);
        tableName = getTableName(beanIntrospection, entityType);
        baseInsertQuery = generateBaseInsertQuery(tableName, fieldData.columnHelpers(), fieldData.relationData().foreignKey());
        insertParameters = generateInsertParameters(fieldData);
    }
    public List<T> find() {
        return null;
    }
    private void runChildQueries(Connection con, Collection<T> payload) throws SQLException{
        if (!fieldData.relationColumnHelpers().isEmpty()){
            for (RelationColumnHelper helper: fieldData.relationColumnHelpers()){
                BaseRepository delegate = applicationContext.getBean(helper.getDelegate());
                Map<Object, Collection> allChildren = new TreeMap<>();

                Iterator<T> iterator = payload.iterator();
                while (iterator.hasNext()){
                    T item = iterator.next();
                    allChildren.put(getIdProperty().get(item) ,(Collection) helper.getBeanProperty().get(item));
                }
                log.info("Built child treemap {}", allChildren);
                delegate.save(con, allChildren);
            }
        }
    }
    public void save(Connection con, Collection<T> payload) throws SQLException{
        String query = getBatchInsertQuery(baseInsertQuery, insertParameters, payload.size());
        runBatchedQuery(con, query, payload, fieldData.relationData().foreignKey());
        runChildQueries(con, payload);
    }
    public void save(Connection con, Map<Object, Collection<T>> payload) throws SQLException{
        int size = payload.values().stream().map(Collection::size).reduce(Integer::sum).orElse(0);
        String query = getBatchInsertQuery(baseInsertQuery, insertParameters, size);
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(query);
            int acc = 0;
            Iterator<Object> iterator = payload.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Collection<T> value = payload.get(key);
                populateBatchPreparedStatement(ps, value, acc, key);
                acc += value.size();
            }
            ps.execute();
            iterator = payload.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                runChildQueries(con, payload.get(key));
            }
        }finally {
            close(ps);
        }
    }
    private List<T> runQueryWithReturnValue(String query){
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            return parseResultSet(rs);
        } catch (SQLException e){
            log.error("{}", e);
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                log.error("Exception thrown during rollback attempt");
            }
        } finally {
            close(con, ps, rs);
        }
        return null;
    }
    protected List<T> parseResultSet(ResultSet rs) throws SQLException{
        List<T> val = new ArrayList<>();

        while (rs.next()){
            T item = (T) beanIntrospection.getConstructor().instantiate();
            for (ColumnHelper columnHelper: fieldData.columnHelpers()){
                columnHelper.getResultParser().parse(rs, item);
            }
        }
        return val;
    }
    public void save(List<T> payload){
        Connection con = null;
        try {
            con = getConnection();
            con.setAutoCommit(false);
            save(con, payload);
            con.commit();
        } catch (SQLException e){
            log.error("{}",e);
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                log.error("Exception thrown during rollback attempt");
            }
        } finally {
            close(con);
        }
    }
    protected void runBatchedQuery(Connection con, String query, Collection<T> payload, Object foreignKey) throws SQLException{
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(query);
            populateBatchPreparedStatement(ps, payload, foreignKey);
            ps.execute();
        }finally {
            close(ps);
        }
    }

    private BeanProperty getIdProperty(){
        for(ColumnHelper columnHelper: fieldData.columnHelpers()){
            if (ColumnHelper.ColumnType.ID.equals(columnHelper.getColumnType()) || ColumnHelper.ColumnType.GENERATED_ID.equals(columnHelper.getColumnType())){
                return columnHelper.getBeanProperty();
            }
        }
        throw new RuntimeException("Failed to find id for relation");
    }


    protected abstract Connection getConnection() throws SQLException;
    public String getInsertQuery(){
        return baseInsertQuery + insertParameters;
    }
    public void populateBatchPreparedStatement(PreparedStatement  ps, Collection<T> payload, int start, Object fk) throws SQLException{
        int colWidth = fieldData.numberOfInsertColumns();

        for (int i = 0; i < colWidth; i++){
            int p = (start * colWidth) + i + 1;
            if (i == colWidth -1 && fk != null) {
                for (int j = 0; j < payload.size(); p = p + colWidth, j++) {
                    ps.setLong(p, (Long) fk);
                }
            } else {
                ColumnHelper<T> helper = fieldData.columnHelpers().get(i);
                Iterator<T> iterator = payload.iterator();
                while (iterator.hasNext()){
                    helper.getStatementSetter().set(ps, p, iterator.next());
                    p = p + colWidth;
                }
            }
        }
        log.info("{}", ps);
    }
    public void populateBatchPreparedStatement(PreparedStatement ps, Collection<T> payload, Object fk) throws SQLException{
        populateBatchPreparedStatement( ps, payload, 0, fk);
    }
}
