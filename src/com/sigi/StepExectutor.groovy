package com.sigi

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

    @Override
    void error(String message) {
        this._steps.error(message)
    }
}