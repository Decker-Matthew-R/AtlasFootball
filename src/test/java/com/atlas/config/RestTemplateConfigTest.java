package com.atlas.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigTest {

    private final RestTemplateConfig config = new RestTemplateConfig();

    @Test
    void restTemplate_ShouldCreateRestTemplateBean() {
        RestTemplate restTemplate = config.restTemplate();

        assertThat(restTemplate).isNotNull();
    }

    @Test
    void restTemplate_ShouldConfigureHttpComponentsClientHttpRequestFactory() {
        RestTemplate restTemplate = config.restTemplate();

        assertThat(restTemplate.getRequestFactory())
                .isInstanceOf(HttpComponentsClientHttpRequestFactory.class);
    }

    @Test
    void restTemplate_ShouldUseHttpComponentsFactory() {
        RestTemplate restTemplate = config.restTemplate();
        HttpComponentsClientHttpRequestFactory factory =
                (HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory();

        assertThat(factory).isNotNull();
    }

    @Test
    void restTemplate_ShouldCreateNewInstanceEachTime() {
        RestTemplate restTemplate1 = config.restTemplate();
        RestTemplate restTemplate2 = config.restTemplate();

        assertThat(restTemplate1).isNotSameAs(restTemplate2);
        assertThat(restTemplate1.getRequestFactory())
                .isInstanceOf(HttpComponentsClientHttpRequestFactory.class);
        assertThat(restTemplate2.getRequestFactory())
                .isInstanceOf(HttpComponentsClientHttpRequestFactory.class);
    }
}
