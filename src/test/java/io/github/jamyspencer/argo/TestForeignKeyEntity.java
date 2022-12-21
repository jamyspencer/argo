package io.github.jamyspencer.argo;

import io.github.jamyspencer.argo.annotations.RelationMapping;
import io.micronaut.core.annotation.Introspected;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Introspected
@RelationMapping(foreignKey = "bob")
public record TestForeignKeyEntity(
        @Id Long id,
        String something,
        Double cost
) {
}
