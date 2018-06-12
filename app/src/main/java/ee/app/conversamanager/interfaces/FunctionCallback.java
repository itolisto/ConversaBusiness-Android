package ee.app.conversamanager.interfaces;

import ee.app.conversamanager.networking.FirebaseCustomException;

public interface FunctionCallback<T> extends FirebaseCallback<T, FirebaseCustomException> {
    @Override
    void done(T t, FirebaseCustomException e);
}