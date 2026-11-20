package com.miniredis;
// import java.util.*;
import java.io.File;
// import java.io.IO;
import java.io.IOException;


/* 
cd "C:\Users\Akshra\OneDrive\Desktop\Mini Radish\src\main\java"
javac com/miniredis/PersistenceManager.java
java com.miniredis.PersistenceManager

*/
public class PersistenceManager {
    public static void main(String[] args) {
        File f=new File("data/dump.rdb");
        // For checking if it exist 
        f.getParentFile().mkdirs();
        if(f.exists()==true){
            System.out.println("It Exist");
        }else{
            try {
                if(f.createNewFile()){
                    System.out.println("File Created Successfully!!");
                }
            } catch (IOException e) {
                System.out.println("Unable to create File.");
                e.printStackTrace();
            }
        }
    }
}
