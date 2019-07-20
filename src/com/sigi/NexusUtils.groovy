package com.sigi

class NexusUtils implements Serializable {


    def uploadFiles(){
       " curl -v r=1 hasPom=false e=zip g=2 a=test-project v=0.0.0 p=zip file=@test.zip -u admin:admin123 http://nexus-container:8081/nexus/service/local/artifact/maven/content"
    }
}