package io.github.jamyspencer.argo;

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class QueryGeneratorWithGeneratedIdTest {
    private final QueryGenerator<TestGeneratedIdEntity> underTest = new QueryGeneratorImpl();
    private final EntityAnnotationProcessor<TestGeneratedIdEntity> annotationProcessor = new EntityAnnotationProcessorImpl();
    private final BeanIntrospection<TestGeneratedIdEntity> beanIntrospection = BeanIntrospection.getIntrospection(TestGeneratedIdEntity.class);
    private FieldData<TestGeneratedIdEntity> fieldData = annotationProcessor.processEntityData(beanIntrospection);
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
        String expected = "INSERT INTO " + tableName + " (something,cost) VALUES ";
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
    class QueryGeneratorImpl implements QueryGenerator<TestGeneratedIdEntity>{

    }
    class EntityAnnotationProcessorImpl implements EntityAnnotationProcessor<TestGeneratedIdEntity> {}
}