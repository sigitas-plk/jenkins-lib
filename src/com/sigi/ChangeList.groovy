package com.sigi

import com.sigi.ioc.ContextRegistry

import java.util.regex.Pattern

class ChangeList implements Serializable {

	static List<Map> getChangeList(String[] ignoreCommitTypes = [], String toTag = null, String fromTag = null) {
		String to = toTag ? toTag : getClosestTag()
		String[] commits = fromTag ? getCommits(to, fromTag) : getCommits(to)
		return getTitleAndTickets(commits, ignoreCommitTypes)
	}

	static String getClosestTag() {
		def steps = ContextRegistry.getContext().getStepExecutor()
		String tag = steps.sh("git describe --tags --abbrev=0").trim()
		if (!tag) {
			steps.error "No tags found."
			return ''
		}
		return tag
	}

	static String[] getCommits(String toTagOrHash, String fromTagOrHash = 'HEAD') {
		return ContextRegistry.getContext().getStepExecutor()
				.sh("git log --pretty=commit-body-start%B --no-merges $toTagOrHash..$fromTagOrHash")
				.split('commit-body-start')
				.collect { it.trim() }
				.findAll { it }
	}

	static List<Map> getTitleAndTickets(String[] commits, String[] ignore, Pattern ticketRegex = JobConstants.jiraTicketRegex) {
		return commits.findAll { shouldIncludeToChangeList(it, ignore) }
				.collect { commit ->
					[title  : commit.split(/\r?\n/).first().replaceAll(ticketRegex, '').trim(),
					 tickets: commit.findAll(ticketRegex).unique { a, b -> a <=> b }]
				}
	}

	static boolean shouldIncludeToChangeList(title, String[] ignoreTypes) {
		String trimmedTitle = title.trim()
		if (!trimmedTitle) {
			return false
		}
		if (!ignoreTypes) {
			return true
		}
		return !(trimmedTitle ==~ /(?i)^(${ignoreTypes.join('|')})\b(.*)/)
	}
}