package com.gorlah.kappabot.rest.model;

import com.gorlah.kappabot.function.BotRequestMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.With;

import javax.validation.constraints.NotEmpty;

@With
@Value
@Builder
@AllArgsConstructor
public class CommandRequest implements BotRequestMetadata {

    @NotEmpty
    @Schema(example = "Gorlah")
    String author;

    @NotEmpty
    @Schema(example = "/kb hello")
    String message;

    @Schema(example = "default")
    String source;
}
