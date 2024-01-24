package io.github.caijiang.common.lock.demo

import io.github.caijiang.common.lock.AutoLock
import org.springframework.stereotype.Service

/**
 * @author CJ
 */
@Service
open class LockWorker {

    @AutoLock
    open fun withoutParameters() {

    }

    @AutoLock
    open fun withDefaultAutoLock(arg1: String?) {

    }

    @AutoLock("name1")
    open fun lockWithName() {

    }

    @AutoLock(key = "#name+' this is key'")
    open fun withKeyExpr(name: String?) {
    }

    @AutoLock("lock1")
    @AutoLock("lock2")
    open fun withTwoLock() {
    }

}