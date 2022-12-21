package io.github.jamyspencer.argo;

import io.micronaut.core.annotation.Introspected;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Introspected
public record TestGeneratedIdEntity(
        @Id @GeneratedValue Long id,
        String something,
        Double cost
) {
}
