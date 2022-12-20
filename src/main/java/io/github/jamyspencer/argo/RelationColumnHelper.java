package io.github.jamyspencer.argo;

import io.github.jamyspencer.argo.annotations.ChildEntity;
import io.micronaut.core.beans.BeanProperty;

public class RelationColumnHelper {
    private final Class<? extends BaseRepository> delegate;
    private final BeanProperty beanProperty;

    public RelationColumnHelper(BeanProperty beanProperty, ChildEntity childEntity) {
        this.beanProperty = beanProperty;
        delegate = childEntity.handler();
    }

    public BeanProperty getBeanProperty() {
        return beanProperty;
    }

    public Class<? extends BaseRepository> getDelegate() {
        return delegate;
    }
}
