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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.SheetsScopes;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
//import java.io.File;

/**
 *
 * @author sean
 */
public class DriveHelper {
  /**
   * Application name.
   */
  private static final String APPLICATION_NAME = "JointBudgetFX";
  /**
   * Global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  /**
   * Directory to store authorization tokens for this application.
   */
  private static final String TOKENS_DIRECTORY_PATH = "tokens-dq";

  /**
   * Global instance of the scopes required by this quick start.
   * If modifying these scopes, delete your previously saved tokens/ folder.
   */
  //private static final List<String> SCOPES =
  //    Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
  private static final List<String> SCOPES =
            Arrays.asList(SheetsScopes.SPREADSHEETS,SheetsScopes.DRIVE);

  //private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
  private static final String CREDENTIALS_FILE_PATH = "resources/cred2.json";
  
  public static Drive service;
  
  private static boolean successLoad = true;
  
  DriveHelper() {
        try {
          // Build a new authorized API client service.
          final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
          service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
              .setApplicationName(APPLICATION_NAME)
              .build();       
          // list files
          //ListFiles();
        } catch (Exception e) {
              // handle exception
              System.out.printf("failed in DriveQuickstart: %s\n", e.toString());          
        }

        // test getting SheetID -- if it doesn't work, delete tokens and try again
        /*
        try {
            this.getSheetID("test");
            System.out.println("test of getSheetID succeeded!");
            successLoad = true;
        } catch (IOException ex) {
            if (ex.getMessage().contains("400 Bad Request")) {
                System.out.println("Error in DriveHelper constructor: .getSheetID(): " +
                    ex.getMessage());                // delete tokens
                //File tokens = new File(resources);
                String filepath = System.getProperty("user.dir") + "/tokens-si/StoredCredential";
                java.io.File tok = new java.io.File(filepath);
                //boolean succ = tok.delete();
                //if (succ) {
                //    System.out.println("token-si successfully deleted");
                //}
                // set flag to let Main know there was an error and to try again
                successLoad = false;
                //System.out.println("root dir = " + root);
            } else {
                System.out.println("Error in DriveHelper constructor: .getSheetID(): " +
                    ex.getMessage());
            }
        }
        */
  }
  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
      throws IOException {
    // Load client secrets.
    InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH); 
    //InputStream in = DriveHelper.class.getResourceAsStream(in);
    if (in == null) {
      throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
    }
    GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline")
        .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    //returns an authorized Credential object.
    return credential;
  }

    public boolean wasSuccessLoad() {
        return successLoad;
    }
    
    public String getSheetID(String name) throws IOException {

        FileList result = service.files().list()
            .setQ("'1tmwpbpNX419zdYtwJwnXO_R1xz5c34lz' in parents and mimeType='application/vnd.google-apps.spreadsheet'")
            .setPageSize(100)
            .setFields("nextPageToken, files(id, name, parents)")
            .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("Error in DriveHelper getSheetID: No files found.");
        } else {
            for (File file : files) {
                if (file.getName().equals(name)) {
                    return file.getId();
                }
            }
        }   
        
        return null;
    }
    
    public void ListFiles() throws IOException, GeneralSecurityException {

    // Print the names and IDs for up to 10 files.
    // q="mimeType='application/vnd.google-apps.spreadsheet'"

        FileList result = service.files().list()
            .setQ("'1tmwpbpNX419zdYtwJwnXO_R1xz5c34lz' in parents and mimeType='application/vnd.google-apps.spreadsheet'")
            .setPageSize(100)
            .setFields("nextPageToken, files(id, name, parents)")
            .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }
    
}
