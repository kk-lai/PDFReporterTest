package com.my3devtools.pdfreportertest;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.oss.pdfreporter.engine.JRException;
import org.oss.pdfreporter.repo.RepositoryManager;
import org.oss.pdfreporter.repo.FileResourceLoader;
import org.oss.pdfreporter.engine.design.JasperDesign;
import org.oss.pdfreporter.registry.ApiRegistry;
import org.oss.pdfreporter.engine.JasperPrint;
import org.oss.pdfreporter.engine.JasperReport;
import org.oss.pdfreporter.engine.xml.JRXmlLoader;
import org.oss.pdfreporter.engine.JasperCompileManager;
import org.oss.pdfreporter.sql.IConnection;
import org.oss.pdfreporter.engine.JasperFillManager;
import org.oss.pdfreporter.engine.JasperExportManager;
import org.oss.pdfreporter.sql.SQLException;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        copyAsset("report");
        copyAsset("resources");
        String reportFolder = getExternalFilesDir(null) + "/report";
        String resourcesFolder = getExternalFilesDir(null) + "/resources";
        generateDemoReport(reportFolder+"/PDFReporterTest.jrxml", reportFolder+"/chinook.db",
                resourcesFolder,
                reportFolder+"/report.pdf");
    }

    private void copyAsset(String file) {
        String extDir = getExternalFilesDir(null) + "/";
        try {
            InputStream is = getAssets().open(file);
            FileOutputStream os = new FileOutputStream(extDir + file);
            copyFile(is, os);
            is.close();
            os.flush();
            os.close();
        } catch (IOException e) {
            try {
                String[] list = getAssets().list(file);
                File dir = new File(extDir + file);
                dir.mkdir();
                for (String listFile : list) {
                    copyAsset(file + "/" + listFile);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.e(TAG, "Failed to copy resoruces to SD card : " + (e != null ? e.getMessage() : null));
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024 * 16];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    protected void generateDemoReport(String jrxmlFile, String sqliteFile, String resourcesFolder,  String pdfPath)
    {
        String jrxmlFolder = jrxmlFile.substring(0, jrxmlFile.lastIndexOf('/'));

        try {
            ApiRegistry.initSession();
            RepositoryManager repositoryManager = RepositoryManager.getInstance();
            repositoryManager.reset();
            repositoryManager.setDefaulReportFolder(jrxmlFolder);
            repositoryManager.setDefaultResourceFolder(resourcesFolder);

            InputStream isReport = FileResourceLoader.getInputStream(jrxmlFile);
            JasperDesign design = JRXmlLoader.load(isReport);
            isReport.close();
            JasperReport jrReport = JasperCompileManager.compileReport(design);
            IConnection sqlDataSource = ApiRegistry.getSqlFactory().newConnection(sqliteFile, null, null);
            JasperPrint jprint = JasperFillManager.fillReport(jrReport, null, sqlDataSource);
            JasperExportManager.exportReportToPdfFile(jprint, pdfPath);
            ApiRegistry.dispose();

        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        } catch (JRException e) {
            Log.d(TAG, e.getMessage());
        } catch (SQLException e) {
            Log.d(TAG, e.getMessage());
        }

    }
}
