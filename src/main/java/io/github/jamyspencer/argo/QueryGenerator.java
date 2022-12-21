package io.github.jamyspencer.argo;

import java.util.List;

public interface QueryGenerator<T> {

    default String generateBaseReadQuery(String tableName, List<ColumnHelper<T>> columnHelpers){
        StringBuilder builder = new StringBuilder("SELECT ");

        for (int i = 0; i < columnHelpers.size(); i++){
            ColumnHelper<T> helper = columnHelpers.get(i);
            builder.append(helper.getColumnName()).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(" FROM ").append(tableName);
        return builder.toString();
    }

    default String generateBaseInsertQuery(String tableName, List<ColumnHelper<T>> columnHelpers, String fk){
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(tableName).append(" (");

        for (int i = 0; i < columnHelpers.size(); i++){
            ColumnHelper<T> helper = columnHelpers.get(i);
            if(!ColumnHelper.ColumnType.GENERATED_ID.equals(helper.getColumnType())) {
                builder.append(helper.getColumnName()).append(",");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        if(fk != null && !fk.isEmpty()) {
            builder.append(",").append(fk);
        }
        builder.append(") VALUES ");
        return builder.toString();
    }

    default String generateBaseDeleteQuery(String tableName) {
        return "DELETE FROM " + tableName;
    }

    default String generateBaseUpdateQuery(String tableName) {
        return "UPDATE " + tableName + " SET ";
    }

    default String generateInsertParameters(FieldData fieldData){
        StringBuilder builder = new StringBuilder("(");
        for (int num = fieldData.numberOfInsertColumns();num > 0; num--) {
            builder.append("?,");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        return builder.toString();
    }

    default String getBatchInsertQuery(String baseQuery, String parameters, int size) {
        StringBuilder builder = new StringBuilder(baseQuery);
        for (; size > 0; size-- ){
            builder.append(parameters).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
