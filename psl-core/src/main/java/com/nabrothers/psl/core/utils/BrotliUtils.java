package com.nabrothers.psl.core.utils;

import lombok.extern.log4j.Log4j2;
import org.brotli.dec.BrotliInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Log4j2
public class BrotliUtils {

    public static String toString(InputStream is) {
        try {
            BrotliInputStream stream = new BrotliInputStream(is);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                StringBuilder result = new StringBuilder();
                String str = null;
                while ((str = reader.readLine()) != null) {
                    result.append(str);
                }
                return result.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
}
