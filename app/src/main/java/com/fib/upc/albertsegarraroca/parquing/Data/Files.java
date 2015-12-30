package com.fib.upc.albertsegarraroca.parquing.Data;

import android.content.Context;
import android.os.Environment;

import com.fib.upc.albertsegarraroca.parquing.Model.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by albert on 29/12/15.
 */
public class Files {
    private static Files ourInstance = new Files();

    public static Files getInstance() {
        return ourInstance;
    }

    private Files() {}

    private static final String GLOBAL_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Utils.APPLICATION_NAME;
    private static final String EMAIL_FILE_DIR = GLOBAL_DIR + "/" + "EmailAttachments";

    public void createFolder(String path) throws IOException {
        File folder = new File(path);
        if (!folder.exists()) {
            if (!folder.mkdirs()) throw new IOException();
        }
        else if (folder.isFile()) {
            // Fuck the police
            boolean del = folder.delete();
            if (!del) throw new IOException();
            boolean createDir = folder.mkdirs();
            if (!createDir) throw new IOException();
        }
    }

    public void init() throws IOException {
        createFolder(GLOBAL_DIR);
        createFolder(EMAIL_FILE_DIR);
    }

    public void saveFile(String folderPath, String filename, String content) throws IOException {
        File folder = new File(folderPath);

        if (!folder.isDirectory()) createFolder(folderPath);

        File dest = new File(folder, filename);

        Writer writer = new BufferedWriter(new FileWriter(dest));
        writer.write(content);
        writer.close();
    }

    public void saveDocument(String filename, String content) throws IOException {
        saveFile(GLOBAL_DIR, filename, content);
    }

    public void saveEmailAttachment(String filename, String content) throws IOException {
        saveFile(EMAIL_FILE_DIR, filename, content);
    }
}
