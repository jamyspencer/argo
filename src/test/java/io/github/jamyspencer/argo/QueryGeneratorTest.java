package io.github.jamyspencer.argo;

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class QueryGeneratorTest {
    private final QueryGenerator<TestEntity> underTest = new QueryGeneratorImpl();
    private final EntityAnnotationProcessor<TestEntity> annotationProcessor = new EntityAnnotationProcessorImpl();
    private final BeanIntrospection<TestEntity> beanIntrospection = BeanIntrospection.getIntrospection(TestEntity.class);
    private FieldData<TestEntity> fieldData = annotationProcessor.processEntityData(beanIntrospection);
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
        String expected = "INSERT INTO " + tableName + " (id,something,cost) VALUES ";
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
        String base = "INSERT INTO " + tableName + " (id,something,cost) VALUES ";
        String value = underTest.getBatchInsertQuery(base, "(?,?,?)", 3);
        String expected = base + "(?,?,?),(?,?,?),(?,?,?)";
        assertEquals(expected, value);
    }

    class QueryGeneratorImpl implements QueryGenerator<TestEntity>{

    }
    class EntityAnnotationProcessorImpl implements EntityAnnotationProcessor<TestEntity> {}
}