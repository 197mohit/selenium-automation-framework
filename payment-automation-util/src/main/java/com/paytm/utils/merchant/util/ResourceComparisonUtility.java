package com.paytm.utils.merchant.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ResourceComparisonUtility {

    String sourceMainDir = "";
    String destMainDir = "";
    int counter = 1;
    StringBuffer comparisonOutput = new StringBuffer();
    String updateOutput = "";
    CSVReader csvReader = new CSVReader();
    private String profileName="";
    List<String[]> keysToIgnore = csvReader.getCSVValues("/etc/ignore/keys.csv");
    List<String[]> filesToIgnore = csvReader.getCSVValues("/etc/ignore/files.csv");
    List<String[]> domainMappings = csvReader.getCSVValues("/etc/domainMappings.csv");


    public StringBuffer getComparisonOutput() {
        return comparisonOutput;
    }

    public String getUpdateOutput() {
        return updateOutput;
    }

    public List<String> getModuleNameList(String sourceMainDir) {
        File[] sourceFileList = new File(sourceMainDir).listFiles();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < sourceFileList.length; i++) {
            list.add(sourceFileList[i].getName());
        }
        return list;

    }

    public void getDiff(File sourceDir, File destDir) throws IOException {
        if (sourceMainDir.equals(""))
            this.sourceMainDir = sourceDir.getAbsolutePath();
        if (destMainDir.equals(""))
            this.destMainDir = destDir.getAbsolutePath();
        File[] sourceFileList = sourceDir.listFiles();
        File[] destFileList = destDir.listFiles();
        Arrays.sort(sourceFileList);
        Arrays.sort(destFileList);
        HashMap<String, File> map = new HashMap<String, File>();
        for (int i = 0; i < destFileList.length; i++) {
            map.put(destFileList[i].getName(), destFileList[i]);
        }
        compareFiles(sourceFileList, map);
    }

    public void updateProperties(File sourceDir, String profile) {
        if (sourceMainDir.equals(""))
            this.sourceMainDir = sourceDir.getAbsolutePath();
        File[] sourceFileList = sourceDir.listFiles();
        Arrays.sort(sourceFileList);
        updateFiles(sourceFileList, profile);
    }

    public void updateFiles(File[] fileArr, String profile) {
        for (int i = 0; i < fileArr.length; i++) {
            if (!filesToIgnore.contains(getRelativePath(true, fileArr[i].getPath()))) {
                if (fileArr[i].isDirectory()) {
                    updateProperties(fileArr[i], profile);
                } else {
                    if (getFileExtension(fileArr[i]).equals("properties")) {
                        updateDomains(fileArr[i], profile);
                    }
                }
            }
        }
    }

    public void compareFiles(File[] fileArr, HashMap<String, File> map) throws IOException {
        for (int i = 0; i < fileArr.length; i++) {
            String fName = fileArr[i].getName();
            File fComp = map.get(fName);
            map.remove(fName);
            if (fComp != null) {
                if (fComp.isDirectory()) {
                    getDiff(fileArr[i], fComp);
                } else {
                    if (getFileExtension(fComp).equals("properties")) {
                        compareProperties(fileArr[i], fComp);
                    }
                }
            } else {
                if (!filesToIgnore.contains(getRelativePath(true, fileArr[i].getPath())))
                    comparisonOutput.append("\n\n" + counter++ + ". " + getRelativePath(true, fileArr[i].getParent()) + " not present in destination profile.");
            }
        }
    }

    public void updateDomains(File sourcePropertyFile, String profile) {
        this.profileName=profile;
        Properties sourceProp = new Properties();
        String key = "";
        try {
            sourceProp.load(new FileInputStream(sourcePropertyFile));
            Set<Object> sourceKeys = sourceProp.keySet();
            for (Object k : sourceKeys) {
                key = (String) k;
                String sourceValue = sourceProp.getProperty(key);
                if (isURL(sourceValue)) {
                    if (!isKeyInIgnoreList(key, getRelativePath(true, sourcePropertyFile.getPath()))) {
                        URL sourceValueUrl = new URL(sourceValue);
                        int port = sourceValueUrl.getPort();
                        String host = "";
                        if (port == -1) {
                            host = sourceValueUrl.getProtocol() + "://" + sourceValueUrl.getHost();
                        } else {
                            host = sourceValueUrl.getProtocol() + "://" + sourceValueUrl.getHost() + ":" + sourceValueUrl.getPort();
                        }

                        String newValue = sourceValue.replace(host, getMappedDomain(host, profile));
                        //sed -i -e 's/125.63.68.113/accounts-staging.paytm.in/' pom.xml

                        executeCommand("sed -i 's#"+sourceValue+"#"+newValue+"#g' "+sourcePropertyFile.getPath());
                        /*sourceProp.setProperty(key, newValue);
                        FileOutputStream output = new FileOutputStream(sourcePropertyFile.getPath());
                        sourceProp.store(output, newValue);
                        output.close();*/
                    }

                }

                if (isKeyInIgnoreList(key, getRelativePath(true, sourcePropertyFile.getPath()))) {
                    executeCommand("sed -i 's# ="+"#="+"#g' "+sourcePropertyFile.getPath());
                    executeCommand("sed -i 's#= "+"#="+"#g' "+sourcePropertyFile.getPath());
                    executeCommand("sed -i 's#"+key+"="+sourceValue+"#"+key+"="+getKeyFromIgnoreList(key, getRelativePath(true, sourcePropertyFile.getPath()))+"#g' "+sourcePropertyFile.getPath());
                }

            }
        } catch (Exception e) {
            updateOutput += e.getMessage() + " but found in " + getRelativePath(true, sourcePropertyFile.getPath()) + " file for key: " + key + "\n";
        }
    }


    public void compareProperties(File sourcePropertyFile, File destPropertyFile) {
        Properties sourceProp = new Properties();
        Properties destProp = new Properties();
        try {
            sourceProp.load(new FileInputStream(sourcePropertyFile));
            destProp.load(new FileInputStream(destPropertyFile));
            Set<Object> sourceKeys = sourceProp.keySet();
            Set<Object> destKeys = destProp.keySet();
            for (Object k : sourceKeys) {
                String key = (String) k;
                if (destKeys.contains(key)) {
                    String sourceValue = sourceProp.getProperty(key);
                    String destValue = destProp.getProperty(key);
                    if (!compareValue(sourceValue, destValue)) {
                        if (!isKeyInIgnoreList(key, getRelativePath(true, sourcePropertyFile.getPath())))
                            // System.out.println("");
                            //comparisonOutput.append("\n\n"+counter+++". "+"Value of key " + key + " is: " + sourceValue + "\t\t" + "but is: " + destValue + " in " + getRelativePath(false, destPropertyFile.getAbsolutePath()));
                            comparisonOutput.append("\n\n" + counter++ + ". Key: " + key + "\nPropertyFile: " + getRelativePath(false, destPropertyFile.getAbsolutePath()) + "\nExpected Value: " + sourceValue + "\nActual Value: " + destValue);
                        //comparisonOutput.append("\n" + key +","+destPropertyFile.getAbsolutePath()+ "," + getRelativePath(false, destPropertyFile.getAbsolutePath()) + ", " + new URL(sourceValue).getHost() + "," + new URL(destValue).getHost()+","+sourceValue+","+destValue);

                    }
                } else {
                    if (!isFileInIgnoreList(getRelativePath(true, sourcePropertyFile.getPath())))
                        //System.out.println("");
                        comparisonOutput.append("\n\n" + counter++ + ". " + key + " not present in " + getRelativePath(false, destPropertyFile.getAbsolutePath()));
                }
            }
        } catch (IOException e) {
            System.out.println("Exception Occurred: " + e);
        }
    }

   /* public static void main(String[] args) throws IOException {
        File sourceDir = new File("/home/nikunjkumar/repos/resources/pgp_sandbox");
        File destDir = new File("/home/nikunjkumar/repos/qa_resources/pgp_hotfix");
        new ResourceComparisonUtility().getDiff(sourceDir, destDir);
    }*/

    private String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    private Boolean compareValue(String sourceValue, String destValue) {
        try {
            String url1 = new URL(sourceValue).getPath();
            String url2 = new URL(destValue).getPath();
            return url1.equals(url2);
        } catch (MalformedURLException ex) {
            return sourceValue.equals(destValue);
        }
    }

    private Boolean isURL(String sourceValue) {
        try {
            String url1 = new URL(sourceValue).getPath();
            return true;
        } catch (MalformedURLException ex) {
            return false;
        }
    }

    private String getRelativePath(Boolean isSource, String absolutePath) {
        if (isSource) {
            return absolutePath.replace(this.sourceMainDir, "");
        } else {
            return absolutePath.replace(this.destMainDir, "");
        }
    }

    private Boolean isKeyInIgnoreList(String keyName, String filePath) {
        Boolean flag = false;
        for (String[] arr : keysToIgnore) {
            if (arr[0].equals(keyName) && arr[1].equals(filePath)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private String getKeyFromIgnoreList(String keyName, String filePath) {
        int i;
        for(i=0; i<keysToIgnore.get(0).length; i++){
            if(keysToIgnore.get(0)[i].equals(this.profileName)){

                break;
            }
        }
        for (String[] arr : keysToIgnore) {
            if (arr[0].equals(keyName) && arr[1].equals(filePath)) {
                return arr[i];
            }
        }
        return "Not Found";
    }

    private Boolean isFileInIgnoreList(String filePath) {
        Boolean flag = false;
        for (String[] arr : filesToIgnore) {
            if (arr[0].equals(filePath)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private String getMappedDomain(String domainName, String profile) throws Exception {
        int profileIndex = 0;
        //Calculate the profile index.
        for (int i = 0; i < domainMappings.get(0).length; i++) {
            if (domainMappings.get(0)[i].equals(profile)) {
                profileIndex = i;
                break;
            }
        }

        for (int i = 0; i < domainMappings.size(); i++) {
            if (domainMappings.get(i)[0].equals(domainName)) {
                return domainMappings.get(i)[profileIndex];
            }
        }

        throw new Exception("Domain Name: " + domainName + " not mapped in csv.");
    }

    private String executeCommand(String command) {
        StringBuffer output = new StringBuffer();
        Process p;
        try {
            p = Runtime.getRuntime().exec(new String[] { "bash", "-c", command });
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

            p.waitFor();
            p.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}

class CSVReader {

    public List<String[]> getCSVValues(String csvFilePath) {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        List<String[]> outputList = new ArrayList<String[]>();
        try {
            br = new BufferedReader(new FileReader(csvFilePath));
            while ((line = br.readLine()) != null) {
                outputList.add(line.split(cvsSplitBy));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return outputList;
    }

}

