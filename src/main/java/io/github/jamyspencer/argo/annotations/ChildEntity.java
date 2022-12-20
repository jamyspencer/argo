package io.github.jamyspencer.argo.annotations;

import io.github.jamyspencer.argo.BaseRepository;

public @interface ChildEntity {
    public Class<? extends BaseRepository> handler();
}
