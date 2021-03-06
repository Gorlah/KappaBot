package com.gorlah.kappabot.rest.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.List;

@Value
public class ValidationErrorResponse {

    List<ValidationError> errors;

    @Value
    public static class ValidationError {

        @Schema(example = "author")
        String field;

        @Schema(example = "must not be empty")
        String message;
    }
}
