import org.junit.Test;
import simpledb.buffer.*;
import simpledb.file.*;

import static junit.framework.TestCase.*;

public class Phase1Tests {
    @Test
    public void testTask1(){
        int size;
        long bef;
        long delta;
        System.out.println("Basic test:");
        for (int i = 10; i < 15; i++){
            size = 2 << i;
            BufferMgr bufferMgr = new BufferMgr(size);
            for (int j = 0; j < size-1; j ++){
                bufferMgr.pin(new Block("FakeFile", j + 1));
            }
            bef = System.currentTimeMillis();
            bufferMgr.pin(new Block("FakeFile", size));
            delta = System.currentTimeMillis() - bef;
            System.out.println("bef: " + bef+ "delta: " + delta);
            System.out.println("For size: " + size + "\tTime in ml to find last empty buffer: " + delta);
        }

        System.out.println("Advanced test:");
        for (int i = 10; i < 15; i++){
            size = 2 << i;
            NewBufferMgr bufferMgr = new NewBufferMgr(size);
            for (int j = 0; j < size-1; j ++){
                bufferMgr.pin(new Block("FakeFile", j + 1));
            }
            bef = System.currentTimeMillis();
            bufferMgr.pin(new Block("FakeFile", size));
            delta = System.currentTimeMillis() - bef;
            System.out.println("For size: " + size + "\tTime in ml to find last empty buffer: " + delta);
        }

        assertEquals(1,1);
    }
}
