/*
export CLASSPATH=~/Desktop/School/UT_grad_school/First_Year/CS386D_DBMS/HW5/postgresql.jar:.
javac sorted_indexOnColB.java
java sorted_indexOnColB
*/

import java.sql.*;
import java.util.*;

public class sorted_indexOnColB {
	static int total = 5000000; // total rows
	static String physicalConfig = "Sorted Order, Index on columnB!!!\n";
	static int maxIterations = 25; // average over this many trials

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

	public static void dropSecondaryIndex(Connection conn, String indexName) throws SQLException {
		System.out.println("dropping secondary index");
		String dropSecondaryIndexString = "drop index " + indexName;
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(dropSecondaryIndexString);
	    stmt.close();
	}

	public static void createSecondaryIndex(Connection conn, String attributeName) throws SQLException {
		// name convention: the index name will be index_attributeName
		System.out.println("creating secondary index");
		String createSecondaryIndexString = "create index index_" + attributeName + " on benchmark(" + attributeName + ")";
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(createSecondaryIndexString);
	    stmt.close();
	}

	public static double timeQuery(Connection conn, int queryType) throws SQLException {
		Random rand = new Random();
		int[] randQueryTarget = new int[maxIterations]; // 10 random but fixed query target
		double[] queryExecutionTimes = new double[maxIterations];
		for (int i = 0; i < maxIterations; ++i) {
			randQueryTarget[i] = rand.nextInt(50000);
			queryExecutionTimes[i] = 0.0;
		}

		Statement stmt = conn.createStatement();
		String queryString;
		for(int iter = 0; iter < maxIterations; ++iter) {
			if (queryType == 0)
				queryString = "select * from benchmark where benchmark.columnA = " + randQueryTarget[iter];
			else if (queryType == 1)
				queryString = "select * from benchmark where benchmark.columnB = " + randQueryTarget[iter];
			else if (queryType == 2)
				queryString = "select * from benchmark where benchmark.columnA = " + randQueryTarget[iter] + " and benchmark.columnB = " + randQueryTarget[iter];
			else
				throw new SQLException("unrecognizable queryType. Must be either 0, or 1, or 2.");
			long startTime = System.currentTimeMillis();
			stmt.executeQuery(queryString);
			long endTime = System.currentTimeMillis();
			queryExecutionTimes[iter] = (endTime - startTime) / 1000.0;
		}
		stmt.close();
		// compute the average and return
		double sumExecutionTime = 0.0;
		for (double queryExecutionTime : queryExecutionTimes)
			sumExecutionTime += queryExecutionTime;
		return sumExecutionTime / maxIterations;

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
		System.out.println("inserting " + total + " rows");
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
		System.out.println("inserting " + total + " rows is done");
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
		System.out.println(physicalConfig);


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

		// drops any secondary index if there
		try {
			dropSecondaryIndex(conn, "index_columnA");
		} catch (SQLException e) {}
		try {
			dropSecondaryIndex(conn, "index_columnB");
		} catch (SQLException e) {}

		// create secondary index
		createSecondaryIndex(conn, "columnB");

		// generate and insert rows
		long startTime = System.currentTimeMillis();
		insertRow(conn);
		long endTime = System.currentTimeMillis();
		System.out.println("\nTime to insert " + total + " rows = " + (endTime - startTime) / 1000.0 + "s\n");

		// execute three queries
		for (int queryType = 0; queryType < 3; ++queryType) {
			double averageQueryExecutionTime = timeQuery(conn, queryType);
			System.out.println("Average time to execute query " + queryType + " over " + maxIterations + " trials = " + averageQueryExecutionTime + "s\n");
		}

		// printTable(conn);

		// drop the table
		dropTable(conn);
		// drops all secondary index
		try {
			dropSecondaryIndex(conn, "index_columnA");
		} catch (SQLException e) {}
		try {
			dropSecondaryIndex(conn, "index_columnB");
		} catch (SQLException e) {}

	}

}