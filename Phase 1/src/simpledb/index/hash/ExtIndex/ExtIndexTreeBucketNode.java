package simpledb.index.hash.ExtIndex;

public class ExtIndexTreeBucketNode implements ExtIndexTreeNodeI {
    private int bucket;
    private int size;
    private int depth;

    public ExtIndexTreeBucketNode(int depth, int bucket, int size){
        this.depth = depth;
        this.bucket = bucket;
        this.size = size;
    }
    @Override
    public ExtIndexTreeBucketNode getBucket(char[] bin) {
        return this;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    public int getSize() {return size;}


}
