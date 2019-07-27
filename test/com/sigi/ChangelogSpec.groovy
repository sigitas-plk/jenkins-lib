package com.sigi

import com.sigi.ioc.ContextRegistry
import com.sigi.ioc.DefaultContext
import com.sigi.ioc.IContext
import com.sigi.ioc.IStepExecutor
import com.sigi.ioc.StepExecutor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges


class ChangelogSpec extends Specification {
	private IContext _context
	private IStepExecutor _steps

	def setup() {

		_context = GroovyMock(IContext)
		_steps = GroovyMock(IStepExecutor)
		_context.getStepExecutor() >> _steps
		ContextRegistry.registerContext(_context)

	}

	def "getTicketsMarkdown should return markdown links with urls"() {

		expect:
		Changelog.getTicketsMarkdown(url, tickets) == expected

		where:
		url                       | tickets                   || expected
		'http://jira.com/'        | ['JIR-123', 'TICKET-231'] || ' [JIR-123](http://jira.com/JIR-123) [TICKET-231](http://jira.com/TICKET-231)'
		' http://jira.com       ' | ['JIR-123']               || ' [JIR-123](http://jira.com/JIR-123)'
		'http://jira.com/'        | []                        || ''
		'http://something.co.uk/' | ['JIR-123']               || ' [JIR-123](http://something.co.uk/JIR-123)'
	}

	def "getTicketsMarkdown should return string with combined jira tickets only if no url provided"() {

		expect:
		Changelog.getTicketsMarkdown(url, tickets) == expected

		where:
		url   | tickets                   || expected
		''    | ['JIR-123', 'TICKET-231'] || ' JIR-123 TICKET-231'
		'   ' | ['JIR-123']               || ' JIR-123'
		null  | ['JIR-223']               || ' JIR-223'
	}

	def "getChangelogMarkdown should return expected markdown for changelist"() {

		given:
		def version = '200'
		def changeList = [
				[title: 'feat: some feature', tickets: ['JIR-123', 'JIRA-321']],
				[title: 'chore: code update', tickets: []]
		]

		when:
		def actual = Changelog.getChangelogMarkdown(version, changeList)

		then:
		String[] expected = ["## 200 - ${new Date().format('yyyy-MM-dd')}", " - feat: some feature JIR-123 JIRA-321", " - chore: code update"]
		actual == expected

	}

	def "getChangelogMarkdown skip items without title"() {

		given:
		def version = '200'
		def changeList = [
				[title: null, tickets: ['JIR-123', 'JIRA-321']],
				[title: 'chore: code update', tickets: []]

		]

		when:
		def actual = Changelog.getChangelogMarkdown(version, changeList)

		then:
		String[] expected = ["## 200 - ${new Date().format('yyyy-MM-dd')}", " - chore: code update"]
		actual == expected
	}

	def "getChangelogMarkdown should return markdown links if url provided"() {

		given:
		def version = ' 200'
		def changeList = [
				[title: 'test: something', tickets: ['JIR-123', 'JIRA-321']]]
		def url = 'http://some-company.com'

		when:
		def actual = Changelog.getChangelogMarkdown(version, changeList, url)

		then:
		String[] expected = ["## 200 - ${new Date().format('yyyy-MM-dd')}", " - test: something [JIR-123](http://some-company.com/JIR-123) [JIRA-321](http://some-company.com/JIRA-321)"]
		actual == expected
	}

	def "getChangelogMarkdown should call error if no version provided"() {

		given:
		def version = ''
		def changeList = [
				[title: 'test: something', tickets: ['JIR-123', 'JIRA-321']]]

		when:
		Changelog.getChangelogMarkdown(version, changeList)

		then:
		1 * _steps.error(_ as String)
	}

	def "getChangelogMarkdown should call error if no changelist provided"() {

		given:
		def version = '200'
		def changeList = null

		when:
		Changelog.getChangelogMarkdown(version, changeList)

		then:
		1 * _steps.error(_ as String)
	}

	def "getPrettyListString should return string with each item in new line"() {

		given:
		def list = ['item 1', 'item 2', 'item 3']
		def newLine = System.getProperty("line.separator")

		when:
		def actual = Changelog.getPrettyListString(list)

		then:
		actual == "item 1${newLine}item 2${newLine}item 3${newLine}"
	}

	def "getPrettyListString should not break given empty list"() {

		given:
		def list = []

		when:
		def actual = Changelog.getPrettyListString(list)

		then:
		actual == ''
	}

