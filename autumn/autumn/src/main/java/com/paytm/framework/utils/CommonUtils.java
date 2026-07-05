package com.paytm.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.YuiCssCompressor;
import com.paytm.framework.reporting.Reporter;

import java.io.*;
import java.math.RoundingMode;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The CommonUtils Class provides the common utility methods.
 */
public class CommonUtils {

    /**
     * This method is used to get the current date in the provided date format.
     *
     * @param format The format in which the date will be returned.
     * @return The current date.
     */
    public static String getdate(String format) {
        final DateFormat sdf = new SimpleDateFormat(format);
        Date date = new Date();
        return sdf.format(date);
    }

    /**
     * This method is used to get the current year in the string format.
     *
     * @return The current year.
     */
    public static int getCurrentYear() {
       return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * This method is used to get the current month in the string format.
     *
     * @return The current month.
     */
    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH);
    }

    /**
     * Rounding mode to round towards the "nearest neighbor" unless
     * both neighbors are equidistant, in which case, round towards
     * the even neighbor. Behaves as for RoundingMode.HALF_UP if the
     * digit to the left of the discarded fraction is odd; behaves as
     * for RoundingMode.HALF_DOWN if it's even.
     *
     * @param number The number that the user want to convert.
     * @return The rounded off number in String format.
     */
    public static String doubleUpConvertor(Double number) {
        try {
            DecimalFormat format = new DecimalFormat("###.##");
            format.setRoundingMode(RoundingMode.HALF_EVEN);
            return format.format(number);
        } catch (Exception e) {
            Reporter.report.error("Couldn't doubleUpConvertor "+e.getMessage());
            return null;
        }
    }

    /**
     * Rounding mode to round towards "nearest neighbor" unless
     * both neighbors are equidistant, in which case round up.
     * Behaves as for RoundingMode.UP if the discarded fraction
     * is ≥ 0.5; otherwise, behaves as for RoundingMode.DOWN.
     *
     * @param number The number that the user want to convert.
     * @return The rounded off number in String format.
     */
    public static String doubleHalfUpConvertor(Double number) {
        try {
            DecimalFormat format = new DecimalFormat("###.##");
            format.setRoundingMode(RoundingMode.HALF_UP);
            return format.format(number);
        } catch (Exception e) {
            Reporter.report.error("Couldn't doubleHalfUpConvertor "+e.getMessage());
            return null;
        }
    }

    /**
     * Rounding mode to round towards "nearest neighbor" unless
     * both neighbors are equidistant, in which case round up.
     * Behaves as for RoundingMode.UP if the discarded fraction
     * is ≥ 0.5; otherwise, behaves as for RoundingMode.DOWN.
     *
     * @param number The number that the user want to convert.
     * @return The rounded off number in String format.
     */
    public static String doubleHalfUpConvertor(String number) {
        try {
            DecimalFormat format = new DecimalFormat("###.##");
            format.setRoundingMode(RoundingMode.HALF_UP);
            return format.format(Double.parseDouble(number));
        } catch (Exception e) {
            Reporter.report.error("Couldn't doubleHalfUpConvertor "+e.getMessage());
            return null;
        }
    }

    public String convertDtoToString(Object object) {
        String dtoStringvalue = null;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            dtoStringvalue = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            Reporter.report.error("Couldn't convertDtoToString "+e.getMessage());
        }
        return dtoStringvalue;
    }


    public Map<String, Object> convertDtoToMap(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Map<String, Object> map = objectMapper.convertValue(object, Map.class);
        return map;
    }

    /**
     * Compresses html & any inline css to remove tabs, new line, carriage return, extra white spaces etc.
     *
     * @param htmlContent html to be compressed
     * @return Compressed html
     */
    public static String getCompressedHTML(String htmlContent) {
        htmlContent = htmlContent.replaceAll("\t", "");
        htmlContent = htmlContent.replaceAll("\r|\n", "");
        HtmlCompressor compressor = new HtmlCompressor();
        compressor.setRemoveIntertagSpaces(true);
        compressor.setRemoveSurroundingSpaces(HtmlCompressor.ALL_TAGS);
        YuiCssCompressor cssCompressor = new YuiCssCompressor();
        compressor.setCssCompressor(cssCompressor);
        return compressor.compress(htmlContent);
    }

    public static String getPathWithValidSeperator(String path) {

        final String UNIX_FILE_SEPARATOR = "/";
        final String WINDOWS_FILE_SEPARATOR = "\\\\";
        String changedPath = path;

        if (File.separator.equals(WINDOWS_FILE_SEPARATOR))
            changedPath = path.replaceAll(UNIX_FILE_SEPARATOR, Matcher.quoteReplacement(WINDOWS_FILE_SEPARATOR));
        if (File.separator.equals(UNIX_FILE_SEPARATOR))
            changedPath = path.replaceAll(WINDOWS_FILE_SEPARATOR, Matcher.quoteReplacement(UNIX_FILE_SEPARATOR));
        return changedPath;
    }

    public static void createDirectory(String directoryPath) {
        File file = new File(directoryPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void replaceKeysInFile(String filePath, String destinationFilePath, String... args) {
        try {
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String text = "", newText = "";
            while ((text = reader.readLine()) != null) {
                newText += MessageFormat.format(text, args) + System.getProperty("line.separator");
            }
            reader.close();

            FileWriter writer = new FileWriter(destinationFilePath);
            writer.write(newText);
            writer.close();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public static void zip(String dirPath) {
        Path sourceDir = Paths.get(dirPath);
        String zipFileName = dirPath.concat(".zip");
        try {
            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    try {
                        Path targetFile = sourceDir.relativize(file);
                        outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                        byte[] bytes = Files.readAllBytes(file);
                        outputStream.write(bytes, 0, bytes.length);
                        outputStream.closeEntry();
                    } catch (IOException e) {
                        Reporter.report.error("Couldn't zip "+e.getMessage());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            outputStream.close();
        } catch (IOException e) {
            Reporter.report.error("Couldn't zip "+e.getMessage());
        }
    }

    public static String formatString(String s, Object... valuesToBeReplacePlaceholders) {
        for (Object o : valuesToBeReplacePlaceholders) {
            s = s.replaceFirst("%s", o.toString());
        }
        return s;
    }

}
