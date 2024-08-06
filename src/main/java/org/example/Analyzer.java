package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Analyzer {

    private int REPORT_STEP;
    private int MAX_PATH_LENGTH;
    private int MAX_FILENAME_LENGTH;
    private int MAX_FOLDER_NESTING_LEVEL;
    private String ALLOW_SYMBOLS;
    private int OFFSET;

    private String REPORT_PATH;

    private File rootFolder;

    public Analyzer(Properties props) {
        REPORT_STEP=Integer.parseInt(props.getProperty("report.step"));
        MAX_PATH_LENGTH=Integer.parseInt(props.getProperty("max.path.length"));
        MAX_FILENAME_LENGTH=Integer.parseInt(props.getProperty("max.filename.length"));
        MAX_FOLDER_NESTING_LEVEL=Integer.parseInt(props.getProperty("max.path.nested"));
        ALLOW_SYMBOLS = props.getProperty("allow.symbols");
        REPORT_PATH = props.getProperty("path.report");

        String rootPath = props.getProperty("path.folder");
        rootFolder = new File(rootPath.substring(0, rootPath.lastIndexOf(File.separator)));
        System.out.println("Root Folder: " + rootFolder.getAbsolutePath());
    }

    public void generateReport(){
        writeLog(analyze(rootFolder, REPORT_STEP),REPORT_PATH);
    }

    private String analyze(File fileIn, int offset){
        System.out.println(fileIn.getAbsolutePath());
        StringBuilder result=new StringBuilder(generateLogEntry(fileIn, offset));

        if (fileIn.isDirectory()){
            File[] folderEntries = fileIn.listFiles();
            for (File entry : folderEntries){
                result.append(analyze(entry, offset+REPORT_STEP));
            }
        }
        return result.toString();
    }

    private String generateLogEntry(File file, int offset){

        StringBuilder result= new StringBuilder();
        if(file.isDirectory()){
            result.append("<h3>");
        }
        else{
            result.append("<p>");
        }

        //First number
        String cssclass_dir="corr";
        String filepath_wo_root = file.getAbsolutePath().substring((int) rootFolder.getPath().length());

        if (!filepath_wo_root.matches(ALLOW_SYMBOLS)){
            cssclass_dir="warning";
        }
        if (filepath_wo_root.length()>MAX_PATH_LENGTH){
            cssclass_dir="wrong";
        }

        String margin = Integer.toString(offset);
        result.append(String.format("<span style=\"margin-left:%spx;\" class=\"%s\">",margin,cssclass_dir));
        result.append(String.format("[%s] ",filepath_wo_root.length()));
        result.append("</span>");

        //Second number
        if(!file.isDirectory()) {
            String cssclass_file = "corr";

            if (!file.getName().contains(".")){
                cssclass_file = "wrong";
            }

            String filename_wo_ext=(file.getName().contains("."))
                    ? file.getName().substring(0,file.getName().lastIndexOf("."))
                    : file.getName();

            if (filename_wo_ext.length() > MAX_FILENAME_LENGTH || filename_wo_ext.length() == 8) {
                cssclass_file = "wrong";
            }

            result.append(String.format("<span class=\"%s\">[%s] </span>",cssclass_file,filename_wo_ext.length()));
        }
        else{
            int level = offset/REPORT_STEP;
            String css_nesting = level > MAX_FOLDER_NESTING_LEVEL ? "wrong" :"corr";
            result.append(String.format("<span class=\"%s\">(%s) </span>",css_nesting,level));
        }
        result.append(filepath_wo_root);

        if(file.isDirectory()){
            result.append("</h3>");
        }
        else{
            result.append("</p>");
        }
        return result.append("\n").toString();
    }

    public void writeLog(String report, String filename){
        try(FileWriter writer = new FileWriter(filename, false))
        {
            String pre="<html>\n<head>\n<style>\n";
            pre+=".corr {color:green;}"+"\n.wrong {color:red;}"+"\n.warning {color:orange;}"+"\n";
            pre+="</style>\n</head>"+"\n<body>"+"\n";

            String post="</body>"+"\n</html>"+"\n";

            writer.write(pre);
            writer.write(report);
            writer.write(post);
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
}