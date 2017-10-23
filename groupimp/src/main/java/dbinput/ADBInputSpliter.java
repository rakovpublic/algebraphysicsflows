package dbinput;

import cluster.IPart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 23.10.2017.
 */
public class ADBInputSpliter<T> implements IDBInputSpliter<T> {
    private IDBMapper<T> idbMapper;
    private String tableName;
    private String clazz;
    private String url;
    private int batchSize;
    private static final Logger logger = LogManager.getLogger(ADBPart.class);
    private static final String delimeter = ":;;-;;:";
    private String sql;

    public ADBInputSpliter(IDBMapper<T> idbMapper, String tableName, String clazz, String url, int batchSize) {
        this.idbMapper = idbMapper;
        this.tableName = tableName;
        this.clazz = clazz;
        this.url = url;
        this.batchSize = batchSize;
        this.sql = "select * from " + tableName + " limit ? offset ? ;";
    }

    @Override
    public List<IPart<T>> split() {
        Connection conn = null;
        PreparedStatement stmt = null;
        List<IPart<T>> tList = new LinkedList<IPart<T>>();
        int amount = 0;
        try {
            Class.forName(clazz);
            conn = DriverManager.getConnection(url);
            stmt = conn.prepareStatement("select count(*) from " + tableName + " ;");
            ResultSet resultSet = stmt.executeQuery();
            resultSet.next();
            amount = resultSet.getInt(0);

        } catch (Exception e) {
            logger.error(e);
        }
        for (int c = 0; c <= amount; ) {
            tList.add(new ADBPart<T>(idbMapper, sql, clazz, url, batchSize, c));
            c += batchSize;
        }
        return tList;
    }

    @Override
    public IPart<T> partFromString(String part) {

        return new ADBPart<T>(idbMapper, sql, clazz, url, batchSize, Integer.parseInt(part));
    }


}
