package com.sigi

static List<String> getChangelogMarkdown(String version, List<Map> changes, String jiraUrl = null) {
    if (!version || !changes) {
        throw new Error('Version and list of changes are required to generate changelog list')
    }
    def heading = ["### ${version} - ${new Date().format('yyyy-MM-dd')} ###"]
    return heading + changes.collect { change ->
        change.title ? "* $change.title${getTicketsMarkup(jiraUrl, change.tickets as List<String>)}" : null
    }.findAll{ p -> p as Boolean}
}

private static getTicketsMarkup(String url,List<String> tickets) {
    if(!tickets || tickets.isEmpty()) {
        return ''
    }
    return ' ' + tickets.collect { ticket -> !url ? ticket :  "[$ticket]($url$ticket)" }.join(' ')
}

static void appendLinesToFileStart(String path, String file, List<String> lines) {
    def dir = new File(path)
    if(!dir.exists()){
        println "Creating directory path ${path}"
        dir.mkdirs()
    }
    def f = new File("$path/$file")
    def n = System.getProperty("line.separator")
    if(!f.exists()){
        println "Creating file ${path}/${file}"
        f << n
    }

    def oldLines = f.readLines()
    f.withWriter { writer ->
        lines.each { line ->
            writer.append("$n$line")
        }
        oldLines.each { line ->
            writer.append("$n$line")
        }
    }
}
