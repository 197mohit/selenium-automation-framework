package com.paytm.configmanager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestDataUtil {
    private static final ObjectMapper objectMapper = ObjectMapperSingleton.getInstance();

    public static <T> T loadData(String path, Class<T> clazz) throws IOException {
        File file = new File(path);
        return objectMapper.readValue(file, clazz);
    }
}
