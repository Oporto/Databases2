package simpledb.index.hash;

import java.util.*;

//TODO I think we will end up needing to increase the buffer size

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import simpledb.index.Index;
import simpledb.query.Constant;
import simpledb.query.TableScan;
import simpledb.record.RID;
import simpledb.record.Schema;
import simpledb.record.TableInfo;
import simpledb.tx.Transaction;

public class ExHashIndex implements Index
{
	//values used for tracking current status and all the hash index info needed
	protected int cBuckets = 0;
	protected int cBucketsNum = 0;
	protected int cBits = 1;
	protected String idxname;
	protected Schema sch;
	protected Transaction tx;
	protected Constant searchKey = null;
	protected TableScan tablescan = null;
	
	//global and index field names?
	//TODO do i need more
	protected final String GBL_FiELD = "gbl";
	protected final String BCKT_NUM = "bcktnum";
	protected final String BCKT_BITS = "bcktbits";
	protected final String BCKT_TUPLES = "bckttuples";
	protected final String BCKT_FILENAME = "idxfilename";
	
	//files for the index
	protected final String IDX_FILENAME = "exhashindexfile";
	protected final String GBL_FILENAME = "exhashgblfile";
	
	//max num tuples in idx bucket;; idk this is a rando value
	protected final int NUM_BCKT_TUPLES = 100;
	
	//both global and index information
	protected Schema gblSchema;
	protected TableInfo gblInfo;
	protected TableScan gblScan;
	protected Schema idxBcktSchema;
	protected TableInfo idxBcktTableInfo;
	protected TableScan idxBcktTableScan;
	
	//TODO determine the hash value for modulo, i didn't know we needed this till i taked to my friend
	//TODO apparetnly we can choose the value but idkkkkkk, i'm gonna use the value he did for now
	protected static final int HASH_MOD_VAL = 1610612741;
	
	//constructy boi
	public ExHashIndex(String idxname, Schema sch, Transaction tx){
		this.idxname = idxname;
		this.sch = sch;
		this.tx = tx;
		//creating the global schema, the global info table, and then scan the table
		gblSchema = new Schema();
		//add in the single field
		gblSchema.addIntField(GBL_FiELD);
		gblInfo = new TableInfo(GBL_FILENAME, gblSchema);
		gblScan = new TableScan(gblInfo, tx);
		gblScan.beforeFirst();
		gblScan.next();
		
		//catch issues with setting the global bit count
		try{
			cBits = gblScan.getInt(GBL_FiELD);
		} catch (Exception e){
			cBits = 0;
			System.out.println("gbl bit count issue");
		}
		
		//now that globals are set, setting up the bucket schema for the index
		idxBcktSchema = new Schema();
		//add all the fields in as well
		idxBcktSchema.addIntField(BCKT_NUM);
		idxBcktSchema.addIntField(BCKT_BITS);
		idxBcktSchema.addIntField(BCKT_TUPLES);
		idxBcktSchema.addStringField(BCKT_FILENAME, 10);
		//TODO fix this if it is not working
		idxBcktTableInfo = new TableInfo(IDX_FILENAME + BCKT_NUM, idxBcktSchema);
		idxBcktTableScan = new TableScan(idxBcktTableInfo, tx);
		idxBcktTableScan.beforeFirst();
	}
	
	@Override
	//close any table scans from before and reset current bucket number that is being looked at
	public void beforeFirst(Constant searchkey)
	{
		close();
		this.searchKey = searchkey;
		//TODO check to make sure this is doing the hash thangs correctly
		cBucketsNum = searchkey.hashCode()%HASH_MOD_VAL;
	}
	
	@Override
	public boolean next()
	{
		while (tablescan.next()){
			if( tablescan.getVal("dataval").equals(searchKey)){
				return true;
			}
	}
		return false;
	}
	
	@Override
	//from current record in tablescan gets the data RID for the bckt
	//used based on tablescan and RID classes done for us4
	public RID getDataRid()
	{
		int blocknum = tablescan.getInt("block");
		int id = tablescan.getInt("id");
		return new RID(blocknum, id);
	}
	
	public void setGblBits(){
		gblScan.beforeFirst();
		while (gblScan.next()){
			gblScan.delete();
			gblScan.beforeFirst();
		}
		gblScan.insert();
		gblScan.setInt(GBL_FiELD, cBits);
	}
	
	// takes number of bits to leave unmasked from rightmost bit and Returns a mask for the
	// number of bits to leave unmasked starting from 2^0
	//this part was confusing to me so I looked up online other examples of bit masking
	public int getMaskValue(int bits){
		int finalsum = 0;
		for (int i = bits -1; i >= 0; i--){
			finalsum = finalsum | (int) Math.pow(2,i);
		}
		return finalsum;
	}
	
