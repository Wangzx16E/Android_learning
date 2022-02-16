package ru.threeguns.engine.manager;

import java.util.List;

import kh.hyper.core.IM;
import ru.threeguns.engine.controller.InternalUser;
import ru.threeguns.engine.manager.impl.FastInfoCacheDBImpl;

@IM(FastInfoCacheDBImpl.class)
public interface FastInfoCache {
    List<InternalUser> getAllUsers();

    void updateUser(InternalUser user);

    InternalUser getUserById(String id);

    void deleteUserById(String id);

    String getActiveUserId();

    InternalUser getActiveUser();

    void setActiveUserId(String id);

}
