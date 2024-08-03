package com.example.webflux.Models;

import lombok.Data;

@Data
public class ResponseMessage {
    private String message;

    public ResponseMessage notFound(){
        this.message="Not have any person.";
        return this;
    }
    public ResponseMessage internalServerError(){
        this.message="Internal server error.";
        return this;
    }
    public ResponseMessage success(){
        this.message="add success.";
        return this;
    }
}
