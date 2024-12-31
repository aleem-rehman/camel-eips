package com.example.dao;

import java.util.HashMap;
import java.util.Map;

public  class InMemoryRepository {

    private final Map<String, String> repository = new HashMap<>();

    public boolean add(String key, String value) {
        return repository.put(key, value) == null;
    }

    public String get(String key) {
        return repository.get(key);
    }

    public void clear() {
        repository.clear();
    }

}
