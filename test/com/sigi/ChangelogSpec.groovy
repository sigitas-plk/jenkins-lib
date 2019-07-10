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

        @ConfineMetaClassChanges([GitUtils])
        def "If tagExists returns false should call error step" () {

        given: 'any tag where tagExists returns false'
        GitUtils.metaClass.static.tagExists = { String str ->  false }
        def n = new Changelog('v1')

        when:'called generate method'
        n.generate()

        then: 'error step with message should be called'
        1 * _steps.error(_ as String)

    }
}