package ru.practicum.ewmservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.ewmservice.customException.ValidationInternalException;
import ru.practicum.ewmservice.model.dto.EndpointHit;
import ru.practicum.ewmservice.model.dto.ViewStats;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatsClient {
    private final WebClient webClient;

    public void sendHit(EndpointHit endpointHit) {
        String url = "/hit";
        ResponseEntity<Void> responseEntity = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(endpointHit), EndpointHit.class)
                .retrieve()
                .toBodilessEntity()
                .block();
        if (responseEntity == null)
            throw new ValidationInternalException(
                    "Error saving statistics. The server did not return a response.");
        else if (responseEntity.getStatusCode() != HttpStatus.OK)
            throw new ValidationInternalException(String.format(
                    "Error saving statistics. The server returned a response code=%s", responseEntity.getStatusCode()));
    }

    public Map<Long, Long> getStats(List<Long> eventIds) {
        return getStats(eventIds, true);
    }

    /**
     * Запрос количества просмотров событий к серверу статистики.
     *
     * @param eventIds Спикок ID событий.
     * @param unique   Флаг уникальных IP.
     * @return Мапа (id события, количество просмотров).
     */
    public Map<Long, Long> getStats(List<Long> eventIds, boolean unique) {
        if (eventIds != null && eventIds.size() == 0)   // Если запросный список пустой, то не нужно дергать базу.
            return new HashMap<>();                     // Это возможно при обработке подборок событий.
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append("/stats")
                // Значения начала и конца временного диапазона - обязательны по ТЗ.
                // Но в ТЗ не указано откуда берется их значения. Поэтому берем с запасом.
                // В файл параметров не выносятся, поскольку это просто заглушки,
                // необходимые из-за упрощения учебного ТЗ.
                .append("?start=")
                .append(URLEncoder.encode("2022-01-01 00:00:00", StandardCharsets.UTF_8))
                .append("&end=")
                .append(URLEncoder.encode("2100-01-01 00:00:00", StandardCharsets.UTF_8))
                .append("&unique=").append(unique);
        if (eventIds != null) {
            sbUrl.append("&uris=");
            Iterator<Long> id = eventIds.iterator();
            sbUrl.append("/events/")
                    .append(id.next());
            while (id.hasNext()) {
                sbUrl.append(",/events/")
                        .append(id.next());
            }
        }

        List<ViewStats> resp = webClient.get()
                .uri(sbUrl.toString())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ViewStats>>() {
                })
                .block();

        if (resp == null) {
            return new HashMap<>();
        } else {
            return resp.stream()
                    .collect(Collectors.toMap(
                            vs -> Long.parseLong(vs.getUri().split("/", 0)[2]),
                            vs -> vs.getHits()));
        }
    }
}
