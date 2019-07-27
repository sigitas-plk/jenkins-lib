package com.sigi

import com.sigi.ioc.ContextRegistry
import com.sigi.ioc.IContext
import com.sigi.ioc.IStepExecutor
import spock.lang.Specification


class GitUtilsSpec extends Specification {
    private IContext _context
    private IStepExecutor _steps

    def setup() {

        _context = GroovyMock(IContext)
        _steps = GroovyMock(IStepExecutor)
        _context.getStepExecutor() >> _steps
        ContextRegistry.registerContext(_context)

    }

    def "commitFiles should check if fileExists and if not should call error step with the file name" () {

        given: 'second call with file name returns false'
        String [] file = ['file.md ', 'file2.md' , 'file3.md']
        _steps.fileExists(_ as String) >>> [ true, false, true ]

        when:
        GitUtils.commitFiles(file, 'something')

        then:
        1 * _steps.error(_ as GString ) >> { assert it.get(0).indexOf('file2.md') >= 0 }
    }

    def "commitFiles should call error step if provided with invalid message" () {

        given:
        String message = null
        _steps.fileExists(_ as String ) >> true

        when:
        GitUtils.commitFiles(['file.txt'] as String [],  message)

        then:
        1 * _steps.error(_ as GString ) >> { assert it.get(0).indexOf("$message") >= 0 }
    }

    def "commitFiles should call git add with file name surrounded by quotes to respect spaces in file names" () {

        given:
        String[] files = ['somePath/with spaces/file.txt']
        _steps.fileExists(_ as String ) >> true

        when:
        GitUtils.commitFiles(files,  'anything')

        then:
        1 * _steps.sh(_ as GString , _ as Boolean ) >> {
            assert it.get(0).indexOf('git add "somePath/with spaces/file.txt"') >= 0
            return 0
        }
    }


    def "commitFiles should call git add with all files in given list" () {

        given:
        String[] files = ['      file1.txt      ', 'file2.txt' ,'file3.txt']
        _steps.fileExists(_ as String ) >> true

        when:
        GitUtils.commitFiles(files,  'anything')

        then:
        1 * _steps.sh(_ as GString , _ as Boolean ) >> {
            assert it.get(0).indexOf('git add "file1.txt" "file2.txt" "file3.txt"') >= 0
            return 0
        }
    }

    def "commitFiles should filter out invalid or empty file names" () {

        given:
        def emptyString = '       '
        String[] files = [null, 'file3.txt', emptyString]
        _steps.fileExists(_ as String ) >> true

        when:
        GitUtils.commitFiles(files,  'anything')

        then:
        1 * _steps.sh(_ as GString , _ as Boolean ) >> {
            assert it.get(0).indexOf('null') == -1 && it.get(0).indexOf(emptyString) == -1
            return 0
        }
    }


    def "commitFiles should return true if steps.sh returns 0" () {

        given:
        String[] files = ['file3.txt']
        _steps.fileExists(_ as String ) >> true
        _steps.sh (_ as GString, true ) >> 0

        when:
        def actual = GitUtils.commitFiles(files,  'anything')

        then:
        actual
    }

}