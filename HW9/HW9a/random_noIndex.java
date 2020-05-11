/*
export CLASSPATH=/home/xiang/Desktop/School/UT_grad_school/First_Year/CS386D_DBMS/HW9/postgresql.jar:.
javac random_noIndex.java
java random_noIndex
*/

import java.sql.*;
import java.util.*;

public class random_noIndex {
	static int totalRows = 5000000; // totalRows rows
	static String physicalConfig = "Loading random inserts, No index!!!\n";
	static int maxIterations = 25; // average over this many trials

	public static void createTable(Connection conn) throws SQLException {
		System.out.println("creating table");
	    String createString = 
	    	"create table table_a ("+ 
	     	"theKey integer primary key," +
	     	"ht integer," +
	     	"tt integer," +
	     	"ot integer," +
	     	"hund integer," +
	     	"ten integer," +
	     	"filler character(247)" +
	     	")";
	    Statement stmt = conn.createStatement();
	    stmt.executeUpdate(createString);
	    stmt.close();
	}

	// public static void dropSecondaryIndex(Connection conn, String indexName) throws SQLException {
	// 	System.out.println("dropping secondary index");
	// 	String dropSecondaryIndexString = "drop index " + indexName;
	// 	Statement stmt = conn.createStatement();
	// 	stmt.executeUpdate(dropSecondaryIndexString);
	//     stmt.close();
	// }

	// public static void createSecondaryIndex(Connection conn, String attributeName) throws SQLException {
	// 	// name convention: the index name will be index_attributeName
	// 	System.out.println("creating secondary index");
	// 	String createSecondaryIndexString = "create index index_" + attributeName + " on table_a(" + attributeName + ")";
	// 	Statement stmt = conn.createStatement();
	// 	stmt.executeUpdate(createSecondaryIndexString);
	//     stmt.close();
	// }

	// public static double timeQuery(Connection conn, int queryType) throws SQLException {
	// 	Random rand = new Random();
	// 	int[] randQueryTarget = new int[maxIterations]; // 10 random but fixed query target
	// 	double[] queryExecutionTimes = new double[maxIterations];
	// 	for (int i = 0; i < maxIterations; ++i) {
	// 		randQueryTarget[i] = rand.nextInt(50000);
	// 		queryExecutionTimes[i] = 0.0;
	// 	}

	// 	Statement stmt = conn.createStatement();
	// 	String queryString;
	// 	for(int iter = 0; iter < maxIterations; ++iter) {
	// 		if (queryType == 0)
	// 			queryString = "select * from table_a where table_a.columnA = " + randQueryTarget[iter];
	// 		else if (queryType == 1)
	// 			queryString = "select * from table_a where table_a.columnB = " + randQueryTarget[iter];
	// 		else if (queryType == 2)
	// 			queryString = "select * from table_a where table_a.columnA = " + randQueryTarget[iter] + " and table_a.columnB = " + randQueryTarget[iter];
	// 		else
	// 			throw new SQLException("unrecognizable queryType. Must be either 0, or 1, or 2.");
	// 		long startTime = System.currentTimeMillis();
	// 		stmt.executeQuery(queryString);
	// 		long endTime = System.currentTimeMillis();
	// 		queryExecutionTimes[iter] = (endTime - startTime) / 1000.0;
	// 	}
	// 	stmt.close();
	// 	// compute the average and return
	// 	double sumExecutionTime = 0.0;
	// 	for (double queryExecutionTime : queryExecutionTimes)
	// 		sumExecutionTime += queryExecutionTime;
	// 	return sumExecutionTime / maxIterations;

	// }


	// use batch insertion without autocommit to insert more rows at a time
	public static double timeInsertRow(Connection conn) throws SQLException {
		// generate totalRows rows in random order on the primary key
		Random rand = new Random();
		System.out.println("generating " + totalRows + " rows");
		String[] valuesToInsert = new String[totalRows];
		for (int i = 0; i < totalRows; ++i) {
			valuesToInsert[i] = "(" + i + ", " 
								+ rand.nextInt(100000) + ", "  
								+ rand.nextInt(10000) + ", "
								+ rand.nextInt(1000) + ", " 
								+ rand.nextInt(100) + ", " 
								+ rand.nextInt(10) + ", " 
								+ "'xyz')";
		}
		Collections.shuffle(Arrays.asList(valuesToInsert));
		System.out.println("generating " + totalRows + " rows is done");


		// load those totalRows rows to our database
		System.out.println("inserting " + totalRows + " rows");
		String insertPrefix = "insert into table_a (theKey, ht, tt, ot, hund, ten, filler) values ";
		Statement stmt = conn.createStatement();
		
		// batch loading within one transaction
		long startTime = System.currentTimeMillis();
		for (String eachInsertString : valuesToInsert) {
			stmt.addBatch(insertPrefix + eachInsertString);
		}
		stmt.executeBatch();
		long endTime = System.currentTimeMillis();
		double timeToInsertRows = (endTime - startTime) / 1000.0;

		// end of transaction
		System.out.println("inserting " + totalRows + " rows is done");
	    stmt.close();
	    return timeToInsertRows;
	}


	public static void printTable(Connection conn) throws SQLException {
		System.out.println("printing table");
	    String selectString = 
	    	"select * from table_a";
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
	    	"drop table table_a";
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
		String url = "jdbc:postgresql://db-1-cs386d.cnjru9krormz.us-east-1.rds.amazonaws.com/cs386d";
		String user = "postgres";
		String pwd = "18708774845gxx";
		Connection conn = DriverManager.getConnection(url, user, pwd);
		// Connection conn = DriverManager.getConnection("jdbc:postgresql:p5", "xiang", "xgu");
		System.out.println("Connected to DB");

		try {
			// drops if there
			dropTable(conn);
		}
		catch (SQLException e) {}

		createTable(conn);

		// // drops any secondary index if there
		// try {
		// 	dropSecondaryIndex(conn, "index_columnA");
		// } catch (SQLException e) {}
		// try {
		// 	dropSecondaryIndex(conn, "index_columnB");
		// } catch (SQLException e) {}

		// // create secondary index
		// createSecondaryIndex(conn, "columnA");
		// createSecondaryIndex(conn, "columnB");

		// generate and insert rows
		double loadingTime = timeInsertRow(conn);
		System.out.println("\nTime to insert " + totalRows + " rows = " + loadingTime + "s\n");

		// // execute three queries
		// for (int queryType = 0; queryType < 3; ++queryType) {
		// 	double averageQueryExecutionTime = timeQuery(conn, queryType);
		// 	System.out.println("Average time to execute query " + queryType + " over " + maxIterations + " trials = " + averageQueryExecutionTime + "s\n");
		// }

		// printTable(conn);

		// drop the table
		// dropTable(conn);
		// // drops all secondary index
		// try {
		// 	dropSecondaryIndex(conn, "index_columnA");
		// } catch (SQLException e) {}
		// try {
		// 	dropSecondaryIndex(conn, "index_columnB");
		// } catch (SQLException e) {}

	}

}