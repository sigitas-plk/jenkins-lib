package com.sigi

import com.sigi.ioc.ContextRegistry

class Changelog implements Serializable {
	private String _buildVersion
	private String[] _ignoreCommits

	Changelog(String buildVersion, String[] ignoreCommitTypes = null) {
		_buildVersion = buildVersion
		_ignoreCommits = ignoreCommitTypes ? ignoreCommitTypes : ['chore', 'ci', 'test']
	}

	String[] generateChangeLog(String toTag = null) {
		List<Map> changeList = ChangeList.getChangeList(_ignoreCommits, toTag)

		if (!changeList) {
			ContextRegistry.getContext().getStepExecutor().echo "No changes found. Skipping generation of changelist markdown."
			return []
		}

		return getChangelogMarkdown(_buildVersion, changeList)
	}

	static boolean writeChangelog(List<String> changeList, String file = 'changelog.md') {
		def steps = ContextRegistry.getContext().getStepExecutor()
		if (!changeList) {
			steps.error "Change lines are required"
			return false
		}

		String change = getPrettyListString(changeList)

		def status = steps.sh("""
            if [ -e "$file" ]; then
                echo "Appending changes to $file"
                echo "$change" | cat - "$file" > temp && mv temp "$file"
            else 
               echo "Creating $file and writing changes."
               echo "$change" > $file
            fi        
            """, true)
		return status == 0
	}

	static void mailChangelog(List<String> changeList, String subject, String[] mailTo, String replyTo = null) {
		def steps = ContextRegistry.getContext().getStepExecutor()
		if (!mailTo) {
			steps.error "mailChangelog requires at least one recipient email address"
			return
		}

		def emailList = mailTo.length == 1 ? mailTo.first() : mailTo.join(',')
		def changes = getPrettyListString(changeList)
		def newLine = System.getProperty("line.separator")
		def body = "${newLine}New build released. Please see the changelist as per commits below:${newLine}${changes}"

		steps.emailext(subject ? subject : 'New web build released', body, emailList, replyTo)
	}

	static String getPrettyListString(List<String> list) {
		if (!list) {
			return ''
		}
		return list.join(System.getProperty("line.separator")) + System.getProperty("line.separator")
	}

	static String[] getChangelogMarkdown(String version, List<Map> changes, String jiraUrl = null) {
		version = version ? version.trim() : ''
		if (!version || !changes || changes.isEmpty()) {
			ContextRegistry.getContext().getStepExecutor().error 'Version and list of changes are required to generate changelog markdown'
			return []
		}

		return ["## ${version} - ${new Date().format('yyyy-MM-dd')}"] + changes.collect { change ->
			change.title ? " - $change.title${getTicketsMarkdown(jiraUrl, change.tickets as List<String>)}" : null
		}.findAll { it }
	}

	static getTicketsMarkdown(String url, List<String> tickets) {
		if (!tickets) {
			return ''
		}

		url = url ? url.trim() : ''
		if (url) {
			url = url.endsWith('/') ? url : "$url/"
		}

		return " " + tickets.collect { ticket -> !url ? ticket : "[$ticket]($url$ticket)" }.join(' ')
	}
}
