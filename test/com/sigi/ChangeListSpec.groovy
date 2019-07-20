package com.sigi

import com.sigi.ioc.ContextRegistry
import com.sigi.ioc.IContext
import com.sigi.ioc.IStepExecutor
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

class ChangeListSpec extends Specification {


    private IContext _context
    private IStepExecutor _steps

    def setup(){
        _context = GroovyMock(IContext)
        _steps = GroovyMock(IStepExecutor)
        _context.getStepExecutor() >> _steps
        ContextRegistry.registerContext(_context)

    }

    def "getClosestTag should return trimmed tag" () {

        given: 'sh step returns 0.0.001 with whitespaces'
        def tag = '0.0.001'
        _steps.sh(_ as String) >> "$tag    "

        when:'called getClosestTag method'
        def actual = ChangeList.getClosestTag()

        then: 'returns trimmed tag'
        actual == tag
    }

    def "getClosestTag should call error step if no tags are return" () {

        given: 'sh step is called with expected git command and returns empty string'
        def tag = ' '
        _steps.sh(_ as String) >> tag

        when:'called getClosestTag method'
        ChangeList.getClosestTag()

        then: 'returns trimmed tag'
        1 * _steps.error(_ as String)
    }

    def "getCommits splits commits to String[] by string delimiter" () {

        given : 'git returns commit strings separated with commit-body-start'
        def separator = 'commit-body-start'
        _steps.sh(_ as GString) >> " ${separator}feat: some title ${separator}ci: another commit"

        when:
        def actual = ChangeList.getCommits('v1')

        then:
        String[] expected = ["feat: some title", "ci: another commit"]
        actual == expected
    }

    def "getCommits should return empty array if git log returns nothing" () {

        given : 'git returns commit strings separated with commit-body-start'
        _steps.sh(_ as GString) >> ""

        when:
        def actual = ChangeList.getCommits('v1')

        then:
        actual == [] as String []
    }

    @ConfineMetaClassChanges([ChangeList])
    def "getTitleAndTickets should return map with title and jira tickets" () {

        given: 'given title should be included'
        ChangeList.metaClass.static.shouldIncludeToChangeList = { String s,  List<String> list -> true }

        when:
        def actual = ChangeList.getTitleAndTickets(['feat: some title JIR-123 JIR-223'] as String[], [])

        then:
        def expected = [[title: 'feat: some title', tickets: ['JIR-123', 'JIR-223']]]
        actual == expected
    }

    @ConfineMetaClassChanges([ChangeList])
    def "getTitleAndTickets should exclude title if its matching ignore lis" () {

        given: 'given title should be excluded'
        ChangeList.metaClass.static.shouldIncludeToChangeList = { String s,  List<String> list -> false }

        when:
        def actual = ChangeList.getTitleAndTickets(['feat: some title JIR-123 JIR-223'] as String[], [])

        then:
        def expected = []
        actual == expected
    }

    @ConfineMetaClassChanges([ChangeList])
    def "getTitleAndTickets should not break if no jira tickets are present in a title" () {

        given: 'given title should be included'
        ChangeList.metaClass.static.shouldIncludeToChangeList = { String s,  List<String> list -> true }

        when:
        def actual = ChangeList.getTitleAndTickets(['feat: some title'] as String[], [])

        then:
        def expected = [[title: 'feat: some title', tickets: []]]
        actual == expected
    }

    @ConfineMetaClassChanges([ChangeList])
    def "getTitleAndTickets should return only title of commit and dismiss body" () {

        given: 'given title should be included'
        ChangeList.metaClass.static.shouldIncludeToChangeList = { String s,  List<String> list -> true }

        when: 'commit text is multi-line'
        def title = """test: commit

                    with body"""
        def actual = ChangeList.getTitleAndTickets([title] as String[], [])

        then:
        def expected = [[title: 'test: commit', tickets: []]]
        actual == expected
    }

    @ConfineMetaClassChanges([ChangeList])
    def "getTitleAndTickets should return jira tickets from anywhere in the commit body" () {

        given: 'given title should be included'
        ChangeList.metaClass.static.shouldIncludeToChangeList = { String s,  List<String> list -> true }

        when: 'commit text is multi-line'
        def title = """test: commit JIR-123

                    with body JIR-23423
                    
                    JIR-1231"""
        def actual = ChangeList.getTitleAndTickets([title] as String[], [])

        then:
        def expected = [[title: 'test: commit', tickets: ['JIR-123', 'JIR-23423', 'JIR-1231']]]
        actual == expected
    }

    @ConfineMetaClassChanges([ChangeList])
    def "getTitleAndTickets should only include unique jira tickets" () {

        given: 'given title should be included'
        ChangeList.metaClass.static.shouldIncludeToChangeList = { String s,  List<String> list -> true }

        when: 'commit text is multi-line'
        def title = """test: commit JIR-123 JIR-123

                    with body JIR-123
                    
                    JIR-222"""
        def actual = ChangeList.getTitleAndTickets([title] as String[], [])

        then:
        def expected = [[title: 'test: commit', tickets: ['JIR-123', 'JIR-222']]]
        actual == expected
    }

    def "shouldIncludeToChangeList should return false title starts with ignore keyword" () {

        expect:
        ChangeList.shouldIncludeToChangeList(title, ignore) == expected

        where:
        title               | ignore                        || expected
        'ci: something'     | ['ci']                        || false
        'test: something'   | ['ci']                        || true
        'fix: something'    | ['ci', 'fix']                 || false
        'chore: something'  | ['ci', 'fix', 'test']         || true
    }

    def "shouldIncludeToChangeList should not ignore title if it doesn't start with keyword" () {

        expect:
        ChangeList.shouldIncludeToChangeList(title, ignore) == expected

        where:
        title               | ignore          || expected
        'test: ci keyword'  | ['ci']          || true
        'cici: something'   | ['ci']          || true
        'fixture: something'| ['fix']         || true
        'cifix: something'  | ['ci', 'fix']   || true
        'chore: something'  | ['chore']       || false
    }

    def "shouldIncludeToChangeList should not be case sensitive" () {

        expect:
        ChangeList.shouldIncludeToChangeList(title, ignore) == expected

        where:
        title           | ignore        || expected
        'Ci: keyword'   | ['ci']        || false
        'CI: something' | ['ci']        || false
        'FiX: something'| ['fix']       || false
        'Ci: keyword'   | ['test']      || true
    }

    def "shouldIncludeToChangeList only exclude titles full word matches" () {

        expect:
        ChangeList.shouldIncludeToChangeList(title, ignore) == expected

        where:
        title               | ignore        || expected
        'ci: keyword'       | ['ci']        || false
        'ci : something'    | ['ci']        || false
        'cii: something'    | ['ci']        || true
        'ci. something'     | ['ci']        || false
        '    ci. something' | ['ci']        || false
    }

    def "shouldIncludeToChangeList should return true given any title if ignore list is empty unless title is empty" () {

        expect:
        ChangeList.shouldIncludeToChangeList(title, []) == expected

        where:
        title               || expected
        'ci: something'     || true
        ' '                 || false
        'fix: something'    || true
    }
}