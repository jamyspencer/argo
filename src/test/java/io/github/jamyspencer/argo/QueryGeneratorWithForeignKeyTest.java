package io.github.jamyspencer.argo;

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class QueryGeneratorWithForeignKeyTest {
    private final QueryGenerator<TestForeignKeyEntity> underTest = new QueryGeneratorImpl();
    private final EntityAnnotationProcessor<TestForeignKeyEntity> annotationProcessor = new EntityAnnotationProcessorImpl();
    private final BeanIntrospection<TestForeignKeyEntity> beanIntrospection = BeanIntrospection.getIntrospection(TestForeignKeyEntity.class);
    private FieldData<TestForeignKeyEntity> fieldData = annotationProcessor.processEntityData(beanIntrospection);
    private final RelationData relationData = annotationProcessor.processRelationData(beanIntrospection);
    private String tableName = "test.test";
    @Test
    void generateBaseReadQuery() {
        String value = underTest.generateBaseReadQuery(tableName, fieldData.columnHelpers());
        String expected = "SELECT id,something,cost FROM " + tableName;
        assertEquals(expected, value);
    }

    @Test
    void generateBaseInsertQuery() {
        String value = underTest.generateBaseInsertQuery(tableName,fieldData.columnHelpers(),relationData.foreignKey());
        String expected = "INSERT INTO " + tableName + " (id,something,cost,bob) VALUES ";
        assertEquals(expected, value);
    }

    @Test
    void generateBaseDeleteQuery() {
        String value = underTest.generateBaseDeleteQuery(tableName);
        String expected = "DELETE FROM " + tableName;
        assertEquals(expected, value);
    }

    @Test
    void generateBaseUpdateQuery() {
        String value = underTest.generateBaseUpdateQuery(tableName);
        String expected = "UPDATE " + tableName + " SET ";
        assertEquals(expected, value);
    }

    @Test
    void getBatchInsertQuery() {
        String base = "INSERT INTO " + tableName + " (something,cost) VALUES ";
        String value = underTest.getBatchInsertQuery(base, "(?,?,?,?)", 3);
        String expected = base + "(?,?,?,?),(?,?,?,?),(?,?,?,?)";
        assertEquals(expected, value);
    }

    @Test
    void generateInsertParameters() {
        String value = underTest.generateInsertParameters(fieldData);
        String expected = "(?,?,?,?)";
        assertEquals(expected, value);
    }
    class QueryGeneratorImpl implements QueryGenerator<TestForeignKeyEntity>{

    }
    class EntityAnnotationProcessorImpl implements EntityAnnotationProcessor<TestForeignKeyEntity> {}
}