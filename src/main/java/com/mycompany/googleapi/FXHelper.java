/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.googleapi;

import static com.mycompany.googleapi.JointCanBudget.categories;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Scanner;
import static javafx.application.Platform.exit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import static com.mycompany.googleapi.JointCanBudget.myDB;
import static com.mycompany.googleapi.JointCanBudget.myDrive;
import static com.mycompany.googleapi.JointCanBudget.mySheets;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javafx.scene.text.Font;

/**
 *
 * @author sean
 */
public class FXHelper {
    // sub-routines for JavaFX, e.g. create spinners
    
    // buttons and spinner need to be accessible to the other elements
    ComboBox comboboxFile;
    Spinner spinnerMonth;
    Spinner spinnerYear;
    Button buttonLoad;
    Button buttonUpdate;
    Button buttonSave;
    Button buttonUpload;
    ArrayList<Transaction> transactions = new ArrayList<>();
    ArrayList<String> choices = new ArrayList<>();
    VBox vboxTransactions;
    File[] files;
    Label labelDisplaySource;
    HBox hboxDisplaySource;
    String currentYearMM;
    
    FXHelper() {
        // do nothing
    }
    
    public VBox getVBoxTransactions() {
        
        vboxTransactions = new VBox();

        // add text box with each transaction
        // clear list of choices
        choices.clear();
        // add blank
        choices.add("");
        for (Category cat : categories) {
            choices.add(cat.getName());
        }
        // spacing between items
        vboxTransactions.setSpacing(5);
        for (Transaction trans : transactions) {
            vboxTransactions.getChildren().add(trans.getHBox(choices));
        }
        
        // fill initial vBox with transactions from current month & year
        updateTransactions();
        
        return vboxTransactions;
    }
    
    public ComboBox getComboBoxFile() {
        
        // load download.csv list and give user option for which one to load
        // Path of the specific directory  
        String directoryPath = "/Users/sean/Downloads"; 
        //Scanner input = new Scanner(System.in); //System.in is a standard input stream  
        
        // Using File class create an object for specific directory 
        File directory = new File(directoryPath); 

        // Using listFiles method we get all the files of a directory  
        // return type of listFiles is array 
        files = directory.listFiles(); 
        
        // sort by date
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        
        // Get name of the all files present in that path 
        File selFile = null;
        ArrayList<String> filenames = new ArrayList<>();
        String defFile = "";
        if (files != null) { 
            int num = 1;
            for (int i=files.length-1; i>=0; i--) { 
                String name = files[i].getName();
                if (name.matches("accountactivity.*csv")) {
                    //System.out.printf("%d: %s (%s)\n", num, name, new Date(files[i].lastModified())); 
                    filenames.add(name);
                    if (num == 1) {
                        defFile = name;
                    }
                    num++;
                }
                //if (num >= 5) break;
            }
            // ask user to select file
            /*
            System.out.printf("Select download file (Enter for 1st):\n");  
            String selStr = input.nextLine();              //reads string
            if (selStr.isEmpty()) selStr = "1";
            Integer sel = null;
            try {
                sel = Integer.parseInt(selStr);
            } catch (NumberFormatException nfe) {
                
            }
            if (sel != null && sel > 0 && sel <= downloads.size()) {
                selFile = downloads.get(sel-1);
            }
            */
        }                 

        //Spinner<String> spFile = new Spinner<>();
        comboboxFile = new ComboBox<String>();
        ObservableList<String> fileOL = FXCollections.observableList(filenames);
        /*
        SpinnerValueFactory<String> valueFactory = 
               new SpinnerValueFactory.ListSpinnerValueFactory<>(fileOL);
        if (defFile.isEmpty() == false) {
            valueFactory.setValue(defFile);
        }
        */
        comboboxFile.setItems(fileOL);
        comboboxFile.setValue(fileOL.get(0));
        //comboboxFile.setValueFactory(valueFactory);
        comboboxFile.setMinWidth(300);
        return comboboxFile;        
    }
    
