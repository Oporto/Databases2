package simpledb.index.hash.ExtIndex;

public interface ExtIndexTreeNodeI{
    ExtIndexTreeBucketNode getBucket(char[] bin);
    int getDepth();
}
