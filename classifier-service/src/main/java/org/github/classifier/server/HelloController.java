package org.github.classifier.server;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String index() {
    return "Greetings from Spring Boot!";
  }


  @RequestMapping(value = "/", method = RequestMethod.POST)
  public void consume(@RequestBody JsonNode node) {

    System.out.print("RECEIVED!!!");
  }
}