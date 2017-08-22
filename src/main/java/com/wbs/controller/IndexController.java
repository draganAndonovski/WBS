package com.wbs.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping(value = "/")
@Controller
public class IndexController {

    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/city", method = RequestMethod.POST)
    public String getCity(@RequestBody String city) {

        System.out.println(city);

        return "index";
    }
}
