package dbinput;

import java.sql.ResultSet;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 23.10.2017.
 */
public interface IDBMapper<T> {
    List<T> map(ResultSet resultSet);
}
