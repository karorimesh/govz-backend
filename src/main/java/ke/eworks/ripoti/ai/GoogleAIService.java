package ke.eworks.ripoti.ai;

import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ke.eworks.ripoti.department.DepartmentDTO;
import ke.eworks.ripoti.department.DepartmentService;
import ke.eworks.ripoti.department.DepartmentType;
import ke.eworks.ripoti.helpline.HelplineAttachment;
import ke.eworks.ripoti.helpline.HelplineClassification;
import ke.eworks.ripoti.helpline.HelplineLocation;
import ke.eworks.ripoti.helpline.HelplineMessage;
import ke.eworks.ripoti.helpline.MessageStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class GoogleAIService {

    private static final String PROMPT = """
            Convert this audio recording into a helpline message and route it to one of the departments below.
            Return only JSON matching the supplied response schema. Do not invent facts that are not audible.
            Use null for unknown optional location fields. The departmentId must match a listed department ID.
            Select departmentId OTHER when no specific listed department clearly matches the recording.
            Set officeId only when a listed office is clearly appropriate. Confidence must be between 0 and 1.

            Departments:
            %s
            """;

    private final GoogleAIGateway googleAIGateway;
    private final DepartmentService departmentService;
    private final ObjectMapper objectMapper;
    private final String model;

    public GoogleAIService(final GoogleAIGateway googleAIGateway,
            final DepartmentService departmentService,
            final ObjectMapper objectMapper,
            @Value("${google.ai.model}") final String model) {
        this.googleAIGateway = googleAIGateway;
        this.departmentService = departmentService;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public HelplineMessage summarize(final String audioUrl) {
        final List<DepartmentDTO> departments = departmentService.findAll();
        if (departments.isEmpty()) {
            throw new IllegalStateException("No departments are available for message classification");
        }
        final String mimeType = "audio/mpeg";
        final Content content = Content.fromParts(
                Part.fromText(PROMPT.formatted(serializeDepartments(departments))),
                Part.fromUri(audioUrl, mimeType));
        final GenerateContentConfig config = GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .responseJsonSchema(responseSchema())
                .build();
        final String response = googleAIGateway.generateContent(model, content, config);
        if (response == null || response.isBlank()) {
            throw new IllegalStateException("Google AI returned no helpline message");
        }
        return mapResponse(response, audioUrl, departments);

    }

    public String generateText(final String prompt) {
        final Content content = Content.fromParts(Part.fromText(prompt));
        final String generatedText = googleAIGateway.generateContent(model, content, null);
        if (generatedText == null || generatedText.isBlank()) {
            throw new IllegalStateException("Google AI returned no response");
        }
        return generatedText;
    }

    private String serializeDepartments(final List<DepartmentDTO> departments) {
        try {
            final List<Map<String, Object>> context = departments.stream()
                    .map(department -> Map.<String, Object>of(
                            "id", valueOrEmpty(department.getId()),
                            "country", valueOrEmpty(department.getCountry()),
                            "name", valueOrEmpty(department.getName()),
                            "type", department.getType() == null ? "" : department.getType().name(),
                            "description", valueOrEmpty(department.getDescription()),
                            "handlesCategories", listOrEmpty(department.getHandlesCategories()),
                            "keywords", listOrEmpty(department.getKeywords()),
                            "escalationOfficeId", valueOrEmpty(department.getEscalationOfficeId())))
                    .toList();
            return objectMapper.writeValueAsString(context);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize department context", exception);
        }
    }

    private HelplineMessage mapResponse(final String response, final String audioUrl,
            final List<DepartmentDTO> departments) {
        final AudioHelplineMessage generated;
        try {
            generated = objectMapper.readValue(response, AudioHelplineMessage.class);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Google AI returned an invalid helpline message", exception);
        }
        validate(generated, departments);

        final HelplineMessage message = new HelplineMessage();
        message.setCountry(generated.country());
        message.setTitle(generated.title());
        message.setMessage(generated.message());
        message.setCategory(generated.category());
        message.setUrgency(generated.urgency());
        message.setStatus(MessageStatus.new_message);
        message.setLocation(mapLocation(generated.location()));
        message.setClassification(mapClassification(generated.classification()));

        final HelplineAttachment recording = new HelplineAttachment();
        recording.setUrl(audioUrl);
        message.setAttachments(List.of(recording));
        return message;
    }

    private void validate(final AudioHelplineMessage generated, final List<DepartmentDTO> departments) {
        if (generated == null || isBlank(generated.country()) || isBlank(generated.title())
                || isBlank(generated.message()) || generated.category() == null || generated.urgency() == null
                || generated.classification() == null || isBlank(generated.classification().departmentId())
                || isBlank(generated.classification().reason()) || generated.classification().confidence() == null
                || generated.classification().confidence() < 0 || generated.classification().confidence() > 1) {
            throw new IllegalStateException("Google AI returned an incomplete helpline message");
        }

        final Set<String> departmentIds = departments.stream()
                .map(DepartmentDTO::getId)
                .collect(Collectors.toSet());
        if (!departmentIds.contains(generated.classification().departmentId())) {
            throw new IllegalStateException("Google AI selected an unknown department");
        }

        if (!isBlank(generated.classification().officeId())) {
            final Set<String> officeIds = departments.stream()
                    .filter(department -> department.getType() == DepartmentType.office)
                    .map(DepartmentDTO::getId)
                    .collect(Collectors.toSet());
            if (!officeIds.contains(generated.classification().officeId())) {
                throw new IllegalStateException("Google AI selected an unknown office");
            }
        }
    }

    private HelplineLocation mapLocation(final AudioHelplineMessage.AudioLocation generated) {
        if (generated == null) {
            return null;
        }
        final HelplineLocation location = new HelplineLocation();
        location.setCounty(generated.county());
        location.setConstituency(generated.constituency());
        location.setWard(generated.ward());
        location.setAddressText(generated.addressText());
        return location;
    }

    private HelplineClassification mapClassification(
            final AudioHelplineMessage.AudioClassification generated) {
        final HelplineClassification classification = new HelplineClassification();
        classification.setDepartmentId(generated.departmentId());
        classification.setOfficeId(generated.officeId());
        classification.setConfidence(generated.confidence());
        classification.setReason(generated.reason());
        return classification;
    }

    private Map<String, Object> responseSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("country", "title", "message", "category", "urgency", "classification"),
                "properties", Map.of(
                        "country", Map.of("type", "string"),
                        "title", Map.of("type", "string"),
                        "message", Map.of("type", "string"),
                        "category", Map.of("type", "string", "enum", List.of(
                                "complaint", "service_request", "corruption_report", "safety_concern",
                                "emergency", "feedback", "general_support")),
                        "urgency", Map.of("type", "string", "enum", List.of("low", "medium", "high", "critical")),
                        "location", Map.of(
                                "type", List.of("object", "null"),
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "county", Map.of("type", List.of("string", "null")),
                                        "constituency", Map.of("type", List.of("string", "null")),
                                        "ward", Map.of("type", List.of("string", "null")),
                                        "addressText", Map.of("type", List.of("string", "null")))),
                        "classification", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "required", List.of("departmentId", "confidence", "reason"),
                                "properties", Map.of(
                                        "departmentId", Map.of("type", "string"),
                                        "officeId", Map.of("type", List.of("string", "null")),
                                        "confidence", Map.of("type", "number", "minimum", 0, "maximum", 1),
                                        "reason", Map.of("type", "string")))));
    }

    private String valueOrEmpty(final String value) {
        return value == null ? "" : value;
    }

    private List<String> listOrEmpty(final List<String> values) {
        return values == null ? List.of() : values;
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}