package com.jkrocha.shoplab.order.api;

import com.jkrocha.shoplab.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        String orderId = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("orderId", orderId));
    }
}
