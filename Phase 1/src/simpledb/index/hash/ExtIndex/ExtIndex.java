package simpledb.index.hash.ExtIndex;

import simpledb.index.Index;
import simpledb.query.Constant;
import simpledb.query.TableScan;
import simpledb.file.Block;
import simpledb.record.RecordFile;
import simpledb.record.RID;
import simpledb.record.Schema;
import simpledb.record.TableInfo;
import simpledb.tx.Transaction;

/**
 * An extensible hash implementation of the Index interface.
 * A variable number of buckets are created, increasing as they fill up
 * and each bucket is implemented as a file of index records.
 * @author Edward Sciore
 */
public class ExtIndex implements Index {
	public static int NUM_BUCKETS = 100;
	private ExtIndexTreeNode tree_head;
	private int tree_height;
	
	private String idxname;
	private int max_bucket_size;
	private Schema sch;
	private Transaction tx;
	private Constant searchkey = null;
	private TableScan ts = null;

	/**
	 * Opens a hash index for the specified index.
	 * @param idxname the name of the index
	 * @param sch the schema of the index records
	 * @param tx the calling transaction
	 */
	public ExtIndex(String idxname, Schema sch, Transaction tx) {
		this.idxname = idxname;
		this.sch = sch;
		this.tx = tx;
		this.max_bucket_size = 100; //TODO: Fix arbitrary value for max bucket size
		this.tree_height = 1;
		this.tree_head = new ExtIndexTreeNode(0,
				new ExtIndexTreeBucketNode(1, 0,0),
				new ExtIndexTreeBucketNode(1, 1,0));
	}

	/**
	 * Positions the index before the first index record
	 * having the specified search key.
	 * The method hashes the search key to determine the bucket,
	 * and then opens a table scan on the file
	 * corresponding to the bucket.
	 * The table scan for the previous bucket (if any) is closed.
	 * @see Index#beforeFirst(Constant)
	 */
	public void beforeFirst(Constant searchkey) {
		close();
		this.searchkey = searchkey;
		ExtIndexTreeBucketNode bucket = findTreeBucket(searchkey.hashCode());
		String tblname = idxname + bucket;
		TableInfo ti = new TableInfo(tblname, sch);
		ts = new TableScan(ti, tx);
	}

	public void beforeFirstInsert(Constant searchkey, int n_of_inserts) {
		close();
		this.searchkey = searchkey;
		ExtIndexTreeBucketNode bucket = findTreeBucket(searchkey.hashCode());
		if (bucket.getSize() + n_of_inserts > max_bucket_size){
			//TODO: Split buckets and reassign to new bucket that matches searchkey hashcode
		}
		String tblname = idxname + bucket;
		TableInfo ti = new TableInfo(tblname, sch);
		ts = new TableScan(ti, tx);
	}

	/**
	 * Moves to the next record having the search key.
	 * The method loops through the table scan for the bucket,
	 * looking for a matching record, and returning false
	 * if there are no more such records.
	 * @see Index#next()
	 */
	public boolean next() {
		while (ts.next())
			if (ts.getVal("dataval").equals(searchkey))
				return true;
		return false;
	}

	/**
	 * Retrieves the dataRID from the current record
	 * in the table scan for the bucket.
	 * @see Index#getDataRid()
	 */
	public RID getDataRid() {
		int blknum = ts.getInt("block");
		int id = ts.getInt("id");
		return new RID(blknum, id);
	}

	/**
	 * Inserts a new record into the table scan for the bucket.
	 * @see Index#insert(Constant, RID)
	 */
	public void insert(Constant val, RID rid) {
		beforeFirstInsert(val, 1);
		ts.insert();
		ts.setInt("block", rid.blockNumber());
		ts.setInt("id", rid.id());
		ts.setVal("dataval", val);
	}

	/**
	 * Deletes the specified record from the table scan for
	 * the bucket.  The method starts at the beginning of the
	 * scan, and loops through the records until the
	 * specified record is found.
	 * @see Index#delete(Constant, RID)
	 */
	public void delete(Constant val, RID rid) {
		beforeFirst(val);
		/*
		TODO: Add check for bucket being empty after deletion and handle that scenario
		 (beforeFirst is used on update and delete as well so it shouldn't worry about this case)
		 */
		while(next())
			if (getDataRid().equals(rid)) {
				ts.delete();
				return;
			}
	}

	/**
	 * Closes the index by closing the current table scan.
	 * @see Index#close()
	 */
	public void close() {
		if (ts != null)
			ts.close();
	}

	/**
	 * Returns the cost of searching an index file having the
	 * specified number of blocks.
	 * The method assumes that all buckets are about the
	 * same size, and so the cost is simply the size of
	 * the bucket.
	 * @param numblocks the number of blocks of index records
	 * @param rpb the number of records per block (not used here)
	 * @return the cost of traversing the index
	 */
	//TODO: Completely override
	public static int searchCost(int numblocks, int rpb){
		return numblocks / ExtIndex.NUM_BUCKETS;
	}

	public ExtIndexTreeBucketNode findTreeBucket(int hashedKey){
		char[] bin = Integer.toBinaryString(hashedKey).toCharArray();
		ExtIndexTreeBucketNode bucket = tree_head.getBucket(bin);
		return bucket;
	}
}