	def "writeChangelog should return return true if shell script returns 0"() {

		given:
		_steps.sh(_ as String, true) >> 0

		when:
		def actual = Changelog.writeChangelog([' '])

		then:
		actual

	}

	def "writeChangelog should return return true if shell script returns non-zero"() {

		given:
		_steps.sh(_ as GString, true) >> 1

		when:
		def actual = Changelog.writeChangelog([' '])

		then:
		!actual
	}

	def "writeChangelog call error step if given list is falsy"() {

		given:
		def list = []

		when:
		Changelog.writeChangelog(list)

		then:
		1 * _steps.error(_ as String)
	}

	@ConfineMetaClassChanges([ChangeList])
	def "writeChangelog should call getPrettyListString with changeList"() {
		GroovyMock(Changelog, global: true)

		given:
		def changeList = ['1', '2']

		when:
		Changelog.writeChangelog(changeList)

		then:
		1 * Changelog.writeChangelog(changeList)
	}

	def "mailChangelog should call error step if not provided any email address"() {

		given:
		String[] mailList = []

		when:
		Changelog.mailChangelog([], 'anything', mailList)

		then:
		1 * _steps.error(_ as String)

	}

	def "mailChangelog should call emailext with single email if only 1 provided"() {

		given:
		String[] mailList = ['single@mail.com']

		when:
		Changelog.mailChangelog([] as List<String>, '', mailList,)

		then:
		1 * _steps.emailext(_ as String, _ as GString, 'single@mail.com', null)

	}

	def "mailChangelog should email to combined list of emails"() {

		given:
		String[] mailList = ['single@mail.com', 'another@gmail.com', 'third@gmail.com']

		when:
		Changelog.mailChangelog([] as List<String>, '', mailList,)

		then:
		1 * _steps.emailext(_ as String, _ as GString, 'single@mail.com,another@gmail.com,third@gmail.com', null)

	}

	def "mailChangelog should email with body containing 'prettyList' of changes"() {

		given:
		String[] mailList = ['single@mail.com']
		List<String> changes = ['title', 'change 1', 'change 2']
		def changeLog = Changelog.getPrettyListString(changes)

		when:
		Changelog.mailChangelog(changes, '', mailList)

		then:
		1 * _steps.emailext(_ as String, _ as GString, mailList.first(), null) >> {
			assert it.get(1).indexOf(changeLog) >= 0
		}
	}

	def "mailChangelog should call emailext with subject"() {

		given:
		String subject = 'Email subject'

		when:
		Changelog.mailChangelog([] as List<String>, subject, 'email@mail.com')

		then:
		1 * _steps.emailext(subject, _ as GString, _ as String, null)
	}

	def "mailChangelog should not break given no subject"() {

		given:
		String subject = null

		when:
		Changelog.mailChangelog([] as List<String>, subject, ['email@mail.com'] as String[])

		then:
		1 * _steps.emailext(_ as String, _ as GString, _ as String, null)
	}

	def "mailChangelog should call emailext with replyTo if provided"() {

		given:
		String replyTo = 'admin@jenkins.com'

		when:
		Changelog.mailChangelog([] as List<String>, null, ['mailto@jenkins.com'] as String[], replyTo)

		then:
		1 * _steps.emailext(_ as String, _ as GString, _ as String, replyTo)
	}


	@ConfineMetaClassChanges([ChangeList])
	def "generateChangeLog should call getChangeList with tag and ignoreList"() {
		GroovyMock(ChangeList, global: true)

		given:
		def tag = '0.0.1'
		String[] ignoreList = ['test', 'ci', 'chore', 'docs']

		when:
		def change = new Changelog('300', ignoreList)
		change.generateChangeLog(tag)

		then:
		1 * ChangeList.getChangeList(ignoreList, tag)
	}

	@ConfineMetaClassChanges([ChangeList, Changelog])
	def "generateChangeLog should call echo step if empty changeList returned by getChangeList"() {

		given:
		ChangeList.metaClass.static.getChangeList = { String[] s, String str -> null }
		Changelog.metaClass.static.getChangelogMarkdown = { String s, List<Map> l -> null }

		when:
		def change = new Changelog('300', [] as String[])
		def actual = change.generateChangeLog('tag')

		then: 'should call echo step, and return empty list'
		1 * _steps.echo(_ as String)
		actual == []
	}
}