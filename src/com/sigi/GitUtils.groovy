package com.sigi


import com.sigi.ioc.ContextRegistry

class GitUtils implements Serializable {

	static boolean config(String user, String email) {
		def steps = ContextRegistry.getContext().getStepExecutor()

		String name = user ? user.trim() : null
		String mail = email ? email.trim() : null

		if (!name || !mail) {
			steps.error "Git user name and email are required for GitUtils to work as expected"
			return false
		}

		def status = steps.sh "git config user.name \"${name}\" && git config user.email \"${mail}\" ", true
		return status == 0
	}

	static boolean pushTag(String tag) {
		def steps = ContextRegistry.getContext().getStepExecutor()
		def normalizedTag = tag ? tag.trim().toLowerCase() : null
		if (!normalizedTag) {
			steps.error "Expected valid string for tag instead got '${tag}'"
			return false
		}

		return 0 == steps.sh("git tag ${normalizedTag} && git push --tags", true)
	}

	static boolean commitFiles(String[] files, String commitMessage) {
		def steps = ContextRegistry.getContext().getStepExecutor()
		def message = commitMessage ? commitMessage.trim() : null

		if (!files) {
			steps.error "Expected list of files to be commited, instead got $files"
		}

		if (!message) {
			steps.error "Expected valid commit message, instead got '${commitMessage}'"
		}

		def normalizedFiles = files.collect { it ? it.trim() : null }.findAll { it }.each { file ->
			if (!steps.fileExists(file)) {
				steps.error "'$file' does not exits. Make sure you are providing path relative to jenkins working directory."
			}
		}

		return 0 == steps.sh("git add \"${normalizedFiles.join('" "')}\" && git commit -m \"${message}\"", true)
	}

}