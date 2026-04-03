package com.prajjwal.userservice.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
public class Packet<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private boolean success;
    private T data;
    private ErrorDto error;

    public Packet<T> ok(T data) {
        this.success = true;
        this.data = data;
        this.error = null;
        return this;
    }

    public Packet<T> error(HttpStatus status, Object message) {
        this.success = false;
        this.data = null;
        this.error = new ErrorDto(status.value(), message, null);
        return this;
    }

    public Packet<T> notFound(String object) {
        return error(HttpStatus.NOT_FOUND, object + "not found");
    }

    public Packet<T> badRequest(Object message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    public Packet<T> unauthorized(Object message) {
        return error(HttpStatus.UNAUTHORIZED, message);
    }

    public Packet<T> forbidden(Object message) {
        return error(HttpStatus.FORBIDDEN, message);
    }

    public Packet<T> internalError(Object message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public Packet<T> created(T data) {
        this.success = true;
        this.data = data;
        this.error = new ErrorDto(HttpStatus.CREATED.value(), "Created", null);
        return this;
    }

    public HttpStatus getHttpStatus() {
        return (error != null) ? HttpStatus.valueOf(error.getCode()) : HttpStatus.OK;
    }
}