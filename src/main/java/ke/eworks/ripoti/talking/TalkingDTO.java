package ke.eworks.ripoti.talking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TalkingDTO {
    private String isActive;
    private String sessionId;
    private String direction;
    private String callerNumber;
    private String destinationNumber;
    private String dtmfDigits;
    private String recordingUrl;
    private String durationInSeconds;
    private String currencyCode;
    private String amount;
    private String callSessionState;
    private String callerCountryCode;
    private String callStartTime;
}
