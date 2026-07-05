package com.paytm.framework.utils;

import com.jcraft.jsch.*;
import com.jcraft.jsch.Session;
import com.paytm.framework.reporting.Reporter;

import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class ServerUtil {

    private static Map<String, Session> sshMap = new HashMap<>();

    public  void disconnectExecutionChannel(ChannelExec channelExec)
    { channelExec.disconnect();
    }

    public  void disconnectSession(Session session)
    { session.disconnect();
    }

    public ChannelExec getChannel(Session session, String channel)  throws  JSchException {
        ChannelExec channelExec = (ChannelExec) session.openChannel(channel);
           return channelExec;
    }
    public synchronized Session getSession(String serverProperty){
        String[] schemeSplit = serverProperty.split(":");
        String host = schemeSplit[0];
        String user = schemeSplit[1];
        String password = schemeSplit[2];
        try {
            Session session1 = null;
            if (sshMap.containsKey(host + ":" + user)) {
                session1 = sshMap.get(host + ":" + user);
                if (session1.isConnected()) {
                    return session1;
                } else
                    sshMap.remove(host + ":" + user);
            }
            JSch jschObj = new JSch();
            session1 = jschObj.getSession(user, host);
            session1.setPassword(password);
            session1.setConfig("StrictHostKeyChecking", "no");
            session1.connect();
            sshMap.put(host + ":" + user, session1);
            return session1;
        } catch (Exception e) {
            Reporter.report.error("Couldn't getSession "+e.getMessage());
            return null;
        }
    }
}
