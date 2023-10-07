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
    public void createBlobs(String dir, MultipartFile[] imageList) {
        Arrays.stream(imageList).forEach((image)->{
            try {
                new BlobServiceClientBuilder()
                        .connectionString(connectStr)
                        .buildClient()
                        .getBlobContainerClient("images")
                        .getBlobClient(dir+"/"+image.getName())
                        .upload(image.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();

            }

        });
    }

    @Override
    public byte[] findBlob(String blobName) {

        return new BlobServiceClientBuilder()
                .connectionString(connectStr)
                .buildClient()
                .getBlobContainerClient("images")
                .getBlobClient(blobName)
                .downloadContent().toBytes();
    }

    @Override
    public String injectImagesToHTML(String html, MultipartFile[] imageList) {
        AtomicInteger index = new AtomicInteger();

        Document document =  Jsoup.parse(html);
        document
                .select("img")
                .replaceAll(element ->
                        element.attributes().hasKey("class")?
                                element.attr("src",imageList[index.getAndIncrement()].getName())
                                        .removeAttr("class"):
                                element
                );

        return document.body().html();

    }

    @Override
    public void deleteImages(String dir, MultipartFile[] imageList) {

        Arrays.stream(imageList).forEach((image)->{

                new BlobServiceClientBuilder()
                        .connectionString(connectStr)
                        .buildClient()
                        .getBlobContainerClient("images")
                        .getBlobClient(dir+"/"+image.getName())
                        .delete();


        });
    }
}
