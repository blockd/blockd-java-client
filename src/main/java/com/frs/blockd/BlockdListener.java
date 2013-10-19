package com.frs.blockd;

public interface BlockdListener {

    /**
     * Called by a BlockdClient when an async ACQUIRE command
     * successfully obtains a lock.
     *
     * @param lockId    The acquired lock.
     */
    public void lockAquired(String lockId);

    /**
     * Called by a BlockdClient when an async ACQUIRE command
     * was unsuccessful in obtaining a lock.
     *
     * @param lockId    The failed lock.
     */
    public void lockFailed(String lockId);

}
