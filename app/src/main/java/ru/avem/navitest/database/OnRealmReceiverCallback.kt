package ru.avem.navitest.database

import io.realm.Realm

interface OnRealmReceiverCallback {
    fun onRealmReceiver(realm: Realm?)
}