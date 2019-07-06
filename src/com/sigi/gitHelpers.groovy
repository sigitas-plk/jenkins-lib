package com.sigi

import com.sigi.Constants
//class gitHelpers {
//    public final static jiraTicketRegex = /((?<!([A-Za-z]{1,10})-?)[A-Z]+-\\d+)/

    static boolean ignoreTitle(title, List<String> ignoreCommitTypes){
        if(!title || !ignoreCommitTypes || ignoreCommitTypes.isEmpty()){
            return false
        }
        return title.matches(/(?i)^[${ignoreCommitTypes.join('|')}](.*)/)
    }

    static Map getCommitTitleAndTickets(String commitHash){
        if(!commitHashExists(commitHash)){
            throw new Exception("Given commit hash '${commitHash}' does not exist")
        }
        return [title: getCommitTitle(commitHash), tickets: getCommitJiraTickets(commitHash)]
    }

    static def getChangeList(String tag, List<String>ignoreCommitTypes){
        return getCommitsBetweenTags(tag, getPreviousTag(tag)).collect{ commit -> getCommitTitleAndTickets(commit)}.findAll{commit -> !ignoreTitle(commit.title, ignoreCommitTypes)}
    }

    static Boolean hasCommitsBetweenTags(String startTag, String endTag){
        return "git diff '${startTag}' '${endTag}' --stat".execute().text.trim().length() > 0
    }

    static List<String> getCommitsBetweenTags(String startTag, String endTag){
        return "git log --pretty=%H --no-merges '${startTag}'...'${endTag}'".execute().text.split(/\r?\n/)
    }

    static String getCommitTitle(String commitHash){
        return "git log --pretty=%s ${commitHash} -1".execute().text.replaceAll(Constants.jiraTicketRegex, '').trim()
    }

    static List<String> getCommitJiraTickets(commitHash) {
        return "git log ${commitHash} -1".execute().text.findAll(Constants.jiraTicketRegex).unique { a, b -> a <=> b }
    }

    static String getPreviousTag(String tag){
        if(!tagExists(tag)){
            throw new Exception("Given tag '${tag}' does not exist, please provide existing tag.")
        }
        def prevTag = "git describe --abbrev=0 --tags '${tag}'^".execute().text
        if(!prevTag){
            throw new Exception("No tags exist before given tag '${tag}', make sure you have commited your tag before running getPreviousTag method")
        }
        return prevTag
    }

    static Boolean tagExists(String tag){
        return "git tag -l '${tag}'".execute().text as Boolean
    }

    static Boolean commitHashExists(String hash) {
        return "git rev-parse -q --verify ${hash}".execute().text as Boolean
    }
//}