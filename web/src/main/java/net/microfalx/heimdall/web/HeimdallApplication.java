package net.microfalx.heimdall.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"net.microfalx.bootstrap", "net.microfalx.heimdall"})
public class HeimdallApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeimdallApplication.class, args);
    }
}
