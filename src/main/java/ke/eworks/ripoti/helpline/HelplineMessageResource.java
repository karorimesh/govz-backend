package ke.eworks.ripoti.helpline;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/helpline-messages", produces = MediaType.APPLICATION_JSON_VALUE)
public class HelplineMessageResource {

    private final HelplineMessageService service;

    public HelplineMessageResource(final HelplineMessageService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<HelplineMessageDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HelplineMessageDTO> get(@PathVariable final String id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PostMapping
    public ResponseEntity<HelplineMessageDTO> create(@RequestBody @Valid final HelplineMessageDTO request) {
        return new ResponseEntity<>(service.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HelplineMessageDTO> update(@PathVariable final String id,
            @RequestBody @Valid final HelplineMessageDTO request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable final String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}