package com.example.webflux.Models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeepAliveMessage {
    private String message;
}
