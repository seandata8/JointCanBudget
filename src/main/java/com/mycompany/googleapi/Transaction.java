/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.googleapi;

import static com.mycompany.googleapi.JointCanBudget.categories;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.apache.commons.csv.CSVRecord;

/*
 *      JointBudget22 Transaction file
 */
public class Transaction {
    
    String desc;             // description
    Float amount;            // amount of transaction
    LocalDate date;          // date of transaction
    String budgetDateString; // date to be added to the budget
    String dateString;       // date string of the transaction
    Category category = null;
    ComboBox catCB;
    boolean isInDB = false;
    boolean catChanged = false;
    boolean budgetDateChanged = false;

    public Transaction(CSVRecord record) {
        // format the date -- had to change for TD format
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        
        // Chase format
        // columns: Details	Posting Date	Description	Amount	Type	Balance	Check or Slip #
        /*
        if (record.size() >= 4) {
            //System.out.println(line);
            date = LocalDate.parse(record.get(1), formatter);
            DateTimeFormatter sqlFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dateString = date.format(sqlFormat);
            amount = Float.valueOf(record.get(3).replaceAll("\\$", ""));
            // remove all single quotes because these are using in the sql
            desc = record.get(2).replaceAll("'", "");
        }        
        */
        // TD format for Debit Purchases
        // columns: 0) Date, 1) Description, 2) Amount Taken, 3) Amount Added, 4) Balance 
        // debit and credit reports use different date formats!
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .appendOptional(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            .toFormatter();
        
        if (record.size() >= 4) {
            //System.out.println(line);
            date = LocalDate.parse(record.get(0), formatter);
            DateTimeFormatter sqlFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dateString = date.format(sqlFormat);
            // default budget date is the transaction date
            budgetDateString = dateString;
            
            //date = LocalDate.parse(record.get(0), formatter);
            //DateTimeFormatter sqlFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            //dateString = date.format(sqlFormat);
            // if amount added is empty, read the amount taken
            if (record.get(3).isEmpty()) {
                amount = -Float.valueOf(record.get(2));
            // if amount taken is empty, read the amount added
            } else if (record.get(2).isEmpty()) {
                amount = Float.valueOf(record.get(3));
            } else {
                amount = 0.0f;
            }
            // remove all single quotes because these are using in the sql
            desc = record.get(1).replaceAll("'", "");
        }          
        //System.out.println(this.toString());
    }
    
    /*
    Transaction(String line) {
        // read in line from csv and turn into transaction
        
        // Step 1: Replace commas between quotes with a placeholder
        String temp = line.replaceAll("(\"[^\"]*?),([^\"]*?\")", "$1__TEMP__$2");
        // Step 2: Remove the placeholder
        String line2 = temp.replaceAll("__TEMP__", "");
        if (line.equals(line2) == false) {
            System.out.println("before fix: " + line);
            System.out.println("after fix: " + line2);
            line = line2;
        }
            
        // split the line
        String[] splits = line.split(",");
        // remove all quotes
        String[] elems = new String[splits.length];
        for (int i=0; i<splits.length; i++) {
            elems[i] = splits[i].replaceAll("\"", "");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ENGLISH);
        
        // SDCCU format
        
        if (splits.length >= 5) {
            //System.out.println(line);
            date = LocalDate.parse(elems[0], formatter);
            amount = Float.parseFloat(elems[4].replaceAll("\\$", ""));
            desc = elems[1];
        }
        
        
        // Chase format
        // columns: Details	Posting Date	Description	Amount	Type	Balance	Check or Slip #
        if (splits.length >= 4) {
            //System.out.println(line);
            date = LocalDate.parse(elems[1], formatter);
            DateTimeFormatter sqlFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dateString = date.format(sqlFormat);
            amount = Float.valueOf(elems[3].replaceAll("\\$", ""));
            // remove all single quotes because these are using in the sql
            desc = elems[2].replaceAll("'", "");
        }        
    }
    */

    Transaction(String ds, Float amt, String dsc, String catname, String bds) {
        
        // create transaction from database
        isInDB = true;
        dateString = ds;
        budgetDateString = bds;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        // the internal date of the Transaction object is the budget date (not the transaction date)
        date = LocalDate.parse(bds, formatter);
        amount = amt;
        desc = dsc;
        for (Category c : categories) {
            if (c.getName().equals(catname)) {
                category = c;
            }
        }
    }
    
    public void setIsInDB(boolean indb) {
        isInDB = indb;
    }
    
    public boolean isIn(ArrayList<Transaction> tlist) {
        
        // check if transaction is in the list, meaning it has the same"
        // 1) description, 2) amount, 3) date
        for (Transaction t : tlist) {
            if (t.getDesc().equals(desc) &&
                    t.getAmount() == amount &&
                    t.getDateString().equals(dateString)) {
                return true;
            }
        }
        return false;        
    }
    
    public boolean wasCatChanged() {
        return catChanged;
    }
    
    public void resetCatChanged() {
        catChanged = false;
    }
    
    public boolean wasBudgetDateChanged() {
        return budgetDateChanged;
    }

    public void resetBudgetDateChanged() {
        budgetDateChanged = false;
    }

    public String getBudgetDateString() {
        return budgetDateString;
    }

    public String getDesc() {
        return desc;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public String getDateString() {
        return dateString;
    }
    
    public String getYearMM() {
        // e.g. 2024-08
        return dateString.substring(0, 7);
    }
    
    public float getAmount() {
        return amount;
    }
    
    public String getCategoryName() {
        if (catCB.getValue() != null) {
            return catCB.getValue().toString();
        } else {
            return "";
        }
    }
    
    public Category guessCategory() {
        
        for (Category c : categories) {
            for (String key : c.getKeywords()) {
                if (desc.toUpperCase().contains(key.toUpperCase())) {
                    // set the guess
                    category = c;
                    return c;
                }
            }
        }
        return null;
    }
    
    public HBox getHBox(ArrayList<String> choices) {
        HBox hbox = new HBox();
        hbox.setSpacing(3);
        
        String sp = "";
        DatePicker dateDP = new DatePicker(date);
        dateDP.setMaxWidth(120);
        // change background to pink if the value is changed
        dateDP.valueProperty().addListener(new ChangeListener<LocalDate>() {
            @Override
            public void changed(ObservableValue<? extends LocalDate> ov, LocalDate t, LocalDate t1) {
                DateTimeFormatter sqlFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                budgetDateString = t1.format(sqlFormat);
                budgetDateChanged = true;
                dateDP.setStyle("-fx-control-inner-background: mistyrose;");
            }
        });
//        dateDP.valueProperty().addListener(new ChangeListener<LocalDate>() {
//            @Override
//            public void changed(ObservableValue<? extends LocalDate> ov, LocalDate t, LocalDate t1) {
//                dateDP.setStyle("-fx-control-inner-background: mistyrose;");
//            }
//        });
        
        //Button dateB = new Button(String.format("%s", date));
        Button amountB = new Button(String.format("$%8.2f", amount));
        amountB.setMinWidth(100);
        catCB = new ComboBox(FXCollections.observableArrayList(choices.toArray()));
        if (category != null) {
            catCB.getSelectionModel().select(category.getName());
        }
        // update category when catCB is changed
        // Listener
        catCB.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String t, String t1) {
                for (Category c : categories) {
                    if (c.getName().equals(t1)) {
                        category = c;
                    }
                }
                System.out.println("Previous Value: "+t);
                System.out.println("Current Value: "+t1);
                // set category changed flag
                catChanged = true;
                // new Background(new BackgroundFill(finalColor, CornerRadii.EMPTY, Insets.EMPTY)));
                catCB.setBackground(new Background(new BackgroundFill(Color.MISTYROSE,
                            CornerRadii.EMPTY, Insets.EMPTY)));
            }
        });
        
        Button descB = new Button(String.format("%s", desc));
        Label descL = new Label(String.format("%s", desc));
        //descL.setStyle("-fx-border-color: red;");
        // color based on if the transaction is in the DB
        if (isInDB) {
            descL.setTextFill(Color.BLUE);
        } else {
            descL.setTextFill(Color.BLACK);
        }
        
        hbox.setAlignment(Pos.CENTER_LEFT);
        
        hbox.getChildren().addAll(dateDP, new Text(sp), amountB, new Text(sp), 
                catCB, new Text(sp), descL);
        
        //ret.getChildren().add(new Text(String.format("%s", date)));
        //ret.getChildren().add(new Text(String.format("%8.2f", amount)));
        //ret.getChildren().add(new Text(String.format("%s", desc)));
        
        return hbox;
    }
    
    public List<Object> getValueList() {
        
        List<Object> ret = new ArrayList<>();
        ret.add(budgetDateString);
        ret.add(desc);
        ret.add(amount);
        if (category != null) {
            ret.add(category.getName());
        } else {
            ret.add("");
        }
        
        return ret;
    }
    
    @Override
    public String toString() {
        String ret = String.format("%s: %8.2f  %s", date, amount, desc);
        return ret;
    }
}
