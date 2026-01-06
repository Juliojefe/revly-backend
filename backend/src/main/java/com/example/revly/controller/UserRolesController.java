package com.example.revly.controller;

import com.example.revly.service.UserRolesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/user-roles")
public class UserRolesController {

    @Autowired
    private UserRolesService userRolesService;

    //  TODO
}