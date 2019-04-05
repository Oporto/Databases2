import simpledb.file.Block;
import simpledb.file.FileMgr;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
public class AdvBufferMgr {
   //hash map to hold the buffer pool
   private Map<Integer, Buffer> bufferpool;
   //reversed hashmap of buffer pool for ease of use
   private Map<Buffer, Integer> reversedPool;
   //map that holds the block and integer that correlates to the buffer
   private Map<Block, Integer> blockLocations;
   //linked list of all of the empty buffers that are available for use
   private LinkedList<Integer> emptyBuffers;
   private int numAvailable;

   /**
    * Creates a buffer manager having the specified number
    * of buffer slots.
    * This constructor depends on both the {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} objects
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * Those objects are created during system initialization.
    * Thus this constructor cannot be called until
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param numbuffs the number of buffer slots to allocate
    */
   AdvBufferMgr(int numbuffs) {
   	//creation of bufferpool and reversedpool
      bufferpool = new HashMap<Integer, Buffer>();
      reversedPool = new HashMap<Buffer, Integer>();
      blockLocations = new HashMap<Block, Integer>();
      emptyBuffers = new LinkedList<Integer>();
      //set number of buffers to be numAvailable
      numAvailable = numbuffs;
      //creation of buffer
      Buffer buffer;
      //for loop to go through # of buffers and create new buffer for each then add to both pools and the empty list
      for (int i=0; i<numbuffs; i++) {
         buffer = new Buffer();
         bufferpool.put(i, buffer);
         reversedPool.put(buffer, i);
         emptyBuffers.add(i);
      }
   }
   
   /**
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   synchronized void flushAll(int txnum) {
      Buffer buff;
      //loops through buffer pool getting each one and checking if modified (dirty bit) and flushes if so
      for (int i = 0; i < bufferpool.size(); i++) {
         buff = bufferpool.get(i);
         if (buff.isModifiedBy(txnum))
            buff.flush();
      }
   }
   
   /**
    * Pins a buffer to the specified block. 
    * If there is already a buffer assigned to that block
    * then that buffer is used;  
    * otherwise, an unpinned buffer from the pool is chosen.
    * Returns a null value if there are no available buffers.
    * @param blk a reference to a disk block
    * @return the pinned buffer
    */
   synchronized Buffer pin(Block blk) {
   	//using the blocks finds an existing buffer
      Buffer buff = findExistingBuffer(blk);
      //if the buffer is null finds/chooses an unpinned buffer
      if (buff == null) {
         buff = chooseUnpinnedBuffer();
         if (buff == null)
            return null;
         int i = reversedPool.get(buff);
         //puts blk in map to keep track of it
         blockLocations.put(blk, i);
         //assign the block to the buffer
         buff.assignToBlock(blk);
      }
      //if buffer is not pinned subtract num available
      if (!buff.isPinned())
         numAvailable--;

      //set time to be used later with LRU
      buff.setTime(System.currentTimeMillis());
      //pin buffer then return
      buff.pin();
      return buff;
   }
   
   /**
    * Allocates a new block in the specified file, and
    * pins a buffer to it. 
    * Returns null (without allocating the block) if 
    * there are no available buffers.
    * @param filename the name of the file
    * @param fmtr a pageformatter object, used to format the new block
    * @return the pinned buffer
    */
   synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
   	//use the function chooseUnpinnedBuffer to find buffer to pin
      Buffer buff = chooseUnpinnedBuffer();
      if (buff == null)
         return null;
      //assign blk
      Block blk = buff.assignToNew(filename, fmtr);
      //get the buffer based on the int with reversedpool
      int i = reversedPool.get(buff);
      //put blk, with int, into block hash map
      blockLocations.put(blk, i);
      //set time again for LRU use
      buff.setTime(System.currentTimeMillis());
      //pin buff
      numAvailable--;
      buff.pin();
      return buff;
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   synchronized void unpin(Buffer buff) {
   	//unpin buffer
      buff.unpin();
      int i = reversedPool.get(buff);
      if (!buff.isPinned())
         numAvailable++;

      //set time to be used later with LRU
      buff.setTime(System.currentTimeMillis());
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return numAvailable;
   }
   
   private Buffer findExistingBuffer(Block blk) {
   	//if block map contains the blk then return the buffer it is attached to else return null
	   if (blockLocations.containsKey(blk)) {
		   return bufferpool.get(blockLocations.get(blk));
	   }
	   return null;
   }
   
   private Buffer findEmptyBuffer() {
   	//if the empty buffer list is empty return null
      if (emptyBuffers.isEmpty())
   	    return null;
      else
      	//otherwise return the first of the empty buffer list and pops it
         return bufferpool.get(emptyBuffers.pop());
	   }
	   
   private Buffer LRUFinder(){
   	//get the time the first of the bufferpool, and get the first buffer
   	    long lruTimes = bufferpool.get(0).getTime();
   	    Buffer lruBuffer = bufferpool.get(0);
	//loop through bufferpool and check for times, updating lruTimes and lruBuffer as you find lru
	   for (int i = 0; i < bufferpool.size(); i++)
	   {
		   Buffer buff = bufferpool.get(i);
		   //checking for least time but also have to check for ones that aren't pinned
		   if (buff.getTime() < lruTimes && !buff.isPinned())
		   {
			   lruTimes = buff.getTime();
			   lruBuffer = buff;
		   }
	   }
	   //return lrubuffer, we found it!
	   return lruBuffer;
   }

   private Buffer chooseUnpinnedBuffer() {
   	//use the findEmptyBuffer function, if the buffer is null return that buffer
      Buffer buff = findEmptyBuffer();
      if (buff != null){
	      return buff;
      }
      //return the lru buffer
	   Buffer LRUChoice = LRUFinder();
	   //line to clear the block, decided we don't need but i'll leave it here in case
	   //blockLocations.remove(LRUChoice.block());
	   //return the lru!
	   return LRUChoice;
   }

   @Override
   public String toString(){
   	//to String function for advanced buffer manager,
      String str = "Advanced Buffer Manager: \n";
      // for loop to go through bufferpool
      for (int i = 0; i < bufferpool.size(); i++){
         str += "Buffer #" + i + ": " + bufferpool.get(i).toString() + "\n";
      }
      return str;
   }
}
