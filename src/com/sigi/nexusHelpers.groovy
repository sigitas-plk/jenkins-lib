package com.sigi

import java.util.regex.Pattern


private static String[] getCommits(String toTagOrHash, String fromTagOrHash = 'HEAD'){
    return "git log --pretty=commit-body-start%B --no-merges $toTagOrHash..$fromTagOrHash".execute().text.split('commit-body-start').each { it.trim() }.findAll{ it}
}


private static List<Map> getCommitTitleAndTickets(String[] commits, Pattern ticketRegex = JobConstants.jiraTicketRegex){
        return commits.findAll{ shouldIncludeToChangeList(it, ['chore', 'fix', 'test']) }
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

def body = getCommits('v1')

println getCommitTitleAndTickets(body)


def test(String x = 'HEAD'){
    println x
}


test()
//def x =  """new-commit-linesomething is there
//
//new-commit-lineantother commit
//with body
//
//new-commit-linesomething else""".split('new-commit-line').each { it.trim()}.findAll{ it}
//
//println x
//
//x.each { println it}