    public Spinner getSpinnerMonth() {

        // SPINNER WITH MONTHS
        ObservableList<String> months = FXCollections.observableArrayList(
               "January", "February", "March", "April", 
               "May", "June", "July", "August", 
               "September", "October", "November", "December");
        FXCollections.reverse(months);
        // create spinner
        //Spinner<String> monthSpinner = new Spinner<>();
        spinnerMonth = new Spinner<String>();
        // Value factory.
        SpinnerValueFactory<String> valueFactory = 
               new SpinnerValueFactory.ListSpinnerValueFactory<>(months);
        valueFactory.setWrapAround(true);
        // Default value - get current month
        LocalDate currentdate = LocalDate.now();
        //Getting the current month
        Month currentMonth = currentdate.getMonth();
        valueFactory.setValue(currentMonth.getDisplayName(TextStyle.FULL, Locale.US));
        // set the current month
        spinnerMonth.setValueFactory(valueFactory);
        spinnerMonth.setMaxWidth(110);

        // update category when catCB is changed
        // Listener
        spinnerMonth.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                updateTransactions();
            }
        });
        
        return spinnerMonth;
    }
    
    public Spinner getSpinnerYear() {
        
        // Default value - get current month
        LocalDate currentdate = LocalDate.now();
        //Getting the current month
        int currentYear = currentdate.getYear();

        //Spinner spYear = new Spinner();
        spinnerYear = new Spinner();
        ArrayList<Integer> years = new ArrayList<>();
        for (int i=currentYear; i>=2024; i--) {
            years.add(i);
        }
        
        ObservableList<Integer> yearsOL = FXCollections.observableList(years);
        SpinnerValueFactory<Integer> valueFactory = 
               new SpinnerValueFactory.ListSpinnerValueFactory<>(yearsOL);
        valueFactory.setValue(currentYear);
        spinnerYear.setValueFactory(valueFactory);
        spinnerYear.setMaxWidth(80);
        
        return spinnerYear;
    }
    
    public Button getButtonUpdate() {
        
        //Button update = new Button("Update");
        buttonUpdate = new Button("Update");

        buttonUpdate.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println("Save update pressed!");
                    updateTransactions();
                }
            }
        );
       
        return buttonUpdate;
    }
    
    private void updateTransactions() {
        
        // loads transactions from the database based on the month and year
        String month = spinnerMonth.getValue().toString();
        String year = spinnerYear.getValue().toString();
        // set CurrentMonthYY
        currentYearMM = year + "-" + myDB.monthToNum(month);
        
        System.out.printf("Updating transactions from %s %s\n", month, year);
        //System.out.println(labelDisplaySource);
        labelDisplaySource.setText("Displaying transactions from " + month +
                " " + year);
        
        // load from database
        transactions = myDB.getTransByDate(month, year);
        
        // display
        updateVBoxTransactions();
        
    }
    
    public Button getButtonSave() {
        
        // save transactions on the screen to the database (either update or create new)
        buttonSave = new Button("Save to Database");

        // action event 
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() { 
            public void handle(ActionEvent e) 
            { 
                System.out.println("Save button pressed!");
                int c = 1;
                // go through transactions
                for (Transaction trans : transactions) {
                    System.out.printf("testing trans #%d - in DB = %s\n",
                            c, myDB.isInDB(trans));
                    c++;
                    // update category if it was changed and it's in the DB
                    if (myDB.isInDB(trans) == true &&
                            trans.wasCatChanged() == true) {
                        myDB.updateCategory(trans);
                    }
                    
                    // update budget_date if it was changed and it's in the DB
                    if (myDB.isInDB(trans) == true &&
                            trans.wasBudgetDateChanged() == true) {
                        myDB.updateBudgetDate(trans);
                    }

                    // add to database if not already in DB and if category is set
                    if (myDB.isInDB(trans) == false &&
                            trans.getCategoryName().isEmpty() == false) {
                        myDB.addTransaction(trans);
                        trans.setIsInDB(true);
                    }
                }
                System.out.println("Added transactions to database");
                
                // update all transactions for month-year from database
                updateTransactions();
                // update list (just with colors)
                //updateVBoxTransactions();
            } 
        };
        buttonSave.setOnAction(event);
        
        return buttonSave;
    }
    
    public Button getButtonLoad() {
        //Button load = new Button("Load");
        buttonLoad = new Button("Load");

        buttonLoad.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        loadTransactions();
                    } catch (FileNotFoundException fnfe) {
                        System.out.println("ERROR: Could not load transactions.");
                    }
                }
            }
        );
        
        return buttonLoad;        
    }
    
    public Button getButtonUpload() {
        //Button load = new Button("Load");
        buttonUpload = new Button("Upload to Sheets");

        buttonUpload.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    // upload current month-year to google sheets
                    String sheetname = currentYearMM + "-Budget";
                    System.out.printf("Gettting file id for %s sheet\n",
                            sheetname);
                    String sheetid = null;
                    try {
                        sheetid = myDrive.getSheetID(sheetname);
                    } catch (IOException ex) {
                        System.out.println("Error in myDrive.getSheetID(): " +
                                ex.getMessage());
                        
                    }
                    System.out.printf("%s id is %s\n", sheetname, sheetid);
                    try {
                        // list sheets in that sheetid
                        mySheets.readRange(sheetid);
                    } catch (IOException ex) {
                        System.out.println("Error in mySheets.readRange(): " +
                                ex.getMessage());
                    }
                    // get values Lists from Transactions
                    List<List<Object>> vals = getValuesFromTrans();
                    try {
                        mySheets.writeTransactions(sheetid, vals);
                    } catch (IOException ex) {
                        System.out.println("Error in mySheets.writeTransactions(): " +
                                ex.getMessage());
                    }
                    
                }
            }
        );
        
        return buttonUpload;        
    }
    
    private void loadTransactions() throws FileNotFoundException {
        
        // add transactions to current list if
        /*
            - the date is within the current month
            - it's not already in the list
            - it's not already in the database
        */
        
        // get the file from the comboBox
        String filename = comboboxFile.getValue().toString();
        System.out.println("Loading file: " + filename);        
        File dlfile = null;
        
        // go through files array and find file with the selected filename
        for (File f : files) {
            if (f.getName().equals(filename)) {
                dlfile = f;
                break;
            }
        }
        
        // read in transactions using CSV tools
        if (dlfile != null) {
            System.out.printf("\nCSV Reading in transactions from %s\n", dlfile.getName());
            try (Reader reader = new FileReader(dlfile);                    
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     //.withFirstRecordAsHeader() // Use if your CSV has headers
                     .withIgnoreHeaderCase()
                     .withTrim())) {
                int count = 0;    
                for (CSVRecord record : csvParser) {
                    Transaction trans = new Transaction(record);

                    // Now decide if it should be added to the list
                    // 1. is it in the current month
                    // 2. not already in transactions list
                    // 3. not already in the database
                    if (currentYearMM.equals(trans.getYearMM()) == true &&
                            trans.isIn(transactions) == false &&
                            myDB.isInDB(trans) == false) {

                        transactions.add(trans);
                        // guess category
                        trans.guessCategory();
                        System.out.println(trans.toString());
                        count++;
                    }
                }
                System.out.printf("Loaded %d transactions\n", count);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("No download file selected. Exiting...");
                exit();            }            
        }
        
        /*
        // get all the transactions from the file
        if (dlfile != null) {
            System.out.printf("\nReading in transactions from %s\n", dlfile.getName());
                    // read wordle list and put in array
            //parsing a CSV file into Scanner class constructor  
            Scanner sc = new Scanner(dlfile);  

            sc.useDelimiter("\n");   //sets the delimiter pattern
            int count = 0;
            // read first line which are headers
            sc.next();
            while (sc.hasNext()) {
                String line = sc.next();
                //System.out.println(line);
                Transaction trans = new Transaction(line);
                
                // Now decide if it should be added to the list
                // 1. is it in the current month
                // 2. not already in transactions list
                // 3. not already in the database
                if (currentYearMM.equals(trans.getYearMM()) == true &&
                        trans.isIn(transactions) == false &&
                        myDB.isInDB(trans) == false) {
                    
                    transactions.add(trans);
                    // guess category
                    trans.guessCategory();
                    System.out.println(trans.toString());
                    count++;
                }
            }
            sc.close();
            System.out.printf("Loaded %d transactions\n", count);
        } else {
            System.out.println("No download file selected. Exiting...");
            exit();
        }        
        */
        
        // refresh list of transactions
        updateVBoxTransactions();
        
        // update message for what is displayed
        String currText = labelDisplaySource.getText();
        labelDisplaySource.setText(currText + " & " + filename);
    }
    
    private void updateVBoxTransactions() {
        // add them to VBox
        vboxTransactions.getChildren().clear();
        
        // sort the transactions
        // example: anagrams.sort(Comparator.comparing(Word::getPoints));
        transactions.sort(Comparator.comparing(Transaction::getDateString));
        
        for (Transaction trans : transactions) {
            vboxTransactions.getChildren().add(trans.getHBox(choices));
        }        
    }
    
    public HBox getDisplaySourceHBox() {
        
        hboxDisplaySource = new HBox();
        
        labelDisplaySource = new Label("display source of displayed transactions");
        labelDisplaySource.setFont(new Font(16.0));
        hboxDisplaySource.setAlignment(Pos.CENTER);
        hboxDisplaySource.getChildren().add(labelDisplaySource);
        
        return hboxDisplaySource;
    }
    
    private List<List<Object>> getValuesFromTrans() {
        
        // return list of transactions and fill the rest with blanks up to 100
        
        List<List<Object>> values = new ArrayList<>();
        
        List<Object> blanks = new ArrayList<>();
        for (int i=0; i<4; i++) {
            blanks.add("");
        }
        
        for (Transaction trans : transactions) {
            values.add(trans.getValueList());
        }
        
        // add blanks
        for (int i=values.size(); i<100; i++) {
            values.add(blanks);
        }
        
        return values;
    }
}
