package com.sigi

import com.sigi.ioc.ContextRegistry

import java.util.regex.Pattern

class ChangeList implements Serializable {

    static List<Map> getChangeList(List<String> ignoreCommitTypes = [], String toTag = null, String fromTag = null){
        String to = toTag ? toTag : getClosestTag()
        String[] commits = fromTag ? getCommits(to, fromTag) : getCommits(to)
        return getTitleAndTickets(commits, ignoreCommitTypes)
    }

    private static String getClosestTag(){
        def steps = ContextRegistry.getContext().getStepExecutor()
        String tag = steps.sh("git describe --tags --abbrev=0").trim()
        if(!tag) {
            steps.error "No tags found."
        }
        return tag
    }

    private static String[] getCommits(String toTagOrHash, String fromTagOrHash = 'HEAD'){
        return  ContextRegistry.getContext().getStepExecutor()
                .sh("git log --pretty=commit-body-start%B --no-merges $toTagOrHash..$fromTagOrHash")
                .split('commit-body-start')
                .each { it.trim() }
                .findAll{ it }
    }

    private static List<Map> getTitleAndTickets(String[] commits, List<String> ignore,  Pattern ticketRegex = JobConstants.jiraTicketRegex){
        return commits.findAll{ shouldIncludeToChangeList(it, ignore) }
                .collect { commit ->
                    [title: commit.split(/\r?\n/).first().replaceAll(ticketRegex, '').trim(),
                     tickets: commit.findAll(ticketRegex).unique { a, b -> a <=> b }]
                }
    }

    private static boolean shouldIncludeToChangeList(title, List<String> ignoreTypes) {
        if (!title || !ignoreTypes) {
            return false
        }
        return !title.trim().matches(/(?i)^[${ignoreTypes.join('|')}](.*)/)
    }

//    private static Boolean tagExists(String tag) {
//        return ContextRegistry.getContext().getStepExecutor().sh("git tag -l '${tag}'").trim() as Boolean
//    }
//    static List getChangeList(List<String> ignoreCommitTypes = null){
//        return getCommitsFromHeadToTag(getClosestTag())
//                .collect { commit -> getCommitTitleAndTickets(commit) }
//                .findAll { commit -> shouldIncludeToChangeList(commit.title, ignoreCommitTypes) }
//    }
//
//    private static ArrayList<String> getCommitsFromHeadToTag(String tag){
//      return ContextRegistry.getContext().getStepExecutor()
//              .sh("git log --pretty=%H --no-merges $tag..HEAD")
//              .split(/\r?\n/)
//    }
//    private static Map getCommitTitleAndTickets(String hashOrTag, Pattern ticketRegex = JobConstants.jiraTicketRegex){
//        def commitBody =  ContextRegistry.getContext().getStepExecutor().sh "git log $hashOrTag --pretty=%B -1"
//        return [title: commitBody.split(/\r?\n/).first().replaceAll(ticketRegex, '').trim(),
//                tickets: commitBody.findAll(ticketRegex).unique { a, b -> a <=> b }]
//    }
//    private static boolean shouldIncludeToChangeList(title, List<String> ignoreTypes) {
//        if (!title || !ignoreTypes) {
//            return false
//        }
//        return !title.matches(/(?i)^[${ignoreTypes.join('|')}](.*)/)
//    }
//    private static String getPreviousTag(String tag = null) {
//        def steps = ContextRegistry.getContext().getStepExecutor()
//        def toTag =  tag ? '\'${tag}\'^' : ''
//        String prevTag = steps.sh("git describe --abbrev=0 --tags $toTag").trim()
//
//        if (!prevTag) {
//            steps.error("No tags previous tags found.")
//        }
//        return prevTag
//    }
//    private static Map getCommitTitleAndTickets(String commitHash) {
//        return [title: getCommitTitle(commitHash), tickets: getCommitJiraTickets(commitHash)]
//    }
//    private static String getCommitTitle(String commitHash, Pattern removeRegex = JobConstants.jiraTicketRegex) {
//        return ContextRegistry.getContext().getStepExecutor().sh("git log --pretty=%s ${commitHash} -1").replaceAll(removeRegex, '').trim()
//    }
//    private static List<String> getCommitJiraTickets(String commitHash, Pattern regex = JobConstants.jiraTicketRegex) {
//        return ContextRegistry.getContext().getStepExecutor().sh("git log ${commitHash} -1").findAll(regex).unique { a, b -> a <=> b }
//    }
//    private static getCommitText(String hash){
//        ContextRegistry.getContext().getStepExecutor().sh("git log ${commitHash} -1")
//    }
//    static List<Map> getChangeListBetweenTags(String tag, String prevTag, List<String> ignoreCommitTypes) {
//        if (!tagExists(tag)) {
//            ContextRegistry.getContext().getStepExecutor().error "Given tag '${tag}' does not exist, please provide existing tag."
//        }
//        return getCommitsBetweenTags(tag, prevTag).collect { commit -> getCommitTitleAndTickets(commit) }.findAll { commit -> !ignoreTitle(commit.title, ignoreCommitTypes) }
//    }
//    private static boolean ignoreTitle(title, List<String> ignoreCommitTypes) {
//        if (!title || !ignoreCommitTypes || ignoreCommitTypes.isEmpty()) {
//            return false
//        }
//        return title.matches(/(?i)^[${ignoreCommitTypes.join('|')}](.*)/)
//    }
//    private static Boolean hasCommitsBetweenTags(String startTag, String endTag) {
//        return ContextRegistry.getContext().getStepExecutor().sh("git diff '${startTag}' '${endTag}' --stat").trim().length() > 0
//    }
//    private static List<String> getCommitsBetweenTags(String startTag, String endTag) {
//        return ContextRegistry.getContext().getStepExecutor().sh("git log --pretty=%H --no-merges '${startTag}'...'${endTag}'").split(/\r?\n/)
//    }
//    private static Boolean tagExists(String tag) {
//        return ContextRegistry.getContext().getStepExecutor().sh("git tag -l '${tag}'").trim() as Boolean
//    }
//
//    private static Boolean commitHashExists(String hash) {
//        return ContextRegistry.getContext().getStepExecutor().sh("git rev-parse -q --verify ${hash}").trim() as Boolean
//    }
}