package kaiquebt.dev.auth.interfaces;

import kaiquebt.dev.auth.model.BaseUser;
import kaiquebt.dev.auth.model.BaseUserSessionLog;

public interface IUserSessionLogInstantiator<T extends BaseUser, U extends BaseUserSessionLog<T>> {
    U instantiate(T user);
}