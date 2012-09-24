package org.tinymediamanager.scraper.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PropertiesUtils {
    public static void load(Properties props, File f) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(f);
            props.load(is);
        } finally {
            if (is!=null) {
                is.close();
            }
        }
    }
    
    public static void load(Properties props, InputStream is) throws IOException {
        try {
            props.load(is);
        } finally {
            is.close();
        }
    }
    
    public static void store(Properties props, File out, String msg) throws IOException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(out);
            props.store(os, msg);
        } finally {
            if (os!=null) {
                try {
                    os.flush();
                } catch (Exception e){
                }
                os.close();
            }
        }
    }
}

