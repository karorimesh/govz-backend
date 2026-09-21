package ke.eworks.ripoti.helpline;

import java.time.Instant;
import java.util.List;
import ke.eworks.ripoti.util.NotFoundException;
import org.springframework.stereotype.Service;


@Service
public class HelplineMessageService {

    private final HelplineMessageRepository repository;

    public HelplineMessageService(final HelplineMessageRepository repository) {
        this.repository = repository;
    }

    public List<HelplineMessageDTO> findAll() {
        return repository.findAll().stream()
                .map(message -> mapToDTO(message, new HelplineMessageDTO()))
                .toList();
    }

    public HelplineMessageDTO get(final String id) {
        return repository.findById(id)
                .map(message -> mapToDTO(message, new HelplineMessageDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public HelplineMessageDTO create(final HelplineMessageDTO request) {
        final HelplineMessage message = new HelplineMessage();
        mapToEntity(request, message);
        final String now = Instant.now().toString();
        message.setSubmittedAt(request.getSubmittedAt() == null ? now : request.getSubmittedAt());
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        if (message.getStatus() == null) {
            message.setStatus(MessageStatus.new_message);
        }
        return mapToDTO(repository.save(message), new HelplineMessageDTO());
    }

    public HelplineMessageDTO createGenerated(final HelplineMessage message) {
        final String now = Instant.now().toString();
        message.setSubmittedAt(message.getSubmittedAt() == null ? now : message.getSubmittedAt());
        message.setCreatedAt(now);
        message.setUpdatedAt(now);
        if (message.getStatus() == null) {
            message.setStatus(MessageStatus.new_message);
        }
        return mapToDTO(repository.save(message), new HelplineMessageDTO());
    }

    public HelplineMessageDTO update(final String id, final HelplineMessageDTO request) {
        final HelplineMessage message = repository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(request, message);
        message.setSubmittedAt(request.getSubmittedAt() == null ? message.getSubmittedAt() : request.getSubmittedAt());
        message.setUpdatedAt(Instant.now().toString());
        return mapToDTO(repository.save(message), new HelplineMessageDTO());
    }

    public void delete(final String id) {
        repository.findById(id).orElseThrow(NotFoundException::new);
        repository.deleteById(id);
    }

    private HelplineMessageDTO mapToDTO(final HelplineMessage source, final HelplineMessageDTO target) {
        target.setId(source.getId());
        target.setCountry(source.getCountry());
        target.setTitle(source.getTitle());
        target.setMessage(source.getMessage());
        target.setCategory(source.getCategory());
        target.setUrgency(source.getUrgency());
        target.setStatus(source.getStatus());
        target.setLocation(source.getLocation());
        target.setSender(source.getSender());
        target.setMaskedSender(source.getMaskedSender());
        target.setAttachments(source.getAttachments());
        target.setClassification(source.getClassification());
        target.setSubmittedAt(source.getSubmittedAt());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }

    private void mapToEntity(final HelplineMessageDTO source, final HelplineMessage target) {
        target.setCountry(source.getCountry());
        target.setTitle(source.getTitle());
        target.setMessage(source.getMessage());
        target.setCategory(source.getCategory());
        target.setUrgency(source.getUrgency());
        target.setStatus(source.getStatus());
        target.setLocation(source.getLocation());
        target.setSender(source.getSender());
        target.setMaskedSender(source.getMaskedSender());
        target.setAttachments(source.getAttachments());
        target.setClassification(source.getClassification());
    }

}