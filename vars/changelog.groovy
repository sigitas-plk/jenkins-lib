import com.sigi.Changelog
import com.sigi.ioc.ContextRegistry

def call(String tagFrom, String tagTo = null){

    ContextRegistry.registerDefaultContext(this)

    def log = new Changelog (tagFrom, tagTo)
    log.generate()

}
