import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherService {
    private static final String API_KEY = System.getenv("YANDEX_WEATHER_API_KEY");
    private static final String BASE_URL = "https://api.weather.yandex.ru/v2/forecast";

    public static void main(String[] args) {
        double latitude = 55.75;
        double longitude = 37.62;
        int limit = 7;
        if (API_KEY.isEmpty()) {
            System.err.println("API-ключ не найден. Установите переменную окружения 'YANDEX_WEATHER_API_KEY'.");
            return;
        }
        try {
            String url = String.format("%s?lat=%s&lon=%s&limit=%d", BASE_URL, latitude, longitude, limit);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("X-Yandex-Weather-Key", API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("Полные данные о погоде:\n" + responseBody);

                int currentTemp = extractTemperature(responseBody);
                System.out.println("Текущая температура: " + currentTemp + "°C");

                double averageTemp = extractAverageTemperature(responseBody);
                System.out.printf("Средняя температура за указанный период: %.2f°C\n", averageTemp);
            } else {
                System.out.println("Ошибка при запросе данных: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int extractTemperature(String responseBody) {
        Pattern pattern = Pattern.compile("\"fact\"\\s*:\\s*\\{[^}]*\"temp\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(responseBody);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Не удалось найти температуру в ответе.");
    }

    private static double extractAverageTemperature(String responseBody) {
        Pattern pattern = Pattern.compile("\"temp_avg\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(responseBody);

        int sum = 0;
        int count = 0;

        while (matcher.find()) {
            sum += Integer.parseInt(matcher.group(1));
            count++;
        }

        if (count == 0) {
            throw new IllegalArgumentException("Не удалось найти данные для средней температуры.");
        }

        return (double) sum / count;
    }
}