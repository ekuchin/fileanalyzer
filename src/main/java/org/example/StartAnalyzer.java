package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class StartAnalyzer
{
    public static void main( String[] args ) throws IOException {

        String propsFilePath = args[0];

        Properties properties = new Properties();
        properties.load(new FileInputStream(propsFilePath));

        Analyzer analyzer = new Analyzer(properties);
        analyzer.generateReport();
    }
}
