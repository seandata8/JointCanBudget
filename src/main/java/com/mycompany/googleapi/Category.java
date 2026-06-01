/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.googleapi;

import java.util.ArrayList;

/**
 *
 * @author sean
 */
public class Category {
    
    String name;
    ArrayList<String> keywords = new ArrayList<>();
    String letter;
    
    Category(String nam, String kws, String lett) {
        name = nam;
        String[] words = kws.split(",");
        for (String w : words) {
            if (w.isEmpty() == false) keywords.add(w);
        }
        letter = lett;
    }
    
    public String getName() {
        return name;
    }
    
    public String getLetter() {
        return letter;
    }
    
    public ArrayList<String> getKeywords() {
        return keywords;
    }
    
    public String getChoice() {
        String[] names = name.split(" ");
        return names[0] + " (" + letter + ")";
    }
}
