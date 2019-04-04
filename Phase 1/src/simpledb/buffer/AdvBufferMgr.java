package simpledb.buffer;

import simpledb.file.Block;
import simpledb.file.FileMgr;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
class AdvBufferMgr {
   private HashMap<Integer, Buffer> bufferpool;
   private HashMap<Buffer, Integer> reversedPool;
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
      bufferpool = new HashMap<Integer, Buffer>();
      reversedPool = new HashMap<Buffer, Integer>();
      emptyBuffers = new LinkedList<Integer>();
      numAvailable = numbuffs;
      Buffer buffer;
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
      Buffer buff = findExistingBuffer(blk);
      if (buff == null) {
         buff = chooseUnpinnedBuffer();
         if (buff == null)
            return null;
         buff.assignToBlock(blk);
      }
      if (!buff.isPinned())
         numAvailable--;
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
      Buffer buff = chooseUnpinnedBuffer();
      if (buff == null)
         return null;
      buff.assignToNew(filename, fmtr);
      numAvailable--;
      buff.pin();
      return buff;
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   synchronized void unpin(Buffer buff) {
      buff.unpin();
      int i = reversedPool.get(buff);
      emptyBuffers.add(i);
      if (!buff.isPinned())
         numAvailable++;
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return numAvailable;
   }
   
   private Buffer findExistingBuffer(Block blk) {
      Buffer buff;
      for (int i = 0; i < bufferpool.size(); i++) {
         buff = bufferpool.get(i);
         Block b = buff.block();
         if (b != null && b.equals(blk))
            return buff;
      }
      return null;
   }
   
   private Buffer chooseUnpinnedBuffer() {
      if (emptyBuffers.isEmpty())
   	    return null;
      else
         return bufferpool.get(emptyBuffers.pop());
	   }
	}
