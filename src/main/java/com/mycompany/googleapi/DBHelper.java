/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.googleapi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author sean
 */
public class DBHelper {
    
    // using SQLite 3 database
    // integers are up to 8 bytes so use long data type in java
    
    private String dbURL;
    
    // database variables
    private Connection conn = null;
    private Statement stmt = null;
    private PreparedStatement prep = null;
    private ResultSet ress = null;
    private String sql = "";
    
    public DBHelper(String databaseName) {
        
        // check if database exists, if it doesn't create it
        
        this.dbURL = "jdbc:sqlite:resources/" + databaseName;
        
        // test - print out number of records in database
        //Connection c = null;
        //Statement stmt = null;
        //ResultSet rs = null;
        
        try {
            System.out.println("Opening database: " + databaseName);
            conn = DriverManager.getConnection(dbURL);

            ResultSet rs = conn.getMetaData().getTables(null, null, null, null);
            while (rs.next()) {
                System.out.println("   table: " + rs.getString("TABLE_NAME"));
            }
            // get number of rows in trans table
            stmt = conn.createStatement();
            sql = "SELECT COUNT(*) as numrecords FROM trans";
            ress = stmt.executeQuery(sql);
            
            System.out.printf("   found %d rows in table 'trans'\n", 
                    ress.getInt("numrecords"));
            
            conn.close();
                    
            // add budget_date column if it doesn't exist
            ensureBudgetDateColumn();

        } catch (Exception e){
            
            if (e.getMessage().contains("missing database")) {
                // create database
                System.out.println("Missing table - will create it");
                createTransTable();
            } else {
                System.out.println("Catch in opening database");
                System.err.println(e.getClass().getName() + ": " + e.getMessage() );
            }
           
        }
        
    }
    
