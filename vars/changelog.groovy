import com.sigi.Changelog
import com.sigi.ioc.ContextRegistry

def call(String toTagOrHash = null, String fromTagOrHash = null){

    ContextRegistry.registerDefaultContext(this)

    def log = new Changelog ('200')
    def changes = toTagOrHash ? log.generateChangeLog(toTagOrHash) : log.generateChangeLog()

    if(changes){
        log.writeChangelog(changes,'changelog.md')
    }
}
