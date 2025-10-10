package kaiquebt.dev.auth.service;

public interface IPasswordValidator {
    public void doValidate(String password) throws IllegalArgumentException;
}
