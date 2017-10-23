package dbinput;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 23.10.2017.
 */
public class ADBPart<T> implements IDBPart<T> {
    private IDBMapper<T> idbMapper;
    private String query;
    private String clazz;
    private String url;
    private int limit;
    private int offset;
    private static final Logger logger = LogManager.getLogger(ADBPart.class);


    ADBPart(IDBMapper<T> idbMapper, String query, String clazz, String url, int limit, int offset) {
        this.idbMapper = idbMapper;
        this.query = query;
        this.clazz = clazz;
        this.url = url;
        this.limit = limit;
        this.offset = offset;
    }

    @Override
    public List<T> getContent() {
        Connection conn = null;
        PreparedStatement stmt = null;
        List<T> tList = new LinkedList<T>();
        try {
            Class.forName(clazz);
            conn = DriverManager.getConnection(url);
            stmt = conn.prepareStatement(query);
            stmt.setInt(0, limit);
            stmt.setInt(1, offset);
            tList.addAll(idbMapper.map(stmt.executeQuery()));
        } catch (Exception e) {
            logger.error(e);
        }
        return tList;
    }

    @Override
    public String toStr() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(offset);
        return stringBuilder.toString();
    }
}
