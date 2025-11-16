package com.smart.edilek.core.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.edilek.core.models.LazyEvent;

@Component
public class LazyEventConverter implements Converter<String, LazyEvent> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LazyEvent convert(String source) {
        try {
            return objectMapper.readValue(source, LazyEvent.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid LazyEvent format");
        }
    }
}
