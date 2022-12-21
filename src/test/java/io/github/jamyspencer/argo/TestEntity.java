package io.github.jamyspencer.argo;

import io.micronaut.core.annotation.Introspected;
import javax.persistence.Table;

@Introspected
@Table(name = "test", schema = "test")
public class TestEntity {
    private String id;
    private Long something;
    private Double cost;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getSomething() {
        return something;
    }

    public void setSomething(Long something) {
        this.something = something;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }
}
