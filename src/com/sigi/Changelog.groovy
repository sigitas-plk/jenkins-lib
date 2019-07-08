package com.sigi

import com.sigi.ioc.ContextRegistry

class Changelog implements Serializable {
    private String _from;
    private String _to;
    private IStepExecutor _steps

    Changelog(String fromTag, String toTag){
        _from = fromTag
        _to = toTag
        _steps = ContextRegistry.getContext().getStepExecutor()
    }

    generate(){
        _steps.sh "git status"
        _steps.sh "tag ${_from} another ${_to}"
    }

}
