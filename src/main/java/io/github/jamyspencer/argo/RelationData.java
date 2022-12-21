package io.github.jamyspencer.argo;

public record RelationData(String foreignKey, String mappingTableName) {
    public boolean hasForeignKey() {
        return foreignKey != null && !foreignKey.isBlank();
    }
}
