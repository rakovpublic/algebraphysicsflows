package filesystems;

import cluster.IPart;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 12.10.2017.
 */
public abstract class AFileInputSpliter<T,P extends IPath,K extends IFilePointer> implements IFileInputSpliter<T,P,K> {


    @Override
    public IPart partFromString(String part) {
        int pathStart=part.indexOf(":")+2;
        int pathEnd=part.indexOf("\",\"",pathStart);
        String path =part.substring(pathStart,pathEnd);
        P rPath= getFileSystem().getPathFromString(path);
        int pointerStart=part.indexOf(":",pathEnd)+2;
        String pointer=part.substring(pointerStart,part.length()-2);
        K p=getFileSystem().getPointerFromString(pointer);
        return new AFilePart<T,P,K>(rPath,p,getFileSystem(),getFileFormat());
    }

    @Override
    public List<IFilePart<T, P,K>> split() {
        return split(this.getPaths(),this.getFileSystem());
    }


}
