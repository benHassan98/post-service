package com.odinbook.postservice.service;

import com.azure.storage.blob.BlobServiceClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ImageServiceImpl implements ImageService{
    @Value("${spring.cloud.azure.storage.connection-string}")
    private String connectStr;
    @Override
    public void createBlobs(String dir, MultipartFile[] imageList) throws RuntimeException{
        Arrays.stream(imageList).forEach((image)->{
            try {
                new BlobServiceClientBuilder()
                        .connectionString(connectStr)
                        .buildClient()
                        .getBlobContainerClient("images")
                        .getBlobClient(dir+"/"+image.getName())
                        .upload(image.getInputStream());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

        });
    }

    @Override
    public String injectImagesToHTML(String html, MultipartFile[] imageList) {
        AtomicInteger index = new AtomicInteger();

        Document document =  Jsoup.parse(html);
        document
                .select("img")
                .replaceAll(element ->
                        element.attributes().hasKey("is-new")?
                                element.attr("src",imageList[index.getAndIncrement()].getName())
                                        .removeAttr("is-new"):
                                element.removeAttr("is-new")
                );

        return document.body().html();

    }

    @Override
    public void deleteImages(String dir) {

        new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient()
                .getBlobContainerClient("images")
                        .listBlobsByHierarchy(dir+"/")
                                .forEach(blobItem -> blobItem.setDeleted(true));

    }
}
