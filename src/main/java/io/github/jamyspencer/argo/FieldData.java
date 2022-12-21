package io.github.jamyspencer.argo;

import java.util.List;

public record FieldData<T>(
        List<ColumnHelper<T>> columnHelpers,
        List<RelationColumnHelper> relationColumnHelpers
) {
    public boolean hasGeneratedId(){
        return columnHelpers.stream()
                .map(helper -> ColumnHelper.ColumnType.GENERATED_ID.equals(helper.getColumnType()))
                .reduce(false, (a,b) -> a || b);
    }
    public int numberOfInsertColumns(){
        return columnHelpers.stream()
                .map(helper -> ColumnHelper.ColumnType.GENERATED_ID.equals(helper.getColumnType()) ? 1 :0)
                .reduce(0, (a,b) -> a + b) + relationColumnHelpers.size();
    }
}
