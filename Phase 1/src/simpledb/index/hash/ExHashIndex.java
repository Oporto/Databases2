package simpledb.index.hash;

import java.util.ArrayList;
import java.util.List;

//TODO I think we will end up needing to increase the buffer size

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
	
	//files for the index
	protected final String IDX_FILENAME = "exhashindexfile";
	protected final String GBL_FILENAME = "exhashgblfile";
	
	//global and index field names?
	//TODO do i need more
	protected final String GBL_FiELD = "gbl";
	protected final String BCKT_NUM = "bcktnum";
	protected final String BCKT_BITS = "bcktbits";
	protected final String BCKT_TUPLES = "bckttuples";
	protected final String BCKT_FILENAME = "idxfilename";
	
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
		
		idxBcktTableInfo = new TableInfo(IDX_FILENAME, idxBcktSchema);
		idxBcktTableScan = new TableScan(idxBcktTableInfo, tx);
		idxBcktTableScan.beforeFirst();
	}
	
	@Override
	public void beforeFirst(Constant searchkey)
	{
	
	}
	
	@Override
	public boolean next()
	{
		return false;
	}
	
	@Override
	public RID getDataRid()
	{
		return null;
	}
	
	@Override
	public void insert(Constant dataval, RID datarid)
	{
	
	}
	
	@Override
	public void delete(Constant dataval, RID datarid)
	{
	
	}
	
	@Override
	public void close()
	{
	
	}
}
