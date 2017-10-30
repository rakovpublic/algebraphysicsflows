package filesystems;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 30.10.2017.
 */
public interface IFileFormat<T> {
    List<T> parseStream(InputStream in);
}
