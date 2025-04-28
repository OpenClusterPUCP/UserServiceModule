package com.example.userservicemodule.Beans;

import lombok.Data;

@Data
public class ImageRequest {
    private String name;
    private String type;
    private Integer userId;
    private String description;
    private String os;
    private String version;
}