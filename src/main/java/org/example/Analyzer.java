package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Analyzer {

    private int REPORT_STEP;
    private int MAX_PATH_LENGTH;
    private int MAX_FILENAME_LENGTH;
    private int MAX_FOLDER_NESTING_LEVEL;
    private String ALLOW_SYMBOLS;
    private int OFFSET;
    private String REPORT_PATH;
    private String ROOT_PATH;
    private List<String> ALLOW_EXTENSIONS;

    private File rootFolder;

    public Analyzer(Properties props) {
        REPORT_STEP = Integer.parseInt(props.getProperty("report.step"));
        MAX_PATH_LENGTH = Integer.parseInt(props.getProperty("max.path.length"));
        MAX_FILENAME_LENGTH = Integer.parseInt(props.getProperty("max.filename.length"));
        MAX_FOLDER_NESTING_LEVEL = Integer.parseInt(props.getProperty("max.path.nested"));
        ALLOW_SYMBOLS = props.getProperty("allow.symbols");
        REPORT_PATH = props.getProperty("path.report");
        ROOT_PATH = props.getProperty("path.folder");
        ALLOW_EXTENSIONS = Arrays.asList(props.getProperty("allow.extensions").toLowerCase().split(","));

        rootFolder = new File(ROOT_PATH);
        System.out.println("Root Folder: " + rootFolder.getAbsolutePath());
    }

    public void generateReport() {
        writeLog(analyze(rootFolder, REPORT_STEP), REPORT_PATH);
    }

    public void writeLog(String report, String filename) {
        try (FileWriter writer = new FileWriter(filename, false)) {
            String pre = "<html>\n<head>\n<style>\n";
            pre += ".corr {color:green;}" + "\n.wrong {color:red;}" + "\n.warning {color:orange;}" + "\n";
            pre += "</style>\n</head>" + "\n<body>" + "\n";

            String post = "</body>" + "\n</html>" + "\n";

            writer.write(pre);
            writer.write(report);
            writer.write(post);
            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String analyze(File fileIn, int offset) {
        System.out.println(fileIn.getAbsolutePath());
        StringBuilder result = new StringBuilder(generateLogEntry(fileIn, offset));

        if (fileIn.isDirectory()) {
            File[] folderEntries = fileIn.listFiles();
            for (File entry : folderEntries) {
                result.append(analyze(entry, offset + REPORT_STEP));
            }
        }
        return result.toString();
    }

    private String generateLogEntry(File file, int offset) {

        StringBuilder result = new StringBuilder();
        result.append(file.isDirectory() ? "<h3>" : "<p>");

        String filepath_wo_root = file.getAbsolutePath().length() > ROOT_PATH.length()
                ? file.getAbsolutePath().substring((int) ROOT_PATH.length())
                : ROOT_PATH;
        generateFirstMark(result, filepath_wo_root, offset);

        if (file.isDirectory()) generateDirectorySecondMark(result, offset);
        else generateFileSecondMark(result, file.getName());

        result.append(filepath_wo_root);
        result.append(file.isDirectory() ? "</h3>" : "</p>");
        return result.append("\n").toString();
    }

    private void generateFirstMark(StringBuilder result, String filepathWoRoot, int offset) {

        // Assert for illegal symbols and common length, written in []

        String cssclass_dir = "corr";
        if (!filepathWoRoot.matches(ALLOW_SYMBOLS)) cssclass_dir = "warning";
        if (filepathWoRoot.length() > MAX_PATH_LENGTH) cssclass_dir = "wrong";

        String margin = Integer.toString(offset);
        result.append(String.format("<span style=\"margin-left:%spx;\" class=\"%s\">", margin, cssclass_dir));
        result.append(String.format("[%s] ", filepathWoRoot.length()));
        result.append("</span>");
    }

    private void generateFileSecondMark(StringBuilder result, String filename) {

        // Assert for illegal extensions and filename length

        String cssclass_file = "corr";

        String filename_wo_ext = (filename.contains("."))
                ? filename.substring(0, filename.lastIndexOf("."))
                : filename;

        String extension = (filename.contains("."))
                ? filename.substring(filename.lastIndexOf(".") + 1)
                : filename;

        if (!ALLOW_EXTENSIONS.contains(extension)) cssclass_file = "wrong";
        if (filename.length() > MAX_FILENAME_LENGTH) cssclass_file = "wrong";

        result.append(String.format("<span class=\"%s\">[%s] </span>", cssclass_file, filename_wo_ext.length()));
    }

    private void generateDirectorySecondMark(StringBuilder result, int offset) {

        // Assert for illegal symbols and nesting depth

        int level = offset / REPORT_STEP;
        String css_nesting = level > MAX_FOLDER_NESTING_LEVEL ? "wrong" : "corr";
        result.append(String.format("<span class=\"%s\">(%s) </span>", css_nesting, level));

    }

}