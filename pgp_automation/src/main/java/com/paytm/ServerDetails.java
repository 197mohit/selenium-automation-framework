package com.paytm;

public class ServerDetails {
    private String ip, user, password, name, logPath;
    private Boolean useEsForLogs;

    public String getIp() {
        return ip;
    }

    public ServerDetails setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getUser() {
        return user;
    }

    public ServerDetails setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ServerDetails setPassword(String password) {
        this.password = password;
        return this;
    }
    public String getName() {
        return name;
    }

    public ServerDetails setName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getUseEsForLogs() {
        return useEsForLogs;
    }

    public ServerDetails setUseEsForLogs(Boolean useEsForLogs) {
        this.useEsForLogs = useEsForLogs;
        return this;
    }

    public String getLogPath() {
        return logPath;
    }

    public ServerDetails setLogPath(String logPath) {
        this.logPath = logPath;
        return this;
    }
}
