package com.sigi

import com.sigi.gitHelpers

import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

class GitHelpersSpec extends Specification {

    def 'should return true if given string starts with one of the strings in ignoreCommitTypes' () {

        given: 'title starts with ci, and ignore list contains "ci"'
        def ignoreCommitTypes = ['ci' , 'something']

        expect: 'should return true'
        gitHelpers.ignoreTitle('ci: anything', ignoreCommitTypes)
    }

    def 'should return true if given title does not start with a string in the ignoreCommitTypes list' () {

        given: 'title starts with ci'
        def title = 'ci: sample commit title'

        and: 'ignore list does not contain "ci"'
        def ignore = ['feat', 'test']

        expect: 'should return false'
        !gitHelpers.ignoreTitle(title, ignore)
    }

    def 'should return false if ignoreCommitTypes is empty' () {

        given: 'title starts with anything'
        def title = 'ci: sample commit title'

        and: 'is empty'
        def ignore = []

        expect: 'should return false'
        !gitHelpers.ignoreTitle(title, ignore)
    }

    def 'should return false if ignoreCommitTypes is falsy' () {

        given: 'title starts with anything'
        def title = 'ci: sample commit title'

        and: 'is falsy'
        def ignore = null

        expect: 'should return false'
        !gitHelpers.ignoreTitle(title, ignore)
    }

    @ConfineMetaClassChanges([gitHelpers])
    def 'it should return map with title and tickets returned by getCommitTitle and getCommitJiraTickets' () {

        setup:
        GroovySpy(gitHelpers, global: true)
        def hash = 'asdfasdfLKJLKJ'
        def title = 'ci: sample title'
        def tickets = ['JIR-1232', 'JIR-232']

        when: 'getCommitTitle and getCommitJiraTickets return given variables'
        gitHelpers.getCommitTitle(hash) >> title
        gitHelpers.getCommitJiraTickets(hash) >> tickets
        gitHelpers.commitHashExists(hash) >> true

        then: 'returned map contains title and tickets with getCommitTitle and '
        gitHelpers.getCommitTitleAndTickets(hash) == [title: title, tickets: tickets]
    }

    @ConfineMetaClassChanges([gitHelpers])
    def 'getChangeList should return list with filtered [title, tickets] map' () {

        given: 'commit list'
        List list =['test', 'ci' , 'fix']

        when: 'commit titles containing test, ci and fix types'
        gitHelpers.metaClass.static.getPreviousTag = { String tag -> tag }
        gitHelpers.metaClass.static.getCommitsBetweenTags = {
            String curTag, String prevTag -> list
        }
        gitHelpers.metaClass.static.getCommitTitleAndTickets =  { String type ->  [title: "${type}: title", tickets: []]}

        then: 'should only return test and fix commit maps'
        gitHelpers.getChangeList('something', ['ci']) == [[title: 'test: title', tickets: []], [title: 'fix: title', tickets: []]]
    }

    @ConfineMetaClassChanges([gitHelpers])
    def 'getChangeList should return empty list if all commits contain types in ignoreCommitTypes' () {

        given: 'commit list'
        List list =['test', 'ci' , 'fix']

        when: 'commit titles containing test, ci and fix types'
        gitHelpers.metaClass.static.getPreviousTag = { String tag -> tag }
        gitHelpers.metaClass.static.getCommitsBetweenTags = {
            String curTag, String prevTag -> list
        }
        gitHelpers.metaClass.static.getCommitTitleAndTickets =  { String type ->  [title: "${type}: title", tickets: []]}

        then: 'should filter out all commits'
        gitHelpers.getChangeList('something', ['ci', 'test', 'fix']) == []
    }

}