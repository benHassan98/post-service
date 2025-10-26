package com.odinbook.postservice.controller;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.odinbook.service.ImageService;

@RestController
public class HomeController {
  private final ImageService imageService;

  @Autowired
  public HomeController(ImageService imageService) {
    this.imageService = imageService;
  }

  @GetMapping("")
  public ResponseEntity<?> findImageList(@RequestHeader("idList") String idListString) {
    var imageList = Arrays.asList(idListString.split(","))
        .stream()
        .map(id -> {
          var args = id.split("/");
          return this.imageService.getImage(args[0], args[1]);
        })
        .collect(Collectors.toList());

    return ResponseEntity.ok(imageList);
  }

}
