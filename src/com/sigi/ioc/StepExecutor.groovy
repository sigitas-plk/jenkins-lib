package com.sigi.ioc

class StepExecutor implements IStepExecutor {

    private _steps

    StepExecutor(steps) {
        this._steps = steps
    }

    @Override
    int sh(String command,Boolean returnStatus) {
        this._steps.sh returnStatus: returnStatus, script: "${command}"
    }

    @Override
    String sh(String command) {
        this._steps.sh returnStdout: true, script: "${command}"
    }

    String sh(GString command){
        return sh(command as String)
    }

    @Override
    void error(String message) {
        this._steps.error(message)
    }

    @Override
    void echo(String message) {
        this._steps.echo(message)
    }
}