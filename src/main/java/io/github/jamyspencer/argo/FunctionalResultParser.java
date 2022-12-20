package io.github.jamyspencer.argo;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface FunctionalResultParser<T> {
    void parse(ResultSet rs, T item) throws SQLException;
}
