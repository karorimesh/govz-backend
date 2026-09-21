package ke.eworks.ripoti.helpline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import ke.eworks.ripoti.util.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class HelplineMessageServiceTest {

    @Mock
    private HelplineMessageRepository repository;

    @InjectMocks
    private HelplineMessageService service;

    @Test
    void createMapsNestedFieldsAndAppliesDefaults() {
        final HelplineMessageDTO request = request();
        when(repository.save(any(HelplineMessage.class))).thenAnswer(invocation -> {
            final HelplineMessage saved = invocation.getArgument(0);
            saved.setId("message-1");
            return saved;
        });

        final HelplineMessageDTO result = service.create(request);

        assertThat(result.getId()).isEqualTo("message-1");
        assertThat(result.getCategory()).isEqualTo(MessageCategory.emergency);
        assertThat(result.getStatus()).isEqualTo(MessageStatus.new_message);
        assertThat(result.getSubmittedAt()).isNotBlank();
        assertThat(result.getCreatedAt()).isEqualTo(result.getUpdatedAt());
        assertThat(result.getClassification().getDepartmentId()).isEqualTo("department-1");
        assertThat(result.getAttachments()).singleElement().satisfies(attachment ->
                assertThat(attachment.getFileName()).isEqualTo("photo.png"));
    }

    @Test
    void createGeneratedAppliesLifecycleFieldsAndPreservesRecording() {
        final HelplineAttachment recording = new HelplineAttachment();
        recording.setUrl("https://example.test/call.mp3");
        final HelplineMessage generated = new HelplineMessage();
        generated.setTitle("Generated report");
        generated.setAttachments(List.of(recording));
        when(repository.save(any(HelplineMessage.class))).thenAnswer(invocation -> {
            final HelplineMessage saved = invocation.getArgument(0);
            saved.setId("message-1");
            return saved;
        });

        final HelplineMessageDTO result = service.createGenerated(generated);

        assertThat(result.getId()).isEqualTo("message-1");
        assertThat(result.getStatus()).isEqualTo(MessageStatus.new_message);
        assertThat(result.getSubmittedAt()).isNotBlank();
        assertThat(result.getCreatedAt()).isEqualTo(result.getUpdatedAt());
        assertThat(result.getAttachments()).singleElement().satisfies(attachment ->
                assertThat(attachment.getUrl()).isEqualTo("https://example.test/call.mp3"));
    }

    @Test
    void updatePreservesCreatedAtAndAllowsStatusChange() {
        final HelplineMessage existing = new HelplineMessage();
        existing.setId("message-1");
        existing.setCreatedAt("2026-09-20T10:00:00Z");
        existing.setUpdatedAt("2026-09-20T10:00:00Z");
        when(repository.findById("message-1")).thenReturn(Optional.of(existing));
        when(repository.save(any(HelplineMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final HelplineMessageDTO request = request();
        request.setStatus(MessageStatus.in_progress);

        final HelplineMessageDTO result = service.update("message-1", request);

        assertThat(result.getStatus()).isEqualTo(MessageStatus.in_progress);
        assertThat(result.getCreatedAt()).isEqualTo("2026-09-20T10:00:00Z");
        assertThat(result.getUpdatedAt()).isNotEqualTo(result.getCreatedAt());
    }

    @Test
    void missingMessageCannotBeUpdated() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update("missing", request()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteChecksThatMessageExistsBeforeDeleting() {
        when(repository.findById("message-1")).thenReturn(Optional.of(new HelplineMessage()));

        service.delete("message-1");

        verify(repository).deleteById("message-1");
    }

    private HelplineMessageDTO request() {
        final HelplineMaskedSender maskedSender = new HelplineMaskedSender();
        maskedSender.setName("Anonymous");

        final HelplineAttachment attachment = new HelplineAttachment();
        attachment.setId("attachment-1");
        attachment.setFileName("photo.png");
        attachment.setFileType("image/png");
        attachment.setUrl("https://example.test/photo.png");

        final HelplineClassification classification = new HelplineClassification();
        classification.setDepartmentId("department-1");
        classification.setConfidence(0.95);
        classification.setReason("Emergency keyword detected");

        final HelplineMessageDTO request = new HelplineMessageDTO();
        request.setCountry("Kenya");
        request.setTitle("Urgent assistance");
        request.setMessage("Please send help");
        request.setCategory(MessageCategory.emergency);
        request.setUrgency(MessageUrgency.critical);
        request.setMaskedSender(maskedSender);
        request.setAttachments(List.of(attachment));
        request.setClassification(classification);
        return request;
    }

}