package io.github.jamyspencer.argo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface FunctionalParameterSetter<T> {
    public void set(PreparedStatement ps, int location, T payload) throws SQLException;
}
