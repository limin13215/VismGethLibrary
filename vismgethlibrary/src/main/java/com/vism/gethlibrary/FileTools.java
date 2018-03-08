package com.vism.gethlibrary;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2018-3-8.
 */

public class FileTools {
    public static void savePrivateFiles(Context context,String keystore,String filename){
        File file = new File(context.getFilesDir(),filename);
        try {
            OutputStream out = new FileOutputStream(file);
            out.write(keystore.getBytes());
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
