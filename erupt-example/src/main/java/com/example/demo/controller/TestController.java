package com.example.demo.controller;

import com.example.demo.dao.ArticleRepository;
import com.example.demo.model.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.List;

@RestController
public class TestController {

    @Autowired
    private ArticleRepository articleRepository;

    @RequestMapping("/test")
    public List<Article> testArticle() {
        return articleRepository.findAll();
    }

    @RequestMapping("/test2")
    public int test2() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int compilationResult = compiler.run(null, null, null, "C:\\Test1111.java");

        return compilationResult;
    }
}