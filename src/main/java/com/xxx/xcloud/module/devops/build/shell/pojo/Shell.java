package com.xxx.xcloud.module.devops.build.shell.pojo;

import javax.xml.bind.annotation.XmlElement;

public class Shell {

    private String command;

    private int unstableReturn;

    @XmlElement(name = "command")
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @XmlElement(name = "unstableReturn")
    public int getUnstableReturn() {
        return unstableReturn;
    }

    public void setUnstableReturn(int unstableReturn) {
        this.unstableReturn = unstableReturn;
    }

}
