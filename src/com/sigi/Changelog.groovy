package com.sigi

import com.sigi.ioc.ContextRegistry

class Changelog implements Serializable {
    private String _buildVersion
    private List<String> _ignoreCommits

    Changelog(String buildVersion, List<String> ignoreCommitTypes = null){
        _buildVersion = buildVersion
        _ignoreCommits = ignoreCommitTypes ? ignoreCommitTypes : ['chore', 'ci', 'docs', 'test']
    }

    List<String> generateChangeLog(String toTag = null){
        def changeList = ChangeList.getChangeList(_ignoreCommits, toTag)
        if(!changeList){
            ContextRegistry.getContext().getStepExecutor().echo "No changes found. Skipping generation of changelist markdown."
            return null
        }
        return getChangelogMarkdown(_buildVersion, changeList)
    }

    static boolean writeChangelog(List<String> changeList, String file = 'changelog.md'){
        def steps = ContextRegistry.getContext().getStepExecutor()
        if(!changeList){
            steps.error "Change lines are required"
        }
        String change = getPrettyListString(changeList)
       return !steps.sh ("""
            if [ -e "$file" ]; then
                echo "Appending changes to $file"
                echo "$change" | cat - "$file" > temp && mv temp "$file"
            else 
               echo "Creating $file and writing changes."
               echo "$change" > $file
            fi        
            """, true)
    }

    private static String getPrettyListString(List<String> list){
        return list.join(System.getProperty("line.separator")) + System.getProperty("line.separator")
    }

    private static List<String> getChangelogMarkdown(String version, List<Map> changes, String jiraUrl = null) {
        if (!version || !changes) {
            ContextRegistry.getContext().getStepExecutor().error 'Version and list of changes are required to generate changelog markdown'
        }
        def heading = ["## ${version} - ${new Date().format('yyyy-MM-dd')}"]
        return heading + changes.collect { change ->
            change.title ? " - $change.title${getTicketsMarkdown(jiraUrl, change.tickets as List<String>)}" : null
        }.findAll{ p -> p as Boolean}
    }

    private static getTicketsMarkdown(String url,List<String> tickets) {
        if(!tickets || tickets.isEmpty()) {
            return ''
        }
        return ' ' + tickets.collect { ticket -> !url ? ticket :  "[$ticket]($url$ticket)" }.join(' ')
    }

}
