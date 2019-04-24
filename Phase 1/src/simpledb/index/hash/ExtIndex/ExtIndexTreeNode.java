package simpledb.index.hash.ExtIndex;

public class ExtIndexTreeNode implements ExtIndexTreeNodeI {
    private ExtIndexTreeNodeI childZero;
    private ExtIndexTreeNodeI childOne;
    private int depth;

    public ExtIndexTreeNode(int depth, ExtIndexTreeNodeI childOne, ExtIndexTreeNodeI childZero){
        this.depth = depth;
        this.childZero = childZero;
        this.childOne = childOne;
    }
    @Override
    public ExtIndexTreeBucketNode getBucket(char[] bin) {
        ExtIndexTreeBucketNode bucket;
        if (bin[depth] == '0'){
            bucket = childZero.getBucket(bin);
        } else{
            bucket = childOne.getBucket(bin);
        }
        return bucket;
    }

    @Override
    public int getDepth() {
        return depth;
    }


}
