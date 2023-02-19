package com.nabrothers.psl.server.service.impl;

import com.nabrothers.psl.sdk.service.CacheService;

import lombok.extern.log4j.Log4j2;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Log4j2
@Service
public class CacheServiceImpl implements CacheService {

    public static String DEFAULT_ROCKSDB_PATH = "./db";

    private static RocksDB rocksDB;

    static {
        RocksDB.loadLibrary();
        Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            rocksDB = RocksDB.open(options, DEFAULT_ROCKSDB_PATH);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(String category, String key, String value) {
        try {
            rocksDB.put((category + "_" + key).getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("RocksDB.put", e);
        }
    }

    @Override
    public String get(String category, String key) {
        try {
            byte[] bytes = rocksDB.get((category + "_" + key).getBytes(StandardCharsets.UTF_8));
            if (bytes == null) {
                return null;
            }
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RocksDB.get", e);
        }
        return null;
    }

    @Override
    public void delete(String category, String key) {
        try {
            rocksDB.delete((category + "_" + key).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("RocksDB.delete", e);
        }
    }
}
