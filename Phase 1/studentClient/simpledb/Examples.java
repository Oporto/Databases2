import java.sql.*;
import simpledb.remote.SimpleDriver;

public class Examples
{
	public static void main(String[] args) {
		Connection conn = null;
		try {
			Driver d = new SimpleDriver();
			conn = d.connect("jdbc:simpledb://localhost", null);
			Statement stmt = conn.createStatement();
			ResultSet rs;
			//times for testing the sql jdbc document
			//table set up taken directly from CreatStudentDB but with more statements added below
			long totalStartTime = System.nanoTime();
			
			
			//Variety of testing statements, I will just be printing out the names for the result set for ease of use
			//and will be checking against the above inserts
			
			//statements to check that the above data is correctly in tables
			String s = "select SId, Sname, MajorId, GradYear from student";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Select from student test \n");
			while (rs.next()) {
				String StudentID = rs.getString("SName");
				System.out.println(StudentID + "\n");
			}
			
			s = "select DId, DName from dept";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Select from dept test \n");
			while (rs.next()) {
				String DeptID = rs.getString("DName");
				System.out.println(DeptID + "\n");
			}
			
			s = "select CId, Title, DeptId from course";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Select from course test \n");
			while (rs.next()) {
				String CourseID = rs.getString("Title");
				System.out.println(CourseID + "\n");
			}
			
			s = "select SectId, CourseId, Prof, YearOffered from section";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Select from section test \n");
			while (rs.next()) {
				Integer SectionID = rs.getInt("SectID");
				System.out.println(SectionID + "\n");
			}
			
			s = "select EId, StudentId, SectionId, Grade from enroll";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Select from enroll test \n");
			while (rs.next()) {
				Integer EnrollID = rs.getInt("EID");
				System.out.println(EnrollID + "\n");
			}
			
			//statements to check conditionals on Student
			s = "select SId, Sname from student where sid = 3 and majorid = 10";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Conditional1 from student test \n");
			while (rs.next()) {
				String StudentID = rs.getString("SName");
				System.out.println(StudentID + "\n");
			}
			
			s = "select SId, Sname from student where majorid = 10";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Conditional2 from student test \n");
			while (rs.next()) {
				String StudentID = rs.getString("SName");
				System.out.println(StudentID + "\n");
			}
			
			s = "select Sid, Sname from student where gradyear = 2005 and sname = 'sue'";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Conditional3 from student test \n");
			while (rs.next()) {
				String StudentID = rs.getString("SName");
				System.out.println(StudentID + "\n");
			}
			
			//statement to check conditional on Dept
			s = "select DId, DName from dept where Did = 10";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Conditional from dept test \n");
			while (rs.next()) {
				String DeptID = rs.getString("DName");
				System.out.println(DeptID + "\n");
			}
			
			//additional testing for course
			s = "select Cid, title from course";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Conditional from course test \n");
			while (rs.next()) {
				String CourseID = rs.getString("Title");
				System.out.println(CourseID + "\n");
			}
			
			//final testing by insertion and then checking the data again to make sure it worked properly
			s = "insert into student(sid, sname, majorid, gradyear) values (1, 'test', 10, 2016)";
			stmt.executeUpdate(s);
			s = "select Sid, Sname from student where gradyear = 2016 and majorid = 10";
			rs = stmt.executeQuery(s);
			System.out.println("Printing Conditional4 from student after update test \n");
			while (rs.next()) {
				String StudentName = rs.getString("SName");
				System.out.println(StudentName + "\n");
			}
			
			long totalEndTime = System.nanoTime();
			System.out.println(String.format("Total Time elapsed: %.3f ms\n", ((float)(totalEndTime -totalStartTime))/1000000.0));
			
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (conn != null)
					conn.close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
