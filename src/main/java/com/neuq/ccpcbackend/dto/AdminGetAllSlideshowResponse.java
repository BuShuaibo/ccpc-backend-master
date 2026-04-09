package com.neuq.ccpcbackend.dto;

import com.neuq.ccpcbackend.entity.Slideshow;
import lombok.Data;

import java.util.List;

@Data
public class AdminGetAllSlideshowResponse {
    Long count;
    List<Slideshow> slideshows;
}
