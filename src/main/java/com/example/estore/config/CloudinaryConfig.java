package com.example.estore.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils; // ✅ use Cloudinary's ObjectUtils

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dn84fhbnk",
                "api_key", "354429764493774",
                "api_secret", "dcvq6jY7dd6-WR9Zj4GRkV8mgs8"
        ));
    }
}

