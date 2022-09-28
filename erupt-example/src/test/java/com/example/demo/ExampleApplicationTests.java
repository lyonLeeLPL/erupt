package com.example.demo;

import com.example.demo.dao.ArticleRepository;
import com.example.demo.model.Article;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.Date;

import static org.assertj.core.api.Assertions.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ExampleApplicationTests {

    @Autowired
    private ArticleRepository articleRepository; //使用方式与 mybatis-plus 大同小异

    //获取所有文章
    @Test
    void findArticleList() {
        for (Article article : articleRepository.findAll()) {
            System.out.println(article.toString());
        }
    }

    @Test
    public void TestGetAppLatestVersion() throws Exception{
        RequestBuilder request = null;
        //构造请求
        request = post("/appProducer/getAppLatestVersion")
                .param("appId", "1001");
    }

    //根据标题获取文章
    @Test
    void findArticleByTitle() {
        for (Article article : articleRepository.findByTitle("title")) {
            System.out.println(article.toString());
        }
    }

    //删除
    @Test
    void deleteArticle() {
//        articleRepository.deleteById(1L);
    }

    //新增 or 更新
    @Test
    void saveArticle() {
        Article article = new Article();
        article.setTitle("title");
        article.setTop(false);
        article.setPublish(true);
        article.setCreateTime(new Date());
        article.setContent("test test test ....");
        articleRepository.save(article);
    }


}