    private void createTransTable() {
        // SQL statement for creating a new table
        sql = "CREATE TABLE trans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "date TEXT, " +
                "desc TEXT, " + 
                "amount REAL, " +
                "category TEXT)";
        
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            conn.commit();
            conn.close();
            System.out.println("Added table 'trans' to " + dbURL.toString());
        } catch (Exception e) {
            System.out.println("Catch in creating table 'trans'");
            System.err.println(e.getClass().getName() + ": " + e.getMessage() );            
        }
    }
    
    public boolean isInDB(Transaction t) {
        
        try {
            conn = DriverManager.getConnection(dbURL);
            stmt = conn.createStatement();
            sql = "select count(*) as count from trans where desc like '" + 
                    t.getDesc() + "' and amount = " + t.getAmount() + 
                    " and date = '" + t.getDateString() + "'";
            ress = stmt.executeQuery(sql);
            if (ress.getInt("count") == 0) {
                //System.out.println("......record not found");
                stmt.close();
                conn.close();
                return false;
            } else {
                //System.out.printf("found %d records\n", ress.getInt("count"));
                t.setIsInDB(true);
                stmt.close();
                conn.close();
                return true;
            }
        } catch (Exception e){
            System.out.println("Catch in isInDB()");
            System.err.println(e.getClass().getName() + ": " + e.getMessage() );
        }
        
        // only run if exception caught
        return false;
    }
    
    public long getTransID(Transaction trans) {
        try {
            conn = DriverManager.getConnection(dbURL);
            stmt = conn.createStatement();
            sql = "select id from trans where desc like '" + 
                    trans.getDesc() + "' and amount = " + trans.getAmount() + 
                    " and date = '" + trans.getDateString() + "'";
            ress = stmt.executeQuery(sql);
            Long tid = ress.getLong("id");
            stmt.close();
            conn.close();
            return tid;
        } catch (Exception e){
            System.out.println("Catch in getTransID()");
            System.err.println(e.getClass().getName() + ": " + e.getMessage() );
        }
        
        // only run if exception caught
        return -1;
    }
    
    public void addTransaction(Transaction trans) {
        
        try {
            //Class.forName("org.sqlite.JDBC");  already did this above
            conn = DriverManager.getConnection(dbURL);
            conn.setAutoCommit(false);

            // insert checked link into table
            stmt = conn.createStatement();
            String sql = "INSERT INTO trans (date, amount, desc, category) VALUES ('" + 
                    trans.getDateString() + "', " + trans.getAmount() + ", '" + 
                    trans.getDesc() + "', '" + trans.getCategoryName() + "');";
            stmt.executeUpdate(sql);
            conn.commit();
            System.out.printf("Added to DB: %s\n", trans.toString());
            stmt.close();
            conn.close();
        } catch (Exception e){
            System.out.println("Catch in addTransaction");
            System.err.println(e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return;
    }
    
    public void updateCategory(Transaction trans) {
        /*
        String query = "UPDATE userSettings SET coins=? WHERE user=?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, 10);
        statement.setString(2, lastUser);
        statement.executeUpdate();
        */
        
        long tid = getTransID(trans);
        
        try {
            //Class.forName("org.sqlite.JDBC");  already did this above
            conn = DriverManager.getConnection(dbURL);
            conn.setAutoCommit(false);
            
            String query = "UPDATE trans SET category='"+ trans.getCategoryName() +
                    "' WHERE id=" + tid;
            prep = conn.prepareStatement(query);
            prep.executeUpdate();
            
            conn.commit();
            System.out.printf("Updated category in DB: %s (id = %d)\n", 
                    trans.toString(), tid);
            prep.close();
            conn.close();
        } catch (Exception e){
            System.out.println("Catch in updateCategory");
            System.err.println(e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return;
    }
    
    public ArrayList<Transaction> getTransByDate(String month, String year) {

        ArrayList<Transaction> trans = new ArrayList<>();
        
        try {
            conn = DriverManager.getConnection(dbURL);
            stmt = conn.createStatement();
            sql = "select * from trans where STRFTIME('%m-%Y', date) = '" + 
                    monthToNum(month) + "-" + year + "'";
            ress = stmt.executeQuery(sql);
            while (ress.next()) {
                String ds = ress.getString("date");
                Float amt = ress.getFloat("amount");
                String dsc = ress.getString("desc");
                String cat = ress.getString("category");
                Long id = ress.getLong("id");
                Transaction tr = new Transaction(ds, amt, dsc, cat);
                trans.add(tr);
                System.out.println(ds + "---" + amt.toString() + "---" + ress.getString("desc"));
            }
            stmt.close();
            conn.close();
        } catch (Exception e){
            System.out.println("Catch in getTransByDate()");
            System.err.println(e.getClass().getName() + ": " + e.getMessage() );
        }

        return trans;
    }
    
    public String monthToNum(String month) {
        
        String[] months = {"January","February","March","April","May","June",
            "July","August","September","October","November","December"};
        
        for(int i=0; i<12; i++) {
            if (months[i].equals(month)) {
                return String.format("%02d", (i+1));
            }
        }
        
        // in case it didn't work
        return "00";
    }
    
    public void removePending() {
        
        try {
            //Class.forName("org.sqlite.JDBC");  already did this above
            conn = DriverManager.getConnection(dbURL);
            conn.setAutoCommit(false);
            
            String query = "DELETE FROM trans WHERE desc like 'POS DEBIT%' OR " +
                            " desc like 'POS CREDIT%' OR " +
                            " desc like 'ORIG CO NAME%'";
            prep = conn.prepareStatement(query);
            prep.executeUpdate();
            
            conn.commit();
                System.out.println("Removed all PENDING records from database");
            prep.close();
            conn.close();
        } catch (Exception e){
            System.out.println("Catch in removePending");
            System.err.println(e.getClass().getName() + ": " + e.getMessage() );
        }
        
        return;        
    }
    
    private void ensureBudgetDateColumn() {
        try {
            conn = DriverManager.getConnection(dbURL);
            stmt = conn.createStatement();

            // check if budget_date column exists
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(trans)");
            boolean found = false;
            while (rs.next()) {
                if ("budget_date".equals(rs.getString("name"))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                stmt.executeUpdate("ALTER TABLE trans ADD COLUMN budget_date TEXT");
                stmt.executeUpdate("UPDATE trans SET budget_date = date WHERE budget_date IS NULL");
                System.out.println("Added budget_date column and backfilled from date");
            }

            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("Catch in ensureBudgetDateColumn()");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
