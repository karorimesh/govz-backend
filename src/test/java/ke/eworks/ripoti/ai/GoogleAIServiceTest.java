package ke.eworks.ripoti.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import java.util.List;
import ke.eworks.ripoti.department.DepartmentDTO;
import ke.eworks.ripoti.department.DepartmentService;
import ke.eworks.ripoti.department.DepartmentType;
import ke.eworks.ripoti.helpline.MessageCategory;
import ke.eworks.ripoti.helpline.MessageStatus;
import ke.eworks.ripoti.helpline.MessageUrgency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;


@ExtendWith(MockitoExtension.class)
class GoogleAIServiceTest {

    private static final String AUDIO_URL = "https://example.test/report.mp3";

    @Mock
    private GoogleAIGateway googleAIGateway;

    @Mock
    private DepartmentService departmentService;

    private GoogleAIService googleAIService;

    @BeforeEach
    void setUp() {
        googleAIService = new GoogleAIService(googleAIGateway, departmentService,
                new ObjectMapper(), "gemini-test");
    }

    @Test
    void summarizeCreatesClassifiedMessageAndAddsRecording() {
        when(departmentService.findAll()).thenReturn(List.of(
                department("health", "Ministry of Health", DepartmentType.department),
                department("clinic", "Central Clinic", DepartmentType.office)));
        when(googleAIGateway.generateContent(eq("gemini-test"), any(Content.class),
                any(GenerateContentConfig.class))).thenReturn("""
                {
                  "country": "Kenya",
                  "title": "Medicine unavailable",
                  "message": "The caller reports that medicine is unavailable at the local clinic.",
                  "category": "service_request",
                  "urgency": "high",
                  "location": {"county": "Nairobi", "constituency": null, "ward": null, "addressText": null},
                  "classification": {
                    "departmentId": "health",
                    "officeId": "clinic",
                    "confidence": 0.91,
                    "reason": "The report concerns access to medicine."
                  }
                }
                """);

        final var result = googleAIService.summarize(AUDIO_URL);

        assertThat(result.getCountry()).isEqualTo("Kenya");
        assertThat(result.getCategory()).isEqualTo(MessageCategory.service_request);
        assertThat(result.getUrgency()).isEqualTo(MessageUrgency.high);
        assertThat(result.getStatus()).isEqualTo(MessageStatus.new_message);
        assertThat(result.getLocation().getCounty()).isEqualTo("Nairobi");
        assertThat(result.getClassification().getDepartmentId()).isEqualTo("health");
        assertThat(result.getClassification().getOfficeId()).isEqualTo("clinic");
        assertThat(result.getAttachments()).singleElement().satisfies(attachment -> {
            assertThat(attachment.getUrl()).isEqualTo(AUDIO_URL);
            assertThat(attachment.getId()).isNull();
            assertThat(attachment.getFileName()).isNull();
            assertThat(attachment.getFileType()).isNull();
        });

        final ArgumentCaptor<Content> content = ArgumentCaptor.forClass(Content.class);
        verify(googleAIGateway).generateContent(eq("gemini-test"), content.capture(),
                any(GenerateContentConfig.class));
        assertThat(content.getValue().toString())
                .contains("health", "Ministry of Health", "clinic", "Central Clinic");
    }

    @Test
    void summarizeRejectsUnknownDepartment() {
        when(departmentService.findAll()).thenReturn(List.of(
                department("health", "Ministry of Health", DepartmentType.department)));
        when(googleAIGateway.generateContent(eq("gemini-test"), any(Content.class),
                any(GenerateContentConfig.class))).thenReturn("""
                {
                  "country": "Kenya",
                  "title": "Report",
                  "message": "A report",
                  "category": "general_support",
                  "urgency": "low",
                  "classification": {
                    "departmentId": "unknown",
                    "confidence": 0.5,
                    "reason": "General inquiry"
                  }
                }
                """);

        assertThatThrownBy(() -> googleAIService.summarize(AUDIO_URL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Google AI selected an unknown department");
    }

    @Test
    void summarizeAcceptsSeededOtherDepartment() {
        when(departmentService.findAll()).thenReturn(List.of(
                department("OTHER", "OTHER", DepartmentType.department)));
        when(googleAIGateway.generateContent(eq("gemini-test"), any(Content.class),
                any(GenerateContentConfig.class))).thenReturn("""
                {
                  "country": "Kenya",
                  "title": "Unclassified report",
                  "message": "The recording does not identify a specific public service.",
                  "category": "general_support",
                  "urgency": "low",
                  "classification": {
                    "departmentId": "OTHER",
                    "confidence": 0.4,
                    "reason": "No specific department clearly matches."
                  }
                }
                """);

        final var result = googleAIService.summarize(AUDIO_URL);

        assertThat(result.getClassification().getDepartmentId()).isEqualTo("OTHER");
        final ArgumentCaptor<Content> content = ArgumentCaptor.forClass(Content.class);
        verify(googleAIGateway).generateContent(eq("gemini-test"), content.capture(),
                any(GenerateContentConfig.class));
        assertThat(content.getValue().toString())
                .contains("Select departmentId OTHER", "\"id\":\"OTHER\"");
    }

    private DepartmentDTO department(final String id, final String name, final DepartmentType type) {
        final DepartmentDTO department = new DepartmentDTO();
        department.setId(id);
        department.setCountry("Kenya");
        department.setName(name);
        department.setType(type);
        department.setDescription("Handles public health reports");
        department.setHandlesCategories(List.of("service_request"));
        department.setKeywords(List.of("health", "medicine"));
        return department;
    }

}