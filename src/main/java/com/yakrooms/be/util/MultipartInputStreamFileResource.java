package com.yakrooms.be.util;

import org.springframework.core.io.AbstractResource;
import java.io.IOException;
import java.io.InputStream;

public class MultipartInputStreamFileResource extends AbstractResource {

    private final InputStream inputStream;
    private final String filename;

    public MultipartInputStreamFileResource(InputStream inputStream, String filename) {
        this.inputStream = inputStream;
        this.filename = filename;
    }

    @Override
    public String getDescription() {
        return "Multipart file resource for " + filename;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }
}