import com.sigi.changelog

import spock.lang.Specification

class ChangelogSpec extends Specification {

    static String resourceDir = 'test/resources/sigi/changelog'

    def "should return markdown for given list of changes" () {
        given:
        def changes = [
                [title: 'test: added test', tickets: ['JIR-123', 'JIR-2']],
                [title: 'fix: fixed something', tickets: ['JIR-2323']],
                [title: 'feat: added new feature',tickets: ['JIR-1']]
        ]
        def tag = '0.0.2'
        def jiraUrl = 'http://test-jira/url/'

        when:
        def formatted = changelog.getChangelogMarkdown(tag, changes,jiraUrl)

        then:
        def expected = [
                "### 0.0.2 - ${new Date().format('yyyy-MM-dd')} ###",
                '* test: added test [JIR-123](http://test-jira/url/JIR-123) [JIR-2](http://test-jira/url/JIR-2)',
                '* fix: fixed something [JIR-2323](http://test-jira/url/JIR-2323)',
                '* feat: added new feature [JIR-1](http://test-jira/url/JIR-1)'
        ]
        formatted == expected
    }

    def "should return markdown for given list of changes without tickets" () {
        given:
        def changes = [
                [title: 'test: added test', tickets: []],
                [title: 'fix: fixed something', tickets: []],
                [title: 'feat: added new feature',tickets: []]
        ]
        def tag = '0.0.2'
        def jiraUrl = 'http://test-jira/url/'

        when:
        def formatted = changelog.getChangelogMarkdown(tag, changes,jiraUrl)

        then:
        def expected = [
                "### 0.0.2 - ${new Date().format('yyyy-MM-dd')} ###",
                '* test: added test',
                '* fix: fixed something',
                '* feat: added new feature'
        ]
        formatted == expected
    }

    def "should not add bullet if title is missing" () {
        given:
        def changes = [
                [title: 'test: added test', tickets: ['JIR-232']],
                [title: '', tickets: ['JIR-343']],
                [title: 'feat: added new feature',tickets: ['JIR-232']]
        ]
        def tag = '0.0.2'
        def jiraUrl = 'http://test-jira/url/'

        when:
        def formatted = changelog.getChangelogMarkdown(tag, changes,jiraUrl)

        then:
        def expected = [
                "### 0.0.2 - ${new Date().format('yyyy-MM-dd')} ###",
                '* test: added test [JIR-232](http://test-jira/url/JIR-232)',
                '* feat: added new feature [JIR-232](http://test-jira/url/JIR-232)'
        ]
        formatted == expected
    }


    def "should return just jira tickets if no jiraUrl provide" () {
        given:
        def changes = [
                [title: 'test: added test', tickets: ['JIR-232']],
                [title: 'feat: added new feature',tickets: ['JIR-232']]
        ]
        def tag = '0.0.2'

        when:
        def formatted = changelog.getChangelogMarkdown(tag, changes)

        then:
        def expected = [
                "### 0.0.2 - ${new Date().format('yyyy-MM-dd')} ###",
                '* test: added test JIR-232',
                '* feat: added new feature JIR-232'
        ]
        formatted == expected
    }

    def 'should create directories if it do not exist' () {
        given:
        def path = resourceDir + '/createDirTest'
        def dir = new File(path)

        when:
        if(dir.exists()){
            assert dir.deleteDir()
        }
        changelog.appendLinesToFileStart(path, 'change.txt', ['line 1'] as List<String>)

        then:
        dir.exists()
    }

    def 'should create file if it doesn\'t exist' () {
        given:
        def path = resourceDir
        def fileName = 'createFileTest.md'
        def file = new File("${path}/${fileName}")
        if(file.exists()){
            assert file.delete()
        }

        when:
        changelog.appendLinesToFileStart(path, fileName, ['line 1'])

        then:
        file.exists()
    }

    def 'should write given lines to file' (){
        given:
        def path = resourceDir
        def fileName = 'changes.md'
        def file = new File("${path}/${fileName}")
        if(file.exists()){
            assert file.delete()
        }
        def lines = ['line 1', 'line 2', 'line 3']

        when:
        changelog.appendLinesToFileStart(path, fileName, lines)

        then: 'empty line at the top of the file + lines added'
        file.readLines() == ['', *lines]
    }

    def 'should append lines to file' () {
        given:
        def path =  resourceDir
        def fileName = 'changes.md'
        def file = new File("${path}/${fileName}")
        def lines = ['line 1', 'line 2', 'line 3']
        if(file.exists()){
            assert file.delete()
        }

        when:
        file << 'line 4'
        changelog.appendLinesToFileStart(path, fileName, lines)

        then:
        file.readLines() == ['', *lines, 'line 4']
    }
}