package kaiquebt.dev.auth.service;

import kaiquebt.dev.auth.model.BaseUser;

public interface IEmailTemplateBean<T extends BaseUser> {
    public String build(T user, String emailConfirmationToken);
    public String getEmailConfirmTitle();
}
