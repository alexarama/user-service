package com.example.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/api/events")
    Map<String, Object> getEvents(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size);
}