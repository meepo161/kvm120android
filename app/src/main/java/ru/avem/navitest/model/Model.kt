package ru.avem.navitest.model

import java.util.*

object Model : Observable(), Observer {
    override fun update(observable: Observable, values: Any) {
        val values = (values as Array<*>)
        val action = values[0]
        val value = values[1]
        notice(action, value)
    }

    private fun notice(param: Any?, value: Any?) {
        setChanged()
        notifyObservers(arrayOf(param, value))
    }
}
