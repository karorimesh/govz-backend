# Ripoti

This app was created with [Bootify.io](https://bootify.io/app/I0FLU99U6AFF) - tips on working with the code [can be found here](https://bootify.io/next-steps/).

## Development

Update your local database connection in `application.yml` or create your own `application-local.yml` file to override settings for development.

During development it is recommended to use the profile `local`. In IntelliJ `-Dspring.profiles.active=local` can be added in the VM options of the Run Configuration after enabling this property in "Modify options".

Lombok must be supported by your IDE. For IntelliJ install the Lombok plugin and enable annotation processing - [learn more](https://bootify.io/next-steps/spring-boot-with-lombok.html).

After starting the application it is accessible under `localhost:8080`.

## Google AI helpline messages

Set `GOOGLE_AI_API_KEY` to a Google AI Studio API key before starting the application. The default model is `gemini-2.5-flash`; override it with `GOOGLE_AI_MODEL` when needed.

Send an audio recording URL to generate and route a helpline message:

```shell
curl -X POST http://localhost:8080/api/ai/audio-summary \
	-H "Content-Type: application/json" \
	-d '{"audioUrl":"https://example.com/recording.mp3"}'
```

The endpoint uses the configured departments to classify the recording and returns a `HelplineMessage` directly. The AI generates the core message, location, urgency, category, and classification. The application sets the status and adds the recording URL as the sole attachment.

```json
{
	"country": "Kenya",
	"title": "Medicine unavailable",
	"message": "The caller reports that medicine is unavailable at the local clinic.",
	"category": "service_request",
	"urgency": "high",
	"status": "new",
	"location": {
		"county": "Nairobi"
	},
	"attachments": [
		{
			"url": "https://example.com/recording.mp3"
		}
	],
	"classification": {
		"departmentId": "health",
		"officeId": "clinic",
		"confidence": 0.91,
		"reason": "The report concerns access to medicine."
	}
}
```

Generate a response for a normal text prompt:

```shell
curl -X POST http://localhost:8080/api/ai/text \
	-H "Content-Type: application/json" \
	-d '{"prompt":"Explain how solar panels work."}'
```

The endpoint returns `{ "response": "..." }`.

## Recorded call processing

New call audits are stored with `processed=false`. Once per minute, the application inspects these audits and creates a helpline message for each callback containing a `recordingUrl`. Audits without a usable recording are marked processed and skipped.

AI or message-persistence failures leave the audit unprocessed so it is retried on the next run. This scheduler is designed for a single application instance and provides at-least-once processing.

At startup, the application creates the fixed `OTHER` department when it is missing. Gemini uses this department when a recording does not clearly match a more specific configured department.

## Build

The application can be built using the following command:

```
mvnw clean package
```

Start your application with the following command - here with the profile `production`:

```
java -Dspring.profiles.active=production -jar ./target/ripoti-0.0.1-SNAPSHOT.jar
```

If required, a Docker image can be created with the Spring Boot plugin. Add `SPRING_PROFILES_ACTIVE=production` as environment variable when running the container.

```
mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=ke.eworks/ripoti
```

## Further readings

* [Maven docs](https://maven.apache.org/guides/index.html)  
* [Spring Boot reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)  
* [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/jpa.html)
