package ke.eworks.ripoti.ai;

import ke.eworks.ripoti.helpline.MessageCategory;
import ke.eworks.ripoti.helpline.MessageUrgency;


record AudioHelplineMessage(
        String country,
        String title,
        String message,
        MessageCategory category,
        MessageUrgency urgency,
        AudioLocation location,
        AudioClassification classification) {

    record AudioLocation(String county, String constituency, String ward, String addressText) {
    }

    record AudioClassification(String departmentId, String officeId, Double confidence, String reason) {
    }

}