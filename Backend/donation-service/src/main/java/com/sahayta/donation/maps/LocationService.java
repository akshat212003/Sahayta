package com.sahayta.donation.maps;

import java.util.Arrays;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.sahayta.donation.dto.NominatimResponse;

@Service
public class LocationService {

    private final RestClient restClient = RestClient.builder()
            .defaultHeader("User-Agent", "Sahayta/1.0")
            .build();

    public double[] getCoordinates(String address) {

        NominatimResponse[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("nominatim.openstreetmap.org")
                        .path("/search")
                        .queryParam("q", address)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(NominatimResponse[].class);

        if (response != null && response.length > 0) {

            return new double[] {
                    Double.parseDouble(response[0].getLat()),
                    Double.parseDouble(response[0].getLon())
            };
        }

        return null;
    }
}