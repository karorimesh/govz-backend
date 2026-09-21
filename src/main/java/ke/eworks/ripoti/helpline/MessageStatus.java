package ke.eworks.ripoti.helpline;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MessageStatus {

    @JsonProperty("new")
    new_message,

    triaged,

    assigned,

    in_progress,

    resolved,

    closed

}