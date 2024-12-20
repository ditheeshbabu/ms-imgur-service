package com.imgur.imgurservice.model.Imgurmodel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImgurResponse {

    private String status_code;
    private boolean success;
    private ImgurData data;
}
