package ee.app.conversamanager.interfaces;

/** package */ interface FirebaseCallback<T1, T2 extends Throwable> {
    void done(T1 t1, T2 t2);
}