package com.sigi


class JobConstants implements Serializable {
    public final static jiraTicketRegex = ~ /((?<!([A-Za-z]{1,10})-?)[A-Z]+-\\d+)/
    public final static nexusUploadUrl = 'http://nexus-container:8081/nexus/service/local/artifact/maven/content'

}