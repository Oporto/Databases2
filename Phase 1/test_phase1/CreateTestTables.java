/******************************************************************/import java.sql.Connection;import java.sql.Driver;import java.sql.SQLException;import java.sql.Statement;import java.util.HashMap;import java.util.Random;import simpledb.remote.SimpleDriver;public class CreateTestTables { final static int maxSize=200; /**  * @param args  */ public static void main(String[] args) {  Connection conn=null;  Driver d = new SimpleDriver();  String host = "localhost"; //you may change it if your SimpleDB server is running on a different machine  String url = "jdbc:simpledb://" + host;  String qry="Create table test1" +  "( a1 int," +  "  a2 int"+  ")";  Random rand=null;  Statement s=null;  Long start;  HashMap<Integer, Long> time_map= new HashMap<Integer, Long>();  try {   System.out.println("Started to create tables");   conn = d.connect(url, null);   s=conn.createStatement();   s.executeUpdate("Create table test1" +     "( a1 int," +     "  a2 int"+   ")");   s.executeUpdate("Create table test2" +     "( a1 int," +     "  a2 int"+   ")");   s.executeUpdate("Create table test3" +     "( a1 int," +     "  a2 int"+   ")");   s.executeUpdate("Create table test4" +     "( a1 int," +     "  a2 int"+   ")");   s.executeUpdate("Create table test5" +     "( a1 int," +     "  a2 int"+   ")");   System.out.println("Started to create indexes");   s.executeUpdate("create sh index idx1 on test1 (a1)");   s.executeUpdate("create ex index idx2 on test2 (a1)");   s.executeUpdate("create bt index idx3 on test3 (a1)");   System.out.println("Indexes created, inserting values");   for(int i=1;i<6;i++)   {    start = System.nanoTime();    if(i!=5)    {     rand=new Random(1);// ensure every table gets the same data     for(int j=0;j<maxSize;j++)     {      s.executeUpdate("insert into test"+i+" (a1,a2) values("+rand.nextInt(1000)+","+rand.nextInt(1000)+ ")");     }    }    else//case where i=5    {     for(int j=0;j<maxSize/2;j++)// insert 10000 records into test5     {      s.executeUpdate("insert into test"+i+" (a1,a2) values("+j+","+j+ ")");     }    }    time_map.put(i, System.nanoTime() - start);   }   conn.close();   for (int k = 1; k < 6;k++){    switch (k){     case 1:      System.out.println("Using static hash index:");      break;     case 2:      System.out.println("Using extensible hash index:");      break;     case 3:      System.out.println("Using b-tree index:");      break;     default:      System.out.println("No index:");    }    System.out.println("For table " + k + " the addition of " + maxSize + " took: " + time_map.get(k) + " nanoseconds");   }  } catch (SQLException e) {   e.printStackTrace();  }finally  {   try {    conn.close();   } catch (SQLException e) {    e.printStackTrace();   }  } }}