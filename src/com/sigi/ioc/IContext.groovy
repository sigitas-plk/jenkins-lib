package com.sigi.ioc

import com.sigi.IStepExecutor

interface IContext {
    IStepExecutor getStepExecutor()
}