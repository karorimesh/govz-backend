package ke.eworks.ripoti.ai;

import jakarta.validation.constraints.NotBlank;

public record TextPromptRequest(@NotBlank String prompt) {
}