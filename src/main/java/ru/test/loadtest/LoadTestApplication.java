package ru.test.loadtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;

@SpringBootApplication
public class LoadTestApplication {

    public static void main(String[] args) {

        var test = new ArrayList<Integer>();

        SpringApplication.run(LoadTestApplication.class, args);
    }

}
