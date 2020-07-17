package ru.avem.navitest.database.entities;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmModel;
import ru.avem.navitest.database.OnRealmReceiverCallback;

public class DatabaseAdapter {
    private Realm mRealm;

    public DatabaseAdapter(OnRealmReceiverCallback onRealmReceiverCallback) {
        mRealm = Realm.getDefaultInstance();
        onRealmReceiverCallback.onRealmReceiver(mRealm);
    }

    public Protocol getProtocolByName(String serialNumber) {
        return mRealm.where(Protocol.class).equalTo("mSerialNumber", serialNumber).findFirst();
    }

    private <E extends RealmModel> int getNextKey(Class<E> clazz) {
        try {
            Number number = mRealm.where(clazz).max("mId");
            if (number != null) {
                return number.intValue() + 1;
            } else {
                return 1;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 1;
        }
    }

    public void open() {
        mRealm.beginTransaction();
    }

    public List<Protocol> getProtocols() {
        return mRealm.where(Protocol.class).findAll();
    }


    public void updateProtocol(Protocol protocol) {
        mRealm.insertOrUpdate(protocol);
    }

    public long insertProtocol(Protocol protocol) {
        int nextKey = getNextKey(Protocol.class);
        protocol.setId(nextKey);
        mRealm.insertOrUpdate(protocol);
        return nextKey;
    }

    public void deleteProtocol(long id) {
        mRealm.where(Protocol.class).equalTo("mId", id).findAll().deleteFirstFromRealm();
    }
}
