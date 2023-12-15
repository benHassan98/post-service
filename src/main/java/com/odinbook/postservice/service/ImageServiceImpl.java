package com.odinbook.postservice.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ImageServiceImpl implements ImageService{
    @Value("${spring.cloud.azure.storage.connection-string}")
    private String connectStr;
    @Override
    public void deleteImages(String content) {

        BlobContainerClient blobContainerClient = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient()
                .getBlobContainerClient("images");

        Jsoup
                .parse(content)
                .select("img")
                .forEach(image->{
                    blobContainerClient
                            .getBlobClient(image.attr("src"))
                            .deleteIfExists();
                });

    }

    @Override
    public void deleteUnusedImages(String oldContent, String newContent) {

        BlobContainerClient blobContainerClient = new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient()
                .getBlobContainerClient("images");

        Elements newContentElements = Jsoup.parse(newContent).select("img");
        Jsoup
                .parse(oldContent)
                .select("img")
                .forEach(element -> {
                   boolean exists =  newContentElements
                            .stream()
                            .anyMatch(newElement->
                                    newElement.attr("src")
                                            .equals(element.attr("src"))
                            );
                if(!exists){
                    blobContainerClient
                            .getBlobClient(element.attr("src"))
                            .deleteIfExists();
                }

                });



    }
}
