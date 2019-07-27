package com.sigi.ioc

class StepExecutor implements IStepExecutor {

	private _steps

	StepExecutor(steps) {
		this._steps = steps
	}

	@Override
	int sh(String command, Boolean returnStatus) {
		this._steps.sh returnStatus: returnStatus, script: "${command}"
	}

	int sh(GString command, Boolean returnStatus) {
		return sh(command as String, returnStatus)
	}

	@Override
	String sh(String command) {
		this._steps.sh returnStdout: true, script: "${command}"
	}

	String sh(GString command) {
		return sh(command as String)
	}

	@Override
	void error(String message) {
		this._steps.error(message)
	}

	void error(GString message) {
		error(message as String)
	}

	@Override
	void echo(String message) {
		this._steps.echo(message)
	}

	@Override
	void emailext(String subject, String body, String to, String replyTo) {
		this._steps.emailext(subject: subject, body: body, to: to, replyTo: replyTo)
	}

	void emailext(String subject, GString body, String to, String replyTo) {
		emailext(subject, body as String, to, replyTo)
	}

	@Override
	boolean fileExists(String file) {
		this._steps.fileExists "$file"
	}
}