package com.sigi.ioc

interface IStepExecutor {
	int sh(String command, Boolean returnStatus)

	int sh(GString command, Boolean returnStatus)

	String sh(String command)

	String sh(GString command)

	void error(String message)

	void error(GString message)

	void echo(String message)

	void emailext(String subject, String body, String to, String replyTo)

	void emailext(String subject, GString body, String to, String replyTo)

	boolean fileExists(String file)
}