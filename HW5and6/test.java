/*
export CLASSPATH=~/Desktop/School/UT_grad_school/First_Year/CS386D_DBMS/HW5/postgresql.jar:.
javac test.java
java test
*/

import java.sql.*;
import java.util.*;

public class test {
	static int total = 5000000;

	public static void createTable(Connection conn) throws SQLException {
		System.out.println("creating table");
	    String createString = 
	    	"create table benchmark ("+ 
	     	"theKey integer primary key," +
	     	"columnA integer," +
	     	"columnB integer," +
	     	"filler char(247)" +
	     	")";
	    Statement stmt = conn.createStatement();
	    stmt.executeUpdate(createString);
	    stmt.close();
	}


	public static void insertRow(Connection conn) throws SQLException {
		// use batch insertion without autocommit to insert more rows at a time
		// generate total rows in sorted order on the primary key
		Random rand = new Random();
		System.out.println("generating " + total + " rows");
		String[] valuesToInsert = new String[total];
		for (int i = 0; i < total; ++i) {
			valuesToInsert[i] = "(" + i + ", " + rand.nextInt(50000) + ", " + rand.nextInt(50000) + ", 'xyz')";
		}
		System.out.println("generating " + total + " rows is done");


		// load those total rows to our database
		System.out.println("inserting rows");
		String insertPrefix = "insert into benchmark (theKey, columnA, columnB, filler) values ";
		Statement stmt = conn.createStatement();
		// start of transaction
		stmt.executeUpdate("begin");
		for (String eachValueToInsert : valuesToInsert) {
			String insertString = insertPrefix + eachValueToInsert;
			stmt.executeUpdate(insertString);
		}
		stmt.executeUpdate("commit");
		// end of transaction
		System.out.println("inserting rows is done");
	    stmt.close();
	}


	public static void printTable(Connection conn) throws SQLException {
		System.out.println("printing table");
	    String selectString = 
	    	"select * from benchmark";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(selectString);
		while (rs.next()) {
	    	System.out.println(rs.getString(1) + "," + rs.getString(2) + "," + rs.getString(3) + "," + rs.getString(4));
		}
		rs.close();
	    stmt.close();
	}


	public static void dropTable(Connection conn) throws SQLException {
		System.out.println("dropping table");
	    String dropString = 
	    	"drop table benchmark";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(dropString);
	    stmt.close();
	}


	public static void main(String[] args) throws SQLException, ClassNotFoundException {

		System.out.println("loading driver");
		Class.forName("org.postgresql.Driver");
		System.out.println("driver loaded");

		System.out.println("Connecting to DB");
		Connection conn = DriverManager.getConnection("jdbc:postgresql:p5", "xiang", "xgu");
		System.out.println("Connected to DB");

		try {
			// drops if there
			dropTable(conn);
		}
		catch (SQLException e) {}

		createTable(conn);

		final long startTime = System.currentTimeMillis();
		insertRow(conn);
		final long endTime = System.currentTimeMillis();
		System.out.println("Total time to insert " + total + " rows = " + (endTime - startTime) / 1000.0 + "s");


		// printTable(conn);
		// dropTable(conn);

	}

}