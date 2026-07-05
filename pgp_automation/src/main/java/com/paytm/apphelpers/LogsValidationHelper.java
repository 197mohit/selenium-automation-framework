package com.paytm.apphelpers;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.ServerDetails;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.ServerUtil;
import org.awaitility.Awaitility;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class LogsValidationHelper {

    public static String getLogsOnServer(ServerConfigProvider.SERVICE service, String grepCmd) throws InterruptedException {
        ServerDetails serverDetails = ServerConfigProvider.getServerDetail(service);
        String ip = serverDetails.getIp();
        String userName = serverDetails.getUser();
        String password = serverDetails.getPassword();
        String details = ip + ":" + userName + ":" + password;
        Boolean useEsForLogs = serverDetails.getUseEsForLogs();
        if (useEsForLogs) {
            return getLogsOnServer(details, grepCmd);
        }
        else{
            String env = LocalConfig.ENV_NAME.toLowerCase();
            String svcName = serverDetails.getName();
            String logs = "";
            try {
                //Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> (LogsValidationHelper.getLogsOnK8sServer(details, env, svcName, grepCmd).contains(PG2LogsValidationHelper.getOrderId(grepCmd))));
                //logs = getLogsOnK8sServer(details, env, svcName, grepCmd);
                logs = Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> LogsValidationHelper.getLogsOnK8sServer(details, env, svcName, grepCmd), s -> !"".equals(s));
            } catch (Exception e) {
                System.out.println("Logs not found for grep cmd : " + grepCmd);
                System.out.println(e);
            }
            return logs;
        }
    }


    public static String getLogsOnServer(String serverIP, String grepCmd) throws InterruptedException {
//        String resp ="";
//        try {
//            ServerUtil serverUtil = new ServerUtil();
//            Session session = null;
//            if(serverIP.contains(":")){
//                session = serverUtil.getSession(serverIP);
//            } else
//                session = serverUtil.getSession(serverIP+":read:read");
//            ChannelExec channel = (ChannelExec)session.openChannel("exec");
//            channel.setCommand(grepCmd);
//            Reporter.report.info(grepCmd, new Object[0]);
//            System.out.println("Grep Command is :" + grepCmd);
//            channel.connect();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
//            String temp;
//            while((temp = reader.readLine()) != null) {
//                resp =resp+temp;
//            }
//            channel.disconnect();
//            session.disconnect();
//            Reporter.report.info("Response of grep cmd :" +resp);
//        } catch (Exception var9) {
//            var9.printStackTrace();
//        }
//        return resp;
        String orderId=PG2LogsValidationHelper.getOrderId(grepCmd);
        String logFile=PG2LogsValidationHelper.getLogFile(grepCmd);
        PG2LogsValidationHelper pg2LogsValidationHelper=new PG2LogsValidationHelper();
        pg2LogsValidationHelper.buildRequest(logFile,orderId);
        String response=pg2LogsValidationHelper.executeESRequest(pg2LogsValidationHelper).toString();
        return response;
    }
    public static String verifyLogsOnPod( String serviceName,String orderId) throws InterruptedException {
        ServerConfigProvider.SERVICE service = ServerConfigProvider.SERVICE.valueOf(serviceName);
        ServerDetails serverDetails = ServerConfigProvider.getServerDetail(service);
        Boolean useEsForLogs = serverDetails.getUseEsForLogs();
        if (useEsForLogs) {
            String kibanaIndex = null;
            try {
                Field field = PG2LogsValidationHelper.setKibanaIndex.class.getField(serviceName);
                kibanaIndex = (String) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            PG2LogsValidationHelper pg2LogsValidationHelper=new PG2LogsValidationHelper();
            pg2LogsValidationHelper.buildNewRequest(kibanaIndex,orderId);
            String response=pg2LogsValidationHelper.executeESRequest(pg2LogsValidationHelper).toString();
            return response;
        }
        else {
            String ip = serverDetails.getIp();
            String userName = serverDetails.getUser();
            String password = serverDetails.getPassword();
            String details = ip + ":" + userName + ":" + password;
            String env = LocalConfig.ENV_NAME.toLowerCase();
            String svcName = serverDetails.getName();
            String logPath = serverDetails.getLogPath();
            String grepCmd = "grep \"" + orderId + "\" " + logPath;
            String logs = "";
            try {
                //Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> (LogsValidationHelper.getLogsOnK8sServer(details, env, svcName, grepCmd).contains(orderId)));
               // logs = getLogsOnK8sServer(details, env, svcName, grepCmd);
                logs = Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> LogsValidationHelper.getLogsOnK8sServer(details, env, svcName, grepCmd), s -> !"".equals(s));
            } catch (Exception e) {
                System.out.println("Logs not found for orderId : " + orderId);
                System.out.println(e);
            }
            return logs;
        }
    }
    public static String verifyLogsOnPod( String serviceName,String orderId,String requestLogger)  {
        System.out.println("Inside verify logs method");
        ServerConfigProvider.SERVICE service = ServerConfigProvider.SERVICE.valueOf(serviceName);
        ServerDetails serverDetails = ServerConfigProvider.getServerDetail(service);
        System.out.println("Server details are"+ serverDetails.toString());
        Boolean useEsForLogs = serverDetails.getUseEsForLogs();
        if (useEsForLogs) {
            String kibanaIndex = null;
            try {
                Field field = PG2LogsValidationHelper.setKibanaIndex.class.getField(serviceName);
                kibanaIndex = (String) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            PG2LogsValidationHelper pg2LogsValidationHelper=new PG2LogsValidationHelper(requestLogger);
            pg2LogsValidationHelper.buildNewRequest(kibanaIndex,orderId,requestLogger);
          String response= null;
          try {
            response = pg2LogsValidationHelper.executeESRequest(pg2LogsValidationHelper).toString();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          return response;
        }
        else {
            String ip = serverDetails.getIp();
            String userName = serverDetails.getUser();
            String password = serverDetails.getPassword();
            String details = ip + ":" + userName + ":" + password;
            String env = LocalConfig.ENV_NAME.toLowerCase();
            String svcName = serverDetails.getName();
            String logPath = serverDetails.getLogPath();
            String grepCmd = "grep \"" + orderId + "\" " + logPath + " | grep \"" + requestLogger + "\"";
            String logs = "";
            try {
                //Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> (LogsValidationHelper.getLogsOnK8sServer(details, env, svcName, grepCmd).contains(orderId)));
                //logs = getLogsOnK8sServer(details, env, svcName, grepCmd);
                logs = Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> LogsValidationHelper.getLogsOnK8sServer(details, env, svcName, grepCmd), s -> !"".equals(s));
            } catch (Exception e) {
                System.out.println("Logs not found for orderId : " + orderId + " requestLogger : " + requestLogger);
                System.out.println(e);
            }
            return logs;
        }
    }
    public static String verifyLogsOnPod(String serviceName, String orderId, String requestLogger, String type) throws InterruptedException {
        ServerConfigProvider.SERVICE service = ServerConfigProvider.SERVICE.valueOf(serviceName);
        ServerDetails serverDetails = ServerConfigProvider.getServerDetail(service);
        Boolean useEsForLogs = serverDetails.getUseEsForLogs();
        if (useEsForLogs) {
            String kibanaIndex = null;
            try {
                Field field = PG2LogsValidationHelper.setKibanaIndex.class.getField(serviceName);
                kibanaIndex = (String) field.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            PG2LogsValidationHelper pg2LogsValidationHelper=new PG2LogsValidationHelper(requestLogger,type);
            pg2LogsValidationHelper.buildNewRequestWithMultipleParams(kibanaIndex,orderId,requestLogger,type);
            String response=pg2LogsValidationHelper.executeESRequest(pg2LogsValidationHelper).toString();
            return response;
        }
        else {
            String ip = serverDetails.getIp();
            String userName = serverDetails.getUser();
            String password = serverDetails.getPassword();
            String details = ip + ":" + userName + ":" + password;
            String env = LocalConfig.ENV_NAME.toLowerCase();
            String svcName = serverDetails.getName();
            String logPath = serverDetails.getLogPath();
            String grepCmd = "grep \"" + orderId + "\" " + logPath + " | grep \"" + requestLogger + "\"" + " | grep \"" + type + "\"";
            String logs = "";
            try {
               // Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> (LogsValidationHelper.getLogsOnK8sServer(details, env, svcName, grepCmd).contains(orderId)));
               // logs = getLogsOnK8sServer(details, env, svcName, grepCmd);
                logs = Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> LogsValidationHelper.getLogsOnK8sServer(details, env, svcName, grepCmd), s -> !"".equals(s));
            } catch (Exception e) {
                System.out.println("Logs not found for orderId : " + orderId + " requestLogger : " + requestLogger + " type : " + type);
                System.out.println(e);
            }
            return logs;
        }
    }
    public static String getLogsOnK8sServer(String serverIP, String nameSpace, String svcName, String grepCmd) {
        String resp = "";
        try {
            ServerUtil serverUtil = new ServerUtil();
            Session session = null;
            if (serverIP.contains(":")) {
                session = serverUtil.getSession(serverIP);
            } else
                session = serverUtil.getSession(serverIP + ":qa_auto_jenkins:Paytm@1234");
            System.out.println(serverIP+" - Server details");
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            String newCommand = " sh fetchLogsFromPod.sh " + nameSpace + " " + svcName + " " + grepCmd;
            channel.setCommand(newCommand);
            System.out.println("Command to exec : "+newCommand);
            Reporter.report.info(grepCmd, new Object[0]);
            System.out.println("Grep Command is : " + grepCmd);
            channel.connect();
            System.out.println(channel.getInputStream()+" Helo");
            System.out.println((new InputStreamReader(channel.getInputStream())).toString());
            BufferedReader reader = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            String temp;
            while ((temp = reader.readLine()) != null) {
                resp = resp + temp;
            }
            channel.disconnect();
            session.disconnect();
            Reporter.report.info("Response of grep cmd :" + resp);
        } catch (Exception var9) {
            var9.printStackTrace();
        }
        return resp.replaceAll("\\\\", "");
    }
}