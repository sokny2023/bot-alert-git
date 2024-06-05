package git.treacking.push.gittrack;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class TelegramService {

    @Value("${telegram.bot.token1}")
    private String botToken1;

    @Value("${telegram.bot.id1}")
    private String chatId1;

    @Value("${telegram.bot.token2}")
    private String botToken2;

    @Value("${telegram.bot.id2}")
    private String chatId2;

    @Value("${telegram.bot.token3}")
    private String botToken3;

    @Value("${telegram.bot.id3}")
    private String chatId3;

    private final RestTemplate restTemplate;


    public void sendGitPushInfo(String projectName, String author, String message, String branch, String dateTime) {
        String botToken;
        String chatId = switch (projectName) {
            case "UI" -> {
                botToken = botToken1;
                yield chatId1;
            }
            case "ADMIN" -> {
                botToken = botToken2;
                yield chatId2;
            }
            case "API" -> {
                botToken = botToken3;
                yield chatId3;
            }
            default -> throw new IllegalArgumentException("Unknown project name: " + projectName);
        };


        // Format dateTime to dd/MM/yyyy HH:mm:ss
        String formattedDateTime = formatDateTime(dateTime);

        String telegramMessage = String.format("""
            🔔 NEW ACTION...!
            ___________________________
            
            📁 Project  : %s
            👨‍💼 Author   : %s
            📦 Message  : %s
            💡 Branch   : %s
            📋 DateTime : %s
            ___________________________
            """, projectName, author, message, branch, formattedDateTime);

        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
        System.out.println(url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestJson = String.format("{\"chat_id\":\"%s\",\"text\":\"%s\"}", chatId, telegramMessage);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to send message to Telegram");
        }
    }

    private String formatDateTime(String dateTime) {
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return offsetDateTime.format(formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Failed to parse date time: " + dateTime, e);
        }
    }
}
