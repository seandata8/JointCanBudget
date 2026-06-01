/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.googleapi;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;

import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.*;
//import java.util.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author sean
 */
public class SheetsHelper {
    
    private static HttpTransport transport;
    private static JacksonFactory jsonFactory;
    private static FileDataStoreFactory dataStoreFactory;
    // added this definition
    private static Sheets service;
    
    // change this to resources json
    //private static final java.io.File DATA_STORE_DIR = 
    //            new java.io.File("src/main/resources/google-sheets-client-secret.json");
    //private static final java.io.File DATA_STORE_DIR = 
    //            new java.io.File(System.getProperty("user.home"), ".credentials/sheets.googleapis.com.json");  
    private static final File DATA_STORE_DIR = 
                new File("resources/cred2.json");
    
    private static List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

    public SheetsHelper() {
        try {
            
            System.out.printf("cred file: %s, %s, %s\n", 
                    DATA_STORE_DIR.canRead(), DATA_STORE_DIR.length(), DATA_STORE_DIR.getAbsolutePath());
            System.out.println("try transport");
            transport = GoogleNetHttpTransport.newTrustedTransport();
            System.out.println("try dataStoreFactory");
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
            System.out.println("try jsonFactory");
            jsonFactory = JacksonFactory.getDefaultInstance();

            // Added Sheets as object
            System.out.println("try service");
            service = getSheetsService();
            System.out.println("SUCCESS!!!!");
        } catch (Exception e) {
            // handle exception
            System.out.printf("failed in SheetsIntegration: %s\n", e.toString());
        }
        
        
    }

    public static Credential authorize() throws IOException {
        // Load client secrets.
        System.out.println("try cfile");
        File cfile = new File("/Users/sean/NetBeansProjects/JointBudgetMaven/resources/cred2.json");
        //cfile.createNewFile();
        System.out.println("try clientSecrets");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                new InputStreamReader(new FileInputStream(cfile)));

        // Build flow and trigger user authorization request.
        System.out.println("try flow");
        /*
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        transport, jsonFactory, clientSecrets, scopes)
                        .setDataStoreFactory(dataStoreFactory)
                        .setAccessType("offline")
                        .build();
        */
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        transport, jsonFactory, clientSecrets, scopes)
                        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens-si")))
                        .setAccessType("offline")
                        .build();        
        System.out.println("try credential (in authorize)");
        Credential credential = new AuthorizationCodeInstalledApp(flow, 
                new LocalServerReceiver()).authorize("ephremnash@gmail.com");
        return credential;
    }

    public static Sheets getSheetsService() throws IOException {
        System.out.println("try credential");
        Credential credential = authorize();
        return new Sheets.Builder(transport, jsonFactory, credential)
                .setApplicationName("JointBudgetFX")
                .build();
    }

    public void readRange(String sheetid) throws GoogleJsonResponseException, IOException {
        
        ValueRange result = null;
        // Gets the values of the cells in the specified range.
        result = service.spreadsheets().values().get(sheetid, "C2:F15").execute();
        int numRows = result.getValues() != null ? result.getValues().size() : 0;
        System.out.printf("%d rows retrieved.\n", numRows);

        // all types are Strings (for dates and numbers)
        if (numRows > 0) {
            for (Object o : result.getValues()) {
                System.out.println(o);
            }
        }
    }
    
    public void writeTransactions(String sheetid, List<List<Object>> values) throws IOException {
        // figure out range from values size
        Integer lastrow = values.size() + 1;
        String range = "C2:F" + lastrow.toString();
        
        // Updates the values in the specified range.
        UpdateValuesResponse result = null;
        ValueRange body = new ValueRange()
            .setValues(values);
        result = service.spreadsheets().values().update(sheetid, range, body)
            .setValueInputOption("USER_ENTERED")
            .execute();
        System.out.printf("%d cells updated.\n", result.getUpdatedCells());
      
    }
    
    public void listSheets(String sheetid) {
        
        System.out.printf("\nSheets in id: %s\n", sheetid);
        try {
            Spreadsheet sp = service.spreadsheets().get(sheetid).execute();
            List<Sheet> sheets = sp.getSheets();
            for (Sheet sheet : sheets) {
                System.out.println(sheet.getProperties().getTitle());
            }
            /*
                need new service here for Drive API, not Sheets API
            
            FileList result = service.files().list()     
                .setQ("'root' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")     
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name, parents)")
                .setPageToken(pageToken)     
                .execute();
            */
        } catch (Exception e) {
            
        }
        
    }
    
    public void writeSomething(List<String> myData) {

        try {
            String id = "INSERT_SHEET_ID";
            String writeRange = "INSERT_SHEET_NAME!A3:E";

            List<List<Object>> writeData = new ArrayList<>();
            for (String someData: myData) {
                List<Object> dataRow = new ArrayList<>();
                dataRow.add(someData);
                writeData.add(dataRow);
            }

            ValueRange vr = new ValueRange().setValues(writeData).setMajorDimension("ROWS");
            service.spreadsheets().values()
                    .update(id, writeRange, vr)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (Exception e) {
            // handle exception
        }
    }    
}
