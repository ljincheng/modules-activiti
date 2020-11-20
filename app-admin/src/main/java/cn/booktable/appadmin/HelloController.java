package cn.booktable.appadmin;

import cn.booktable.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("/test/")
public class HelloController {

    private HelloWorld helloWorld;



    public HelloController(HelloWorld helloWorld) {
        this.helloWorld = helloWorld;
    }

    @GetMapping("/hello")
    public String hello() {

        return helloWorld.hello();
    }


    @GetMapping("/anotherHelloworld")
    public String anotherHello() {
        return helloWorld.anotherHello();
    }

    public String test(){

        StringUtils.isNotBlank("Test");

        return "test";
    }
}
