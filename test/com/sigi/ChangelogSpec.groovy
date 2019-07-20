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

        def setup(){
          _context = GroovyMock(IContext)
          _steps = GroovyMock(IStepExecutor)
          _context.getStepExecutor() >> _steps
            ContextRegistry.registerContext(_context)

        }

    def "getTicketsMarkdown should return markdown links with urls" () {

        expect:
        Changelog.getTicketsMarkdown(url, tickets) == expected

        where:
        url                         |  tickets                       || expected
        'http://jira.com/'          |  ['JIR-123', 'TICKET-231']     || '[JIR-123](http://jira.com/JIR-123) [TICKET-231](http://jira.com/TICKET-231)'
        ' http://jira.com       '   |  ['JIR-123']                   || '[JIR-123](http://jira.com/JIR-123)'
        'http://jira.com/'          |  []                            || ''
        'http://something.co.uk/'   |  ['JIR-123']                   || '[JIR-123](http://something.co.uk/JIR-123)'
    }

    def "getTicketsMarkdown should return string with combined jira tickets only if no url provided" () {

        expect:
        Changelog.getTicketsMarkdown(url, tickets) == expected

        where:
        url     |  tickets                       || expected
        ''      |  ['JIR-123', 'TICKET-231']     || 'JIR-123 TICKET-231'
        '   '   |  ['JIR-123']                   || 'JIR-123'
        null    |  ['JIR-223']                   || 'JIR-223'
    }
}

//        @ConfineMetaClassChanges([GitUtils])
//        def "If tagExists returns false should call error step" () {
//
//        given: 'any tag where tagExists returns false'
//        GitUtils.metaClass.static.tagExists = { String str ->  false }
//        def n = new Changelog('v1')
//
//        when:'called generate method'
//        n.generate()
//
//        then: 'error step with message should be called'
//        1 * _steps.error(_ as String)
//
//    }
//}