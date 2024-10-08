package com.example.webflux.Models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Person {
    private int id;
    private String name;
    private int age;
    private String email;
    private String phone;
}
