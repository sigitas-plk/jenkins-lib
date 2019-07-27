import com.sigi.Changelog
import com.sigi.GitUtils
import com.sigi.ioc.ContextRegistry

def call(String toTagOrHash = null, String fromTagOrHash = null) {

	ContextRegistry.registerDefaultContext(this)
	String build = env.BUILD_NUMBER.padLeft(3, '0')

	String changelogFile = 'changelog.md'
	String commitMessage = "ci: v${build} changelog added"

	String emailSubject = "Web build v${build} released"
	String[] mailList = ['sigitas@pleikys.com']


	Changelog changeLog = new Changelog(build)
	List<String> changes = toTagOrHash ? changeLog.generateChangeLog(toTagOrHash) : changeLog.generateChangeLog()

	if (changes) {
		def isSuccessful = []
		isSuccessful.push(changeLog.writeChangelog(changes, changelogFile))
		GitUtils.config('Sigitas', 'sigitas@mail.com')
		isSuccessful.push(GitUtils.pushTag(build))
		isSuccessful.push(GitUtils.commitFiles([changelogFile] as String[], commitMessage))
		changeLog.mailChangelog(changes, emailSubject, mailList, 'sigitas@mail.com')

		if(isSuccessful.findAll{!it}){
			currentBuild.result = "UNSTABLE"
		}
	}
}