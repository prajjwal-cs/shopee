package com.prajjwal.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Dictionary;

@Data
@AllArgsConstructor
public class ErrorDto implements Serializable {
    private int code;
    private Object message;
    private Dictionary<String, String> errors;
}