package io.github.jamyspencer.argo;

import java.util.List;

public record FieldData<T>(
        List<ColumnHelper<T>> columnHelpers,
        List<RelationColumnHelper> relationColumnHelpers,
        RelationData relationData
) {
    public boolean hasGeneratedId(){
        return columnHelpers.stream()
                .map(helper -> ColumnHelper.ColumnType.GENERATED_ID.equals(helper.getColumnType()))
                .reduce(false, (a,b) -> a || b);
    }
    /**
     * @return int number of fields the Entity has, minus Generated fields
     * minus any Child Entity fields plus any foreign keys.
     */
    public int numberOfInsertColumns(){
        return columnHelpers.stream()
                .map(helper -> ColumnHelper.ColumnType.GENERATED_ID.equals(helper.getColumnType()) ? 0 :1)
                .reduce(0, (a,b) -> a + b) + (relationData.hasForeignKey() ? 1 : 0);
    }
}
