package com.sigi.ioc

interface IStepExecutor {
    int sh(String command, Boolean returnStatus)
    String sh(String command)
    void error(String message)
    void echo(String message)
}