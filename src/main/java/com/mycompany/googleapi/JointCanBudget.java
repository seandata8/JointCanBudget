/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.mycompany.googleapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;

/**
 *
 * @author sean
 */
public class JointCanBudget extends Application {

    // Program to load, store and send to Google sheets transactions from joint Chase card
    /*
    Transactions
        o blue are already in database
        o black are new or were modified and need to up saved to database
        o if category changed, color purple?
    
    Buttons on main screen
        o Update (month/year)
            - load transactions from database for this month/year, clear others
        o Load (from file)
            - only load transactions for that month/year
            - only load transactions that aren't already in the database
            - only load transactions that aren't part of the current list
            - show as black
            - add to current screen and put in order of date
            - resort list from oldest to newest after loading
        o Save to Database
            - must have a category defined
            - only black ones need to be saved
            - after saving, keep same list of transactions but recolor
            - if category changed and it's in the database, update that as well
        o Upload to Sheets
            - upload all transactions in current month-year to Sheets on Drive
    */
    
    
    //static DBHelper myDB = new DBHelper("sdccu.sqlite");
    //static DBHelper myDB = new DBHelper("chase3.sqlite");
    static DBHelper myDB = new DBHelper("tdjoint.sqlite");
    static FXHelper myFX = new FXHelper();   
    static SheetsHelper mySheets;
    static DriveHelper myDrive;
    
    static ArrayList<Category> categories = new ArrayList<>();
    static ArrayList<Transaction> transactions = new ArrayList<>();

    static Scanner input = new Scanner(System.in);

    
    @Override
    public void start(Stage stage) {

        // organization
        /*  stage [Stage}
            --scene [Scene] (with setScene)
                --box [VBox] (added with new Scene)
                    --fileName [Label]
                    --sp [ScrollPane]
                        --vb [VBox] (added with setContent)
                            -- pics [Image] (added with getChildren().add)
        */
        VBox mainBox = new VBox();
        HBox chooserHB = new HBox();
        ScrollPane transSP = new ScrollPane();
        //VBox transVP = new VBox();
        Scene scene = new Scene(mainBox, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Scroll Pane");
        HBox displaySourceHBox = myFX.getDisplaySourceHBox();
        Separator hsep = new Separator(Orientation.HORIZONTAL);
        mainBox.getChildren().addAll(chooserHB, hsep, displaySourceHBox, transSP);
        VBox.setVgrow(transSP, Priority.ALWAYS);
        
        // create chooser HBox
        // - spinner with months
        // - spinner with years
        // - button to Update
        // - dropdown with files
        // - button to Load transactions
        
        // Load elements from myFX
        ComboBox fileComboBox = myFX.getComboBoxFile();
        Button loadButton = myFX.getButtonLoad();
        Separator sep1 = new Separator(Orientation.VERTICAL);
        Spinner monthSpinner = myFX.getSpinnerMonth();   
        Spinner yearSpinner = myFX.getSpinnerYear();        
        //Button updateButton = myFX.getButtonUpdate();
        Separator sep2 = new Separator(Orientation.VERTICAL);
        Button saveButton = myFX.getButtonSave();
        Separator sep3 = new Separator(Orientation.VERTICAL);
        Button uploadButton = myFX.getButtonUpload();
        VBox transactionsVBox = myFX.getVBoxTransactions();

        
        // add all elements to chooser HBox
        chooserHB.setSpacing(10);
        chooserHB.setMinHeight(50);
        chooserHB.setAlignment(Pos.CENTER);
        //     root.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
        //+ "-fx-border-width: 2;" + "-fx-border-insets: 5;"
        //+ "-fx-border-radius: 5;" + "-fx-border-color: blue;");
        //chooserHB.setStyle("-fx-border-style: solid inside;");
        chooserHB.getChildren().addAll(monthSpinner, yearSpinner,
                sep1, fileComboBox, loadButton, sep2, saveButton, sep3, 
                uploadButton);
                
        // add Transactions list
        transSP.setVmax(440);
        transSP.setPrefSize(115, 150);
        transSP.setContent(transactionsVBox);

        stage.show();
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        //SheetsHelper mySheets = new SheetsHelper();
        //DriveHelper myDrive = new DriveHelper();
                
        mySheets = new SheetsHelper();
        myDrive = new DriveHelper();
        
        // valid token test
        try {
            String id = myDrive.getSheetID("test");
            // id = null but that's okay, at least it didn't trigger an exception
            System.out.println("id = " + id);
        } catch (IOException ex) {
            if (ex.getMessage().contains("400 Bad Request")) {
                System.out.println("WARNING: tokens out of date -- need to refresh");
                // delete tokens
                String filepath = System.getProperty("user.dir") + "/tokens-si/StoredCredential";
                File tok = new File(filepath);
                if (tok.delete()) {
                    System.out.println("Deleted /tokens-si/StoredCredential");
                };        
                filepath = System.getProperty("user.dir") + "/tokens-dq/StoredCredential";
                tok = new File(filepath);
                if (tok.delete()) {
                    System.out.println("Deleted /tokens-dq/StoredCredential");
                }; 
                // now create mySheets and myDrive again
                mySheets = new SheetsHelper();
                myDrive = new DriveHelper();                
            } else {
                System.out.println("Error in Main main(): valid token test " +
                    ex.getMessage());  
            }
        }
        
        // add each category with keywords
        categories.add(new Category("Restaurants", "hortons", "r"));
        categories.add(new Category("Groceries","longos,metro,purdys,food basics,lcbo,galleria", "g"));
        categories.add(new Category("Extra","amazon,amzn,radio-canada", "e"));
        categories.add(new Category("Fuel & Subway","pres/,shell,ttc", "f"));
        //categories.add(new Category("Condo Insurance","mercury", "c"));
        categories.add(new Category("Cell Phone","koodo", "p"));
        categories.add(new Category("Cleaners","cleaners", "w"));
        categories.add(new Category("Insurance","square one", "i"));
        categories.add(new Category("IGNORE","msp", "x"));
        
        // remove pending charges from database
        // -- these will be reloaded from file and uploaded again, but shouldn't
        //    be part of the database because they are temporary
        // includes "POS DEBIT", "POS CREDIT" and "ORIG CO NAME"
        myDB.removePending();
        
        // this jumps to start
        launch(args);        
    }
    
}
