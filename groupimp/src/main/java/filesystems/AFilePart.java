package filesystems;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 09.10.2017.
 */
public  class AFilePart<T,P extends IPath,K extends IFilePointer> implements IFilePart {
    private P path;
    private K filePointer;
    private IFileSystem<P,K> fileSystem;
    private IFileFormat<T> fileFormat;

    public AFilePart(P path, K filePointer, IFileSystem<P,K> fileSystem, IFileFormat<T> fileFormat) {
        this.path = path;
        this.filePointer = filePointer;
        this.fileSystem = fileSystem;
        this.fileFormat = fileFormat;
    }

    @Override
    public List<T> getContent() {
        return this.getFileFormat().parseStream(this.getStream());
    }

    @Override
    public String toStr() {
        StringBuilder stringBuilder= new StringBuilder();
        stringBuilder.append("{\"path\":\"");
        stringBuilder.append(getPath().toStr());
        stringBuilder.append("\",\"");
        stringBuilder.append(getPointer().toStr());
        stringBuilder.append("\"}");
        return stringBuilder.toString();
    }

    @Override
    public InputStream getStream() {
        return getFileSystem().getStream(getPath(),getPointer());
    }

    @Override
    public K getPointer() {
        return filePointer;
    }

    @Override
    public IPath getPath() {
        return path;
    }

    @Override
    public IFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    public IFileFormat getFileFormat() {
        return fileFormat;
    }
}
