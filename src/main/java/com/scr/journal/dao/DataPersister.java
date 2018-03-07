package com.scr.journal.dao;

public interface DataPersister<T> {
    void persist(T data);
}