	@Override
	//for bckt inserts new record into table scan
	public void insert(Constant dataval, RID datarid)
	{
		beforeFirst(dataval);
		idxBcktTableScan.beforeFirst();
		
		//first step is there an existing bucket? lets see
		while(idxBcktTableScan.next()){
			//TODO was confused on this part and need to review before submitting
			//need two values 1.current has val, and bucket val (its bit num)
			int bcktnummask = idxBcktTableScan.getInt(BCKT_NUM) & getMaskValue(idxBcktTableScan.getInt(BCKT_BITS));
			int cbcktnummask = cBucketsNum & getMaskValue(idxBcktTableScan.getInt(BCKT_BITS));
			
			//are these values equal
			if(bcktnummask == cbcktnummask){
				//is bckt boutta be full
				if (idxBcktTableScan.getInt(BCKT_TUPLES) >= NUM_BCKT_TUPLES){
					//now the hard stuff, need to reorder these bad boys
					reorderBcktRecords();
					return;
				} else {
					//easy one juse insert into a bucket that already exists
					insertExistingIdxBckt(dataval, datarid);
					//TODO could use this for printing to make sure everything is inserting correctly
					return;
				}
			}
		}
		//new value and new bucket option? & getMask(currentBitCount), currentBitCount);
		insertNewIdxBckt(dataval, datarid, cBucketsNum & getMaskValue(cBits), cBits);
		//TODO can do same type of printing down here as well
	}
	
	//reorganizing all the bucket values, big oof
	//need to increase dir size and put all the values at their new bit values
	public void reorderBcktRecords(){
		int oldBcktBits = idxBcktTableScan.getInt(BCKT_BITS);
		
		//does the global need to be incremented? increment current and set to global
		if ((idxBcktTableScan.getInt(BCKT_BITS) + 1) > cBits){
			cBits++;
			setGblBits();
		}
		
		//i think we need this to track the displaced bois
		HashMap<Constant, RID> idkMap = new HashMap<Constant, RID>();
		List<Constant> idkList = new ArrayList<Constant>();
		
		idxBcktTableScan.beforeFirst();
		while (idxBcktTableScan.next()){
			
			//bucket bits greater, so need to increment
			idxBcktTableScan.setInt(BCKT_BITS, idxBcktTableScan.getInt(BCKT_BITS)+1);
			
			//open current bucket file, this one is full and we gonna have to do some work on it
			String fname = idxBcktTableScan.getString(BCKT_FILENAME);
			tablescan = getTableFromFName(fname);
			tablescan.beforeFirst();
			
			//lets go through all the records in the file
			while(tablescan.next())
			{
				Constant key = tablescan.getVal("dataval");
				int bckt = key.hashCode() % HASH_MOD_VAL;
				
				//masked values for the record and  and also for new bckt bits
				int chckbcktnummask = idxBcktTableScan.getInt(BCKT_NUM) & getMaskValue(idxBcktTableScan.getInt(BCKT_BITS));
				int bcktnummask = bckt & getMaskValue(idxBcktTableScan.getInt(BCKT_BITS));
				
				//ok so now we increased the bit value but what if a record should be hashed in here
				//we gotta remove that ish and resort it, so lets add it to our list for now
				if (bcktnummask != chckbcktnummask)
				{
					idkMap.put(key, getDataRid());
					idkList.add(key);
					idxBcktTableScan.setInt(BCKT_TUPLES, idxBcktTableScan.getInt(BCKT_TUPLES) - 1);
					tablescan.delete();
				}
			}
		}
		
		for (Constant k : idkList){
			insert(k, idkMap.get(k));
		}
		tablescan.close();
	}
	
	//inserting the record into a bucket that already exists, also increments tuples in bucket
	public void insertExistingIdxBckt(Constant val, RID rid){
		idxBcktTableScan.setInt(BCKT_TUPLES, idxBcktTableScan.getInt(BCKT_TUPLES) + 1);
		tablescan = getTableFromFName(idxBcktTableScan.getString(BCKT_FILENAME));
		
		//actually inserting the new value
		tablescan.beforeFirst();
		tablescan.insert();
		tablescan.setInt("block", rid.blockNumber());
		tablescan.setInt("id", rid.id());
		tablescan.setVal("dataval", val);
		tablescan.close();
	}
	
	//new value and new bucket oh boy!
	public void insertNewIdxBckt(Constant val, RID rid, int maskBcktNum, int bits){
		//insert the new bucket
		idxBcktTableScan.insert();
		idxBcktTableScan.setInt(BCKT_NUM, maskBcktNum);
		idxBcktTableScan.setInt(BCKT_BITS, bits);
		idxBcktTableScan.setInt(BCKT_TUPLES, 1);
		idxBcktTableScan.setString(BCKT_FILENAME, idxname + maskBcktNum);
		
		//does global bits need to be incremented, idk lets find out
		if(bits < cBits){
			cBits++;
			setGblBits();
		}
		
		tablescan = getTableFromFName(idxBcktTableScan.getString(BCKT_FILENAME));
		
		//actually inserting the new value
		tablescan.beforeFirst();
		tablescan.insert();
		tablescan.setInt("block", rid.blockNumber());
		tablescan.setInt("id", rid.id());
		tablescan.setVal("dataval", val);
		tablescan.close();
		//end by incrementing
		cBuckets++;
	}
	
	//gets table scan based on a file name
	public TableScan getTableFromFName(String fname) {
		TableInfo tbleinfo = new TableInfo(fname, sch);
		return new TableScan(tbleinfo, tx);
	}
	
	@Override
	//if datarid matches deletes the record from the tablescan. starts in scan, loops through until finds the record
	public void delete(Constant dataval, RID datarid)
	{
		beforeFirst(dataval);
		while (next()){
			if (getDataRid().equals(datarid)){
				tablescan.delete();
				return;
			}
		}
	}
	
	@Override
	//closes index with closing table scan
	public void close()
	{
		if (tablescan != null){
			tablescan.close();
		}
	}
	
	//TODO add some print functions for testing??
	//TODO for index bucket use .next on the tablescan and print out all the fields
	//TODO print the bit count, bucket count, bucket number, and hash mod for stats?
}
