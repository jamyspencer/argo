package io.github.jamyspencer.argo.annotations;

import io.github.jamyspencer.argo.BaseRepository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marker Annotation to indicate that the data(probably a Collection) in this
 * field is handled by a delegate repository class indicated by handler. The
 * intended use case is to enable handling a One-To-Many relationship.
 */
@Target({ElementType.FIELD})
public @interface ChildEntity {
    /**
     * Repository class that is used as a delegate to run database queries
     * on the field that this annotation annotates. This delegate class must
     * be annotated as a @Singleton
     */
    public Class<? extends BaseRepository> handler();
}
