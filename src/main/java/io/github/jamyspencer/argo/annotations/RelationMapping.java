package io.github.jamyspencer.argo.annotations;

public @interface RelationMapping {
    /**
     * foreignKey is a value NOT in the Entity. In a One-To-One
     * or One-To-Many relationship this will be the id of the parent
     * object
     */
    String foreignKey() default "";
    /**
     * key is the name of the column that the foreign key maps to
     */
    String key() default "";
    /**
     * name of table used to map Many-TO-Many relationships
     */
    String mappingTableName() default "";
    Class javaType() default Long.class;
}
