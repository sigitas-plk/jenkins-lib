package com.sigi

import com.sigi.ioc.ContextRegistry

import java.util.regex.Pattern


    static boolean ignoreTitle(title, List<String> ignoreCommitTypes){
        if(!title || !ignoreCommitTypes || ignoreCommitTypes.isEmpty()){
            return false
        }
        return title.matches(/(?i)^[${ignoreCommitTypes.join('|')}](.*)/)
    }

    static Map getCommitTitleAndTickets(String commitHash){
        if(!commitHashExists(commitHash)){
            ContextRegistry.getContext().getStepExecutor().error "Given commit hash '${commitHash}' does not exist"
        }
        return [title: getCommitTitle(commitHash), tickets: getCommitJiraTickets(commitHash)]
    }

    static def getChangeList(String tag, List<String>ignoreCommitTypes){
        return getCommitsBetweenTags(tag, getPreviousTag(tag)).collect{ commit -> getCommitTitleAndTickets(commit)}.findAll{commit -> !ignoreTitle(commit.title, ignoreCommitTypes)}
    }

    static Boolean hasCommitsBetweenTags(String startTag, String endTag){
        return  ContextRegistry.getContext().getStepExecutor().sh("git diff '${startTag}' '${endTag}' --stat").trim().length() > 0
    }

    static List<String> getCommitsBetweenTags(String startTag, String endTag){
        return ContextRegistry.getContext().getStepExecutor().sh("git log --pretty=%H --no-merges '${startTag}'...'${endTag}'").split(/\r?\n/)
    }

    static String getCommitTitle(String commitHash, Pattern removeRegex = JobConstants.jiraTicketRegex){
        return  ContextRegistry.getContext().getStepExecutor().sh("git log --pretty=%s ${commitHash} -1").replaceAll(removeRegex, '').trim()
    }

    static List<String> getCommitJiraTickets(String commitHash, Pattern regex = JobConstants.jiraTicketRegex) {
        return  ContextRegistry.getContext().getStepExecutor().sh("git log ${commitHash} -1").findAll(regex).unique { a, b -> a <=> b }
    }

    static String getPreviousTag(String tag){
        def steps =  ContextRegistry.getContext().getStepExecutor()
        if(!tagExists(tag)){
            steps.error "Given tag '${tag}' does not exist, please provide existing tag."
        }
        def prevTag = steps.sh("git describe --abbrev=0 --tags '${tag}'^").trim()
        if(!prevTag){
          steps.error("No tags exist before given tag '${tag}', make sure you have commited your tag before running getPreviousTag method")
        }
        return prevTag
    }

    static Boolean tagExists(String tag){
        return ContextRegistry.getContext().getStepExecutor().sh("git tag -l '${tag}'").trim() as Boolean
    }

    static Boolean commitHashExists(String hash) {
        return ContextRegistry.getContext().getStepExecutor().sh("git rev-parse -q --verify ${hash}").trim() as Boolean
    }