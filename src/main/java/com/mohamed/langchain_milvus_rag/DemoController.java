package com.mohamed.langchain_milvus_rag;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DemoController {
    @GetMapping("/x")
    public String demo (){

        return "not.html";
    }
}
