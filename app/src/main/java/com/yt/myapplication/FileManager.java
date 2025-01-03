package com.yt.myapplication;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private final Context m_context;

    public String readInternalFile(String str) throws IOException {
        String content = "";
        InputStream inputStream = this.m_context.openFileInput(str);
        if (inputStream == null) {
            return content;
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        String separator = System.getProperty("line.separator");
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append(separator);
        }
        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();
        return stringBuilder.toString();
    }

    public static File getPublicPicturesDirectory(String str) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), str);
        if (!file.mkdirs()) {
            Log.e("SNAPGuidesError", "Directory not created");
        }
        return file;
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (!"mounted".equals(state)) {
            if (!"mounted_ro".equals(state)) {
                return false;
            }
        }
        return true;
    }

    public File getPrivatePicturesDirectory(String str) {
        File file = new File(this.m_context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), str);
        if (!file.mkdirs()) {
            Log.e("SNAPGuidesError", "Directory not created");
        }
        return file;
    }

    public void writeInternalFile(String str, byte[] bArr, boolean z) throws IOException {
        FileOutputStream outputStream = this.m_context.openFileOutput(str, z ? Context.MODE_APPEND : 0);
        outputStream.write(bArr);
        outputStream.close();
    }

    public FileManager(Context context) {
        this.m_context = context;
    }

    public static boolean isExternalStorageWritable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public void writeInternalFile(String fileName, String content, boolean append) throws IOException {
        FileOutputStream fos = this.m_context.openFileOutput(fileName, append ? Context.MODE_APPEND : Context.MODE_PRIVATE);
        fos.write(content.getBytes(Charset.forName("UTF-8")));
        fos.close();
    }

    public void appendToFile(String fileName, String data) throws IOException {
        FileOutputStream outputStream = this.m_context.openFileOutput(fileName, Context.MODE_APPEND);
        outputStream.write((data + "\n").getBytes());
        outputStream.close();
    }
    // פונקציה לקריאת קובץ
    public List<String> readFileLines(String fileName) throws IOException {
        List<String> lines = new ArrayList<>();
        FileInputStream inputStream = this.m_context.openFileInput(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();
        return lines;
    }
    public boolean deleteInternalFile(String str) {
        return this.m_context.deleteFile(str);
    }

    String[] getInternalFileList() {
        return this.m_context.fileList();
    }
}