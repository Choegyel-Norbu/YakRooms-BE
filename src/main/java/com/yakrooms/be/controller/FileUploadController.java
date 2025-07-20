package com.yakrooms.be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.yakrooms.be.service.impl.UploadThingService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private UploadThingService uploadThingService;

    @Autowired
    public FileUploadController(UploadThingService uploadThingService) {
        this.uploadThingService = uploadThingService;
    }

    @PostMapping
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = uploadThingService.uploadFilesToUploadThing(files);
        return ResponseEntity.ok(urls);
    }
}