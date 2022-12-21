package io.github.jamyspencer.argo;

import io.github.jamyspencer.argo.annotations.ChildEntity;
import io.github.jamyspencer.argo.annotations.RelationMapping;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;

import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface EntityAnnotationProcessor<T> {
    default FieldData<T> processEntityData(BeanIntrospection<T> beanIntrospection){

        Collection<BeanProperty<T, Object>> properties = beanIntrospection.getBeanProperties();
        List<ColumnHelper<T>> columnHelpers = new ArrayList<>();
        List<RelationColumnHelper> relationColumnHelpers = new ArrayList<>();
        properties.forEach(property -> {
            ChildEntity val = property.getAnnotationMetadata().synthesize(ChildEntity.class);
            if (val != null){
                relationColumnHelpers.add(new RelationColumnHelper(property, val));
            } else{
                columnHelpers.add(new ColumnHelper<T>(property));
            }
        });
        return new FieldData<T>(columnHelpers, relationColumnHelpers);
    }

    default String getTableName(BeanIntrospection<T> beanIntrospection, Class<T> beanClass){
        Table[] tableData = beanIntrospection.synthesizeDeclaredAnnotationsByType(Table.class);

        if(tableData.length > 0){
            String tableName = tableData[0].name();
            if(tableData[0].schema() != null && !tableData[0].schema().isBlank()) {
                return tableData[0].schema() + "." + tableName;
            }
        } else {
            return beanClass.getSimpleName();
        }
        return null;
    }

    default RelationData processRelationData(BeanIntrospection<T> beanIntrospection){
        RelationMapping[] relationMapping = beanIntrospection.synthesizeDeclaredAnnotationsByType(RelationMapping.class);
        if (relationMapping.length > 0) {
            return new RelationData(relationMapping[0].foreignKey(), relationMapping[0].mappingTableName());
        }
        return new RelationData(null, null);
    }
}
