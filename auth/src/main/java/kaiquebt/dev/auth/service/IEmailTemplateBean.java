package kaiquebt.dev.auth.service;

import kaiquebt.dev.auth.model.BaseUser;

public interface IEmailTemplateBean<T extends BaseUser> {
    public String buildEmailConfirm(T user, String emailConfirmationToken);
    public String getEmailConfirmTitle();
    public String getRecoverAccountTitle();
    public String buildRecoverAccount(T user, String recoverToken);
}
