package com.example.userservice.service;

import com.example.userservice.client.EventServiceClient;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RefreshScope
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventServiceClient eventServiceClient;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User cu id " + id + " nu a fost găsit"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User " + username + " nu a fost găsit"));
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Saving user: {}", user.getUsername());
        return userRepository.save(user);
    }

    public void delete(Long id) {
        findById(id);
        log.info("Deleting user with id: {}", id);
        userRepository.deleteById(id);
    }

    @CircuitBreaker(name = "eventService", fallbackMethod = "getEventsFallback")
    @Retry(name = "eventService")
    public Map<String, Object> getUserEvents() {
        log.info("Fetching events from event-service");
        return eventServiceClient.getEvents(0, 5);
    }

    public Map<String, Object> getEventsFallback(Exception ex) {
        log.error("Circuit breaker activated for getUserEvents: {}", ex.getMessage());
        return Collections.singletonMap("error", "Serviciul de evenimente nu este disponibil momentan");
    }
}