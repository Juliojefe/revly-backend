package com.example.revly.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.revly.repository.PostImageRepository;

@Service
public class PostImageService {

    @Autowired
    private PostImageRepository postImageRepository;

    //  TODO
}