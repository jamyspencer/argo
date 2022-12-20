package io.github.jamyspencer.argo;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.beans.BeanProperty;

import javax.annotation.processing.Generated;
import javax.persistence.Id;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class ColumnHelper<T> {

    private final String columnName;
    private final ColumnType columnType;
    private final BeanProperty<T, Object> beanProperty;
    private final FunctionalParameterSetter<T> statementSetter;
    private final FunctionalResultParser<T> resultParser;

    public ColumnHelper(BeanProperty<T, Object> beanProperty) {
        this.columnType = parseType(beanProperty);
        this.beanProperty = beanProperty;
        this.statementSetter = generateFunctionalParameterSetter(beanProperty);
        this.resultParser = generateFunctionalResultParser((beanProperty));
        this.columnName = parseColumnName(beanProperty);
    }

    public BeanProperty<T, Object> getBeanProperty() {
        return beanProperty;
    }

    public FunctionalParameterSetter<T> getStatementSetter() {
        return statementSetter;
    }

    public FunctionalResultParser<T> getResultParser() {
        return resultParser;
    }

    public String getColumnName() {
        return columnName;
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    enum ColumnType {
        ID, GENERATED_ID, PROPERTY
    }

    private String parseColumnName(BeanProperty<T, Object> beanProperty){
        return beanProperty.getName();
    }
    private ColumnType parseType(BeanProperty<T, Object> beanProperty){
        AnnotationValue<Id> id = beanProperty.getAnnotationMetadata().getAnnotation(Id.class);
        AnnotationValue<Generated> generated = beanProperty.getAnnotationMetadata().getAnnotation(Generated.class);

        ColumnType type = ColumnType.PROPERTY;
        if (id != null) {
            if (generated != null) {
                type = ColumnType.GENERATED_ID;
            }
            type = ColumnType.ID;
        }
        return type;
    }
    private FunctionalParameterSetter<T> generateFunctionalParameterSetter(BeanProperty<T, Object> property){
        if (String.class.equals(property.getType())) {
            return (PreparedStatement ps, int pos, T item) -> ps.setString(pos, (String) property.get(item));
        } else if (long.class.equals(property.getType())) {
            return (PreparedStatement ps, int pos, T item) -> ps.setLong(pos, (long) property.get(item));
        } else if (Long.class.equals(property.getType())) {
            return (PreparedStatement ps, int pos, T item) -> ps.setLong(pos, (Long) property.get(item));
        } else if (int.class.equals(property.getType())) {
            return (PreparedStatement ps, int pos, T item) -> ps.setInt(pos, (int) property.get(item));
        } else if (Integer.class.equals(property.getType())) {
            return (PreparedStatement ps, int pos, T item) -> ps.setInt(pos, (Integer) property.get(item));
        } else if (double.class.equals(property.getType())) {
            return (PreparedStatement ps, int pos, T item) -> ps.setDouble(pos, (double) property.get(item));
        } else if (Double.class.equals(property.getType())) {
            return (PreparedStatement ps, int pos, T item) -> ps.setDouble(pos, (Double) property.get(item));
        } else if (Date.class.equals(property.getType())) {
            return (PreparedStatement ps, int pos, T item) -> ps.setDate(pos, (Date) property.get(item));
        } else {
            return (PreparedStatement ps, int pos, T item) -> ps.setObject(pos, property.get(item));
        }
    }
    private FunctionalResultParser<T> generateFunctionalResultParser(BeanProperty<T, Object> property) {
        if (String.class.equals(property.getType())) {
            return (ResultSet rs, T item) -> property.set(item, rs.getString(columnName));
        } else if (Long.class.equals(property.getType()) || long.class.equals(property.getType())) {
            return (ResultSet rs, T item) -> property.set(item, rs.getLong(columnName));
        }  else if (Integer.class.equals(property.getType()) || int.class.equals(property.getType())) {
            return (ResultSet rs, T item) -> property.set(item, rs.getInt(columnName));
        }  else if (Double.class.equals(property.getType()) || double.class.equals(property.getType())) {
            return (ResultSet rs, T item) -> property.set(item, rs.getDouble(columnName));
        } else if (Date.class.equals(property.getType())) {
            return (ResultSet rs, T item) -> property.set(item, rs.getDate(columnName));
        } else {
            return (ResultSet rs, T item) -> property.set(item, rs.getObject(columnName));
        }
    }
}
