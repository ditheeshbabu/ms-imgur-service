package com.imgur.imgurservice.service;

import com.imgur.imgurservice.model.ImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImgService {
    ImageResponse uploadAndSaveImage(MultipartFile file, String username);

    void deleteImage(String imageId, String username);

    List<ImageResponse> getImagesByUsername(String accessToken);

    ImageResponse getImageById(String imageId);

}
