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

    private enum Label {OK, WARNING, ERROR}

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
        result.append(file.isDirectory() ? "<h3" : "<p");
        result.append(String.format(" style=\"margin-left:%spx\">", offset));

        String filename = file.getName();

        String filepathWithoutRoot = file.getAbsolutePath().length() > ROOT_PATH.length()
                ? file.getAbsolutePath().substring((int) ROOT_PATH.length())
                : ROOT_PATH;

        String extension = (filename.contains("."))
                ? filename.substring(filename.lastIndexOf(".") + 1)
                : filename;

        // Relative path length
        generateMark(
                result,
                String.valueOf(filepathWithoutRoot.length()),
                filepathWithoutRoot.length() > MAX_PATH_LENGTH ? Label.ERROR : Label.OK
        );
        // Filename length
        generateMark(
                result,
                String.valueOf(filename.length()),
                filename.length() > MAX_FILENAME_LENGTH ? Label.ERROR : Label.OK
        );
        // Directory depth
        if (!file.isDirectory()) {
            generateMark(
                    result,
                    String.valueOf(offset / REPORT_STEP - 1),
                    offset / REPORT_STEP > MAX_FOLDER_NESTING_LEVEL + 1 ? Label.ERROR : Label.OK
            );
        }
        // Illegal symbols
        generateMark(
                result,
                "abc",
                !filename.matches(ALLOW_SYMBOLS) ? Label.WARNING : Label.OK
        );
        // Illegal extensions
        if (!file.isDirectory()) {
            generateMark(
                    result,
                    "ext",
                    (!ALLOW_EXTENSIONS.contains(extension)) ? Label.WARNING : Label.OK
            );
        }

        result.append(filepathWithoutRoot);
        result.append(file.isDirectory() ? "</h3>" : "</p>");
        return result.append("\n").toString();
    }

    private void generateMark(StringBuilder result, String label, Label type) {
        String cssClass = "corr";
        switch (type) {
            case WARNING:
                cssClass = "warning";
                break;
            case ERROR:
                cssClass = "wrong";
                break;
        }

        result.append(String.format("<span class=\"%s\">", cssClass));
        result.append(String.format("[%s] ", label));
        result.append("</span>");
    }

}