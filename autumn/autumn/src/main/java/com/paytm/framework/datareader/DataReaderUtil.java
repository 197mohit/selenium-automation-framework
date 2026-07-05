package com.paytm.framework.datareader;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.constructor.SafeConstructor;
import com.opencsv.CSVReader;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.lang.RandomStringUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.DataProvider;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataReaderUtil {

    private static synchronized Map<String, String> resolveDataProviderParams(Method testMethod) {
        if (testMethod == null)
            throw new IllegalArgumentException("Test Method context cannot be null.");

        DataProviderParams args = testMethod.getAnnotation(DataProviderParams.class);
        if (args == null)
            throw new IllegalArgumentException("Test Method context has no CPDataProviderParams annotation.");
        if (args.value() == null || args.value().length == 0)
            throw new IllegalArgumentException("Test Method context has a malformed CPDataProviderParams annotation.");
        Map<String, String> arguments = new HashMap<String, String>();
        for (int i = 0; i < args.value().length; i++) {
            String[] parts = args.value()[i].split("=");
            arguments.put(parts[0].trim(), parts[1].trim());
        }
        return arguments;
    }

    public static synchronized String[][] readExcel(String fileName, String sheetName, String tableName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        Workbook workbook = null;
        Sheet sheet;
        String[][] tabArray = null;

        try {
            workbook = Workbook.getWorkbook(file);
        } catch (IOException | BiffException e) {
            throw new RuntimeException(e);
        }

        sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            throw new RuntimeException("Worksheet not found: [Workbook | Sheet] => [" + fileName + " | " +
                    sheetName + "]");
        }

        int startRow, startCol, endRow, endCol, ci, cj;

        Cell tableStart = sheet.findCell(tableName);
        if (tableStart == null) {
            throw new RuntimeException("Data Table not found: [Workbook | Sheet | Table] => [" + fileName + " | " +
                    sheetName + " | " + tableName + "]");
        }

        startRow = tableStart.getRow();
        startCol = tableStart.getColumn();

        Cell tableEnd = sheet.findCell(tableName, startCol + 1, startRow + 1, 100, 64000, false);
        if (tableEnd == null) {
            throw new RuntimeException("Data Table end marker not found: [Workbook | Sheet | Table] => [" + fileName +
                    " | " + sheetName + " | " + tableName + "]");
        }

        endRow = tableEnd.getRow();
        endCol = tableEnd.getColumn();

        if ((endRow - startRow) < 2 || (endCol - startCol) < 2) {
            return null;
        }

        tabArray = new String[endRow - startRow - 1][endCol - startCol - 1];
        ci = 0;

        for (int i = startRow + 1; i < endRow; i++, ci++) {
            cj = 0;
            for (int j = startCol + 1; j < endCol; j++, cj++) {
                // Below code checks if the Excel has keyword 'RANDOM' & generates a random alphanumeric string
                // & prefixes it with 'AUTO' else it will just take the input as specified.
                if (sheet.getCell(j, i).getContents().equalsIgnoreCase("RANDOM")) {
                    String randomAlphanumeric = RandomStringUtils.randomAlphanumeric(6);
                    tabArray[ci][cj] = "AUTO" + randomAlphanumeric;
                } else {
                    tabArray[ci][cj] = sheet.getCell(j, i).getContents();
                }
            }
        }
        return (tabArray);
    }

    @DataProvider(name = "ExcelDataProvider")
    public static synchronized Object[][] excelDataProvider(Method testMethod) {
        Map<String, String> arguments = resolveDataProviderParams(testMethod);
        Object[][] retObjArr = readExcel(arguments.get("fileName"), arguments
                .get("sheetName"), arguments.get("tableName"));

        if (retObjArr == null) {
            throw new RuntimeException("Data Table is either malformed or empty [Workbook | Sheet | Table] => [" +
                    arguments.get("fileName") + " | " + arguments.get("sheetName") + " | " +
                    arguments.get("tableName") + "]");
        }
        return (retObjArr);
    }

    @DataProvider(name = "ExcelDataProviderTranspose")
    public static synchronized Object[][] excelDataProviderTranspose(Method testMethod) {
        Object[][] retObjArr = excelDataProvider(testMethod);
        Object[][] transposeObjArray = new Object[retObjArr[0].length][retObjArr.length];
        for (int row = 0; row < retObjArr[0].length; row++) {
            for (int col = 0; col < retObjArr.length; col++) {
                transposeObjArray[row][col] = retObjArr[col][row];
            }
        }
        return (transposeObjArray);
    }

    public static synchronized String[][] readCSV(String fileName, String tableName) {

        int startRow, startCol, endRow, endCol, i, j, ci, cj;
        String[][] tabArray;
        CSVReader csvSource;
        List<String[]> allLines;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try {
            csvSource = new CSVReader(new FileReader(file));
            allLines = csvSource.readAll();
            csvSource.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        search:
        {
            for (startRow = i = 0; i < allLines.size(); i++) {
                String arr[] = allLines.get(i);
                for (startCol = j = 0; j < arr.length; j++, startCol++) {
                    if (arr[j].equalsIgnoreCase(tableName)) {
                        break search;
                    }
                }
                startRow++;
            }
            throw new RuntimeException("Data Table not found: [File | Table] => [" + fileName + " | " + tableName + "]");
        }

        search:
        {
            for (endRow = i = startRow + 1; i < allLines.size(); i++) {
                String arr[] = allLines.get(i);
                for (endCol = j = startCol + 1; j < arr.length; j++, endCol++) {
                    if (arr[j].equalsIgnoreCase(tableName)) {
                        break search;
                    }
                }
                endRow++;
            }
            throw new RuntimeException("Data Table end marker not found: [Workbook | Table] => [" + fileName + " | " + tableName + "]");
        }


        if ((endRow - startRow) < 2 || (endCol - startCol) < 2) {
            return null;
        }

        tabArray = new String[endRow - startRow - 1][endCol - startCol - 1];
        ci = 0;

        for (i = startRow + 1; i < endRow; i++, ci++) {
            cj = 0;
            for (j = startCol + 1; j < endCol; j++, cj++) {
                tabArray[ci][cj] = allLines.get(i)[j];
            }
        }

        return (tabArray);
    }

    @DataProvider(name = "CsvDataProvider")
    public static synchronized Object[][] csvDataProvider(Method testMethod) {
        Map<String, String> arguments = resolveDataProviderParams(testMethod);
        Object[][] retObjArr = readCSV(arguments.get("fileName"), arguments.get("tableName"));

        if (retObjArr == null) {
            throw new RuntimeException("Data Table is either malformed or empty [File | Table] => [" +
                    arguments.get("fileName") + " | " + arguments.get("tableName") + "]");
        }

        return (retObjArr);
    }

    @DataProvider(name = "CsvDataProviderTranspose")
    public static synchronized Object[][] csvlDataProviderTranspose(Method testMethod) {
        Object[][] retObjArr = csvDataProvider(testMethod);
        Object[][] transposeObjArray = new Object[retObjArr[0].length][retObjArr.length];
        for (int row = 0; row < retObjArr[0].length; row++) {
            for (int col = 0; col < retObjArr.length; col++) {
                transposeObjArray[row][col] = retObjArr[col][row];
            }
        }
        return (transposeObjArray);
    }

    public static synchronized Object[][] readJSON(final String fileName, final String tableName) {
        JSONParser parser = new JSONParser();
        Object[][] testdataArray;
        Object[][] testArray;
        int rowLength, colLength = 0;
        int rowi = 0;

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try {
            Object jsonvalue = parser.parse(new FileReader(file));
            Map<String, Object> map = (Map<String, Object>) jsonvalue;
            ArrayList<Object> valueList = (ArrayList<Object>) map.get(tableName);
            if (valueList == null) {
                throw new RuntimeException("Data Table not found: [File | Table] => [" + fileName + " | " + tableName + "]");
            }

            Object[] valuearray = valueList.toArray();
            // Determining the maximum number of rows in Json document
            rowLength = valuearray.length;
            // Determining the maximum number of columns in Json document
            for (Object value : valuearray) {
                Map<String, Object> valuemap = (Map<String, Object>) value;
                int keylen = valuemap.keySet().size();
                if (colLength < keylen) {
                    colLength = keylen;
                }
            }

            testdataArray = new Object[rowLength][colLength];
            // Creating 2D array to be returned by function
            testArray = new Object[rowLength][colLength];
            // Storing values in an array
            for (Object value : valuearray) {
                Map<String, Object> valuemap = (Map<String, Object>) value;
                Object[] mapvaluearray = valuemap.values().toArray();
                int len = mapvaluearray.length;
                int columnj = 0;
                while (columnj < len) {
                    testdataArray[rowi][columnj] = mapvaluearray[columnj];
                    columnj++;
                }
                rowi++;
            }

            for (rowi = rowLength - 1; rowi >= 0; rowi--) {
                for (int columnj = colLength - 1; columnj >= 0; columnj--) {
                    testArray[rowi][colLength - 1 - columnj] = testdataArray[rowi][columnj];
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: [" + fileName + "]");
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        return testArray;
    }

    @DataProvider(name = "JsonDataProvider")
    public static synchronized Object[][] jsonDataProvider(Method testMethod) {
        Map<String, String> arguments = resolveDataProviderParams(testMethod);
        Object[][] retObjArr = readJSON(arguments.get("fileName"), arguments.get("tableName"));

        if (retObjArr == null) {
            throw new RuntimeException("Data Table is either malformed or empty [JSON | Table] => [" +
                    arguments.get("fileName") + " | " +
                    arguments.get("tableName") + "]");
        }
        return (retObjArr);
    }

    public static synchronized void updateMerchantYaml(String filePath) {

    }

    public static synchronized Map<String, Object> readYML(final String fileName) {
        Object ymlData = null;
        Map<String, Object> map = null;
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            File file = new File(classLoader.getResource(fileName).getFile());
            InputStream input = new FileInputStream(file);
            Yaml yaml = new Yaml(new SafeConstructor());
            ymlData = yaml.loadAll(input).iterator().next();
            if (ymlData instanceof Map) {
                map = (Map<String, Object>) ymlData;
            } else
                throw new RuntimeException("No Value found.");

        } catch (FileNotFoundException e) {
            throw new RuntimeException("YAML document not found: [" + fileName + "]");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public static synchronized Object[][] readYML(final String fileName, final String key) {
        int rowi = 0;
        int datacolumns = 0;
        Object[][] testArray = null;

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        try {
            File file = new File(classLoader.getResource(fileName).getFile());
            InputStream input = new FileInputStream(file);
            Yaml yaml = new Yaml(new SafeConstructor());
            Object yamlvalue = yaml.loadAll(input).iterator().next();

            if (yamlvalue instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) yamlvalue;
                ArrayList<Object> valuelist = (ArrayList<Object>) map.get(key);
                Object[] valuearray = valuelist.toArray();

                // Determining the maximum number of rows in YAML document
                int rowlen = valuearray.length;

                // Determining the maximum number of columns in YAML document
                for (Object value : valuearray) {
                    Map<String, Object> valuemap = (Map<String, Object>) value;
                    int keylen = valuemap.keySet().size();
                    if (datacolumns < keylen) {
                        datacolumns = keylen;
                    }
                }

                // Creating 2D array to be returned by function
                testArray = new Object[rowlen][datacolumns];

                // Storing values in an array
                for (Object value : valuearray) {
                    Map<String, Object> valuemap = (Map<String, Object>) value;
                    Object[] mapvaluearray = valuemap.values().toArray();
                    int len = mapvaluearray.length;
                    int columnj = 0;
                    while (columnj < len) {
                        testArray[rowi][columnj] = mapvaluearray[columnj];
                        columnj++;
                    }
                    rowi++;
                }
            } else
                throw new RuntimeException("No Value found.");

        } catch (FileNotFoundException e) {
            throw new RuntimeException("YAML document not found: [" + fileName + "]");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Returning 2D array
        return testArray;
    }

    @DataProvider(name = "YamlDataProvider")
    public static synchronized Object[][] yamlDataProvider(Method testMethod) {
        Map<String, String> arguments = resolveDataProviderParams(testMethod);
        Object[][] retObjArr = readYML(arguments.get("fileName"), arguments.get("tableName"));

        if (retObjArr == null) {
            throw new RuntimeException("Data Table is either malformed or empty [YAML | Table] => [" +
                    arguments.get("fileName") + " | " +
                    arguments.get("tableName") + "]");
        }

        return (retObjArr);
    }

}