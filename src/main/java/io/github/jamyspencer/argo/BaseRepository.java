package io.github.jamyspencer.argo;

import io.github.jamyspencer.argo.annotations.ChildEntity;
import io.github.jamyspencer.argo.annotations.RelationMapping;
import io.micronaut.context.*;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Table;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Introspected
public abstract class BaseRepository<T> {
    @Inject
    ApplicationContext applicationContext;
    private BeanIntrospection<T> beanIntrospection;
    private final List<RelationColumnHelper> childEntities = new ArrayList<>();
    private final List<ColumnHelper<T>> tableProperties = new ArrayList<>();
    private String tableName;
    private String foreignKey;
    private final String baseInsertQuery;
    private final static Logger logger = LoggerFactory.getLogger(BaseRepository.class);
    public BaseRepository(Class<T> entityType) {
        processEntityData(entityType);
        baseInsertQuery = generateBaseInsertQuery(tableName, tableProperties, foreignKey);
    }
    public List<T> find() {
        return null;
    }
    private void runChildQueries(Connection con, Collection<T> payload) throws SQLException{
        if (!childEntities.isEmpty()){
            for (RelationColumnHelper helper: childEntities){
                BaseRepository delegate = applicationContext.getBean(helper.getDelegate());
                Map<Object, Collection> allChildren = new TreeMap<>();

                Iterator<T> iterator = payload.iterator();
                while (iterator.hasNext()){
                    T item = iterator.next();
                    allChildren.put(getIdProperty().get(item) ,(Collection) helper.getBeanProperty().get(item));
                }
                logger.info("Built child treemap {}", allChildren);
                delegate.save(con, allChildren);
            }
        }
    }
    public void save(Connection con, Collection<T> payload) throws SQLException{
        runBatchedQuery(con, generateBatchInsertQuery(payload.size()), payload, foreignKey);
        runChildQueries(con, payload);
    }
    public void save(Connection con, Map<Object, Collection<T>> payload) throws SQLException{
        int size = payload.values().stream().map(Collection::size).reduce(Integer::sum).orElse(0);
        String query = generateBatchInsertQuery(size);
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
            logger.error("{}", e);
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                logger.error("Exception thrown during rollback attempt");
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
            for (ColumnHelper columnHelper: tableProperties){
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
            logger.error("{}",e);
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                logger.error("Exception thrown during rollback attempt");
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
        for(ColumnHelper columnHelper: tableProperties){
            if (ColumnHelper.ColumnType.ID.equals(columnHelper.getColumnType()) || ColumnHelper.ColumnType.GENERATED_ID.equals(columnHelper.getColumnType())){
                return columnHelper.getBeanProperty();
            }
        }
        throw new RuntimeException("Failed to find id for relation");
    }
    private void processEntityData(Class<T> entityType){

        beanIntrospection = BeanIntrospection.getIntrospection(entityType);
        Table[] tableData = beanIntrospection.synthesizeDeclaredAnnotationsByType(Table.class);
        if(tableData.length > 0){
            tableName = tableData[0].name();
            if(tableData[0].schema() != null && !tableData[0].schema().isBlank()) {
                tableName = tableData[0].schema() + "." + tableName;
            }
        } else {
            tableName = entityType.getSimpleName();
        }

        RelationMapping[] relationMapping = beanIntrospection.synthesizeDeclaredAnnotationsByType(RelationMapping.class);
        if (relationMapping.length > 0) {
            foreignKey = relationMapping[0].foreignKey();
        }
        Collection<BeanProperty<T, Object>> properties = beanIntrospection.getBeanProperties();
        properties.forEach(property -> {

            ChildEntity val = property.getAnnotationMetadata().synthesize(ChildEntity.class);
            if (val != null){
                childEntities.add(new RelationColumnHelper(property, val));
            } else{
                tableProperties.add(new ColumnHelper(property));
            }
        });
    }

    protected abstract Connection getConnection() throws SQLException;
    public String getInsertQuery(){
        return baseInsertQuery + getParameters();
    }
    private String getParameters(){
        StringBuilder builder = new StringBuilder("(");
        int num = tableProperties.size();
        if (foreignKey != null) num++;
        for (;num > 0; num--) builder.append("?,");
        builder.deleteCharAt(builder.length()-1);
        builder.append("),");
        return builder.toString();
    }
    public String getReadQuery(){
        return "SELECT " + tableProperties.stream().map(property -> property.getColumnName()).collect(Collectors.joining(",")) + " FROM " + tableName;
    }
    private String generateBaseInsertQuery(String tableName, List<ColumnHelper<T>> properties, String fk){
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(tableName).append("(");
        for (ColumnHelper<T> property: tableProperties) {
            builder.append(property.getColumnName()).append(",");
        }
        if(fk != null && !fk.isEmpty()) {
            builder.append(fk);
        } else {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(") VALUES");
        return builder.toString();
    }
    protected String generateBatchInsertQuery(int length){
        StringBuilder builder = new StringBuilder(baseInsertQuery);
        builder.append(getParameters().repeat(length));
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
    public void populateBatchPreparedStatement(PreparedStatement ps, Collection<T> payload, int start, Object fk) throws SQLException{
        int colWidth = tableProperties.size();
        boolean hasForeignKey = foreignKey != null && !(foreignKey.isBlank());
        if (hasForeignKey) colWidth++;

        for (int i = 0; i < colWidth; i++){
            int p = (start * colWidth) + i + 1;
            if (i == colWidth -1 && hasForeignKey) {
                for (int j = 0; j < payload.size(); p = p + colWidth, j++) {
                    ps.setLong(p, (Long) fk);
                }
            } else {
                ColumnHelper<T> helper = tableProperties.get(i);
                Iterator<T> iterator = payload.iterator();
                while (iterator.hasNext()){
                    helper.getStatementSetter().set(ps, p, iterator.next());
                    p = p + colWidth;
                }
            }
        }
    }
    public void populateBatchPreparedStatement(PreparedStatement ps, Collection<T> payload, Object fk) throws SQLException{
        populateBatchPreparedStatement( ps, payload, 0, fk);
    }
    protected void close(Connection con){
        try {
            if (con != null && !con.isClosed()){
                con.setAutoCommit(true);
                con.close();
            }
        } catch (SQLException e) {
            logger.error("Failed to close connection: {}", e);
        }
    }
    protected void close(PreparedStatement ps){
        try {
            if (ps != null && !ps.isClosed()){
                ps.close();
            }
        } catch (SQLException e) {
            logger.error("Failed to close prepared statement: {}", e);
        }
    }
    protected void close(ResultSet rs){
        try {
            if (rs != null && !rs.isClosed()){
                rs.close();
            }
        } catch (SQLException e) {
            logger.error("Failed to close result set: {}", e);
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
