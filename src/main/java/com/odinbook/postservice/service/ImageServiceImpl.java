package com.odinbook.postservice.service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.odinbook.postservice.DTO.ImageDTO;
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
    public void createBlobs(List<ImageDTO> imageList) throws RuntimeException{
        if(Objects.isNull(imageList))
            return;

        imageList.forEach((image)->{
            try {

                new BlobServiceClientBuilder()
                        .connectionString(connectStr)
                        .buildClient()
                        .getBlobContainerClient("images")
                        .getBlobClient(image.getId())
                        .upload(image.getFile().getInputStream());

            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }

        });
    }

    @Override
    public String injectImagesToHTML(String html, List<String> imageNameList) {
        AtomicInteger index = new AtomicInteger();

        Document document =  Jsoup.parse(html);
        imageNameList.forEach(imageName->{
            document
                    .selectFirst("img[src=");

        });
        document
                .select("img[src=")
//                .first()
                .replaceAll(element ->
                        element.attributes().hasKey("is-new")?
                                element.attr("src",imageNameList.get(index.getAndIncrement()))
                                        .removeAttr("is-new"):
                                element.removeAttr("is-new")
                );

        return document.body().html();

    }

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
