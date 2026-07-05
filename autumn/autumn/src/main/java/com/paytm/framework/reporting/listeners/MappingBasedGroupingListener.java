package com.paytm.framework.reporting.listeners;

import com.paytm.framework.datareader.DataReaderUtil;
import com.paytm.framework.reporting.listenerDecorators.DefaultListener;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class MappingBasedGroupingListener extends DefaultListener {

    String[][] masterConfig;
    final List<String> noGroupsCols = getNoGroupCols();
    //int j=0;

    protected MappingBasedGroupingListener(String[][] masterConfig ) {
        this.masterConfig =masterConfig;
    }

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        super.transform(annotation,testClass,testConstructor,testMethod);
        //if (annotation.getGroups().length == 0) {
        if(testMethod!=null) {
            annotation.setGroups(new String[]{});
            String className = testMethod.getDeclaringClass().getName();
            String methodName = testMethod.getName();
            HashMap<String, Integer> indexMap = new HashMap<>();

            int i = 0;
            for (String s : masterConfig[0]) {
                indexMap.put(s, i++);
            }

            List<String> applicableColIndex = new ArrayList<>();

            for (int row = 0; row < masterConfig.length; row++) {
                if (masterConfig[row][indexMap.get("Class_Name")].equals(className)
                        && masterConfig[row][indexMap.get("TC_Name")].equals(methodName)) {
                    String enableStatus = masterConfig[row][indexMap.get("Enable_Status")];
                    annotation.setEnabled(enableStatus.equals("1"));
                    if (enableStatus.equals("1")) {
                        for (int col = 0; col < masterConfig[row].length; col++) {
                            if (!masterConfig[row][col].equals("0") && !masterConfig[row][col].equals("") && !noGroupsCols.contains(masterConfig[0][col])) {
                                applicableColIndex.add(masterConfig[0][col]);
                                //System.out.println("j---> "+j++);
                            }
                        }
                        break;
                    }
                }
            }
            if (applicableColIndex.size() > 0) {
                annotation.setGroups(GetStringArray(applicableColIndex));
            }
        }
    }

    // Function to convert ArrayList<String> to String[]
    private String[] GetStringArray(List<String> list)
    {
        String str[] = new String[list.size()];
        Object[] objArr = list.toArray();
        int i = 0;
        for (Object obj : objArr) {
            str[i++] = (String)obj;
        }
        return str;
    }

    public abstract List<String> getNoGroupCols();

}
