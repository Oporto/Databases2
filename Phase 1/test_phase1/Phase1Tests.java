import org.junit.Test;
import simpledb.file.*;
import simpledb.server.Startup;

import static junit.framework.TestCase.*;

public class Phase1Tests {
    @Test
    public void testTask1(){
        int size;
        long bef;
        long delta1;
        long delta2;
        System.out.println("Basic test:");
        for (int i = 10; i < 15; i++){
            size = 2 << i;
            BufferMgr bufferMgr = new BufferMgr(size);
            bef = System.currentTimeMillis();
            for (int j = 0; j < size; j ++){
                bufferMgr.pin(new Block("FakeFile", j + 1));
            }
            delta1 = System.currentTimeMillis() - bef;
            System.out.println("For size: " + size + "\tTime in ms to fill buffer: " + delta1);
        }

        System.out.println("Advanced test:");
        for (int i = 10; i < 15; i++){
            size = 2 << i;
            NewBufferMgr bufferMgr = new NewBufferMgr(size);
            bef = System.currentTimeMillis();
            for (int j = 0; j < size; j ++){
                bufferMgr.pin(new Block("FakeFile", j + 1));
            }
            delta2 = System.currentTimeMillis() - bef;
            System.out.println("For size: " + size + "\tTime in ms to fill buffer: " + delta2);
        }

        assertTrue(true);
    }

    @Test
    public void testTask2(){
        int size;
        long bef;
        long delta1;
        long delta2;
        int step;
        System.out.println("Basic test:");
        for (int i = 10; i < 15; i++){
            size = 2 << i;
            BufferMgr bufferMgr = new BufferMgr(size);
            for (int j = 0; j < size; j ++){
                bufferMgr.pin(new Block("FakeFile", j + 1));
            }
            bef = System.currentTimeMillis();
            step = size/1024;
            for (int k = 1; k < 1024; k++){
                for (int p = 0; p < 100; p++){
                    bufferMgr.pin(new Block("FakeFile", k*step));
                }
            }
            delta1 = System.currentTimeMillis() - bef;
            System.out.println("For size: " + size + "\tTime in ms to pin 1024 buffers 100 times each: " + delta1);
        }

        System.out.println("Advanced test:");
        for (int i = 10; i < 15; i++){
            size = 2 << i;
            NewBufferMgr bufferMgr = new NewBufferMgr(size);
            for (int j = 0; j < size; j ++){
                bufferMgr.pin(new Block("FakeFile", j + 1));
            }
            bef = System.currentTimeMillis();
            step = size/1024;
            for (int k = 1; k < 1024; k++){
                for (int p = 0; p < 100; p++){
                    bufferMgr.pin(new Block("FakeFile", k*step));
                }
            }
            delta2 = System.currentTimeMillis() - bef;
            System.out.println("For size: " + size + "\tTime in ms to pin 1024 buffers 100 times each: " + delta2);
        }

        assertTrue(true);
    }
    @Test
    public synchronized void testTask3() throws InterruptedException{
        NewBufferMgr bufferMgr = new NewBufferMgr(3);
        Buffer buff1 = bufferMgr.pin(new Block("FakeFile", 1));
        Buffer buff2 = bufferMgr.pin(new Block("FakeFile", 2));
        Buffer buff3 = bufferMgr.pin(new Block("FakeFile", 3));
        bufferMgr.unpin(buff2);
        wait(10^9);
        bufferMgr.unpin(buff3);
        wait(10^9);
        bufferMgr.unpin(buff1);
        wait(10^9);
        bufferMgr.pin(new Block("FakeFile", 4));
        System.out.println(bufferMgr.toString());
    }

    @Test
    public void testSimpleDB() throws Exception{
        String[] args = new String[1];
        args[0] = "testcs4432DB";
        simpledb.server.Startup.main(args);

        CreateStudentDB.main(args);
        Examples.main(args);
    }
}
