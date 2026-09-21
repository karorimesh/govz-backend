package ke.eworks.ripoti.ai;

import jakarta.validation.constraints.NotBlank;

public record AudioSummaryRequest(@NotBlank String audioUrl) {
}