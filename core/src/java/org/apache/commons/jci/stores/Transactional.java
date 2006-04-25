package org.apache.commons.jci.stores;

public interface Transactional {

    void onStart();

    void onStop();
}
