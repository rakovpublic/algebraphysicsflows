package filesystems;

import filesystems.IFilePart;
import filesystems.IFileSystem;
import filesystems.IPath;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 09.10.2017.
 */
public abstract class AFilePart<T,P extends IPath,F extends IFileSystem<P>> implements IFilePart {
    abstract List<T> streamToList(InputStream is);

    @Override
    public List<T> getContent() {
        return this.streamToList(this.getStream());
    }
}
