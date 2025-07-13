package com.atlas.externalAPIs.apiFootball.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.atlas.config.jwt.JwtTokenProvider;
import com.atlas.externalAPIs.apiFootball.controller.model.FixtureDto;
import com.atlas.externalAPIs.apiFootball.controller.model.supportingTypes.FixtureResponseDto;
import com.atlas.externalAPIs.apiFootball.service.ApiFootballService;
import com.atlas.externalAPIs.apiFootball.service.FixtureMapperService;
import com.atlas.externalAPIs.apiFootball.service.model.ExceptionTypes.ApiFootballException;
import com.atlas.externalAPIs.apiFootball.service.model.response.FixtureResponse;
import com.atlas.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiFootballController.class)
@AutoConfigureMockMvc(addFilters = false)
class ApiFootballControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private ApiFootballService apiFootballService;

    @MockitoBean private FixtureMapperService fixtureMapperService;

    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @MockitoBean private UserService userService;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void getUpcomingFixtures_ShouldReturnSuccessResponse_WhenServiceReturnsData() throws Exception {
        FixtureResponse serviceResponse = new FixtureResponse();
        FixtureResponseDto expectedDto = createSuccessResponse();

        when(apiFootballService.getUpcomingFixtures()).thenReturn(serviceResponse);
        when(fixtureMapperService.mapToDto(serviceResponse)).thenReturn(expectedDto);

        mockMvc.perform(get("/api/fixtures/upcoming").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Fixtures retrieved successfully"))
                .andExpect(jsonPath("$.results").value(2))
                .andExpect(jsonPath("$.fixtures").isArray())
                .andExpect(jsonPath("$.fixtures.length()").value(2));

        verify(apiFootballService).getUpcomingFixtures();
        verify(fixtureMapperService).mapToDto(serviceResponse);
    }

    @Test
    void getUpcomingFixtures_ShouldReturnEmptyResponse_WhenNoFixturesFound() throws Exception {
        FixtureResponse serviceResponse = new FixtureResponse();
        FixtureResponseDto expectedDto = createEmptyResponse();

        when(apiFootballService.getUpcomingFixtures()).thenReturn(serviceResponse);
        when(fixtureMapperService.mapToDto(serviceResponse)).thenReturn(expectedDto);

        mockMvc.perform(get("/api/fixtures/upcoming").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("No fixtures found"))
                .andExpect(jsonPath("$.results").value(0))
                .andExpect(jsonPath("$.fixtures").isEmpty());

        verify(apiFootballService).getUpcomingFixtures();
        verify(fixtureMapperService).mapToDto(serviceResponse);
    }

    @Test
    void getUpcomingFixtures_ShouldReturnServiceUnavailable_WhenApiFootballExceptionThrown()
            throws Exception {
        when(apiFootballService.getUpcomingFixtures())
                .thenThrow(new ApiFootballException("External API error", new RuntimeException()));

        mockMvc.perform(get("/api/fixtures/upcoming").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("External service unavailable"))
                .andExpect(jsonPath("$.results").value(0))
                .andExpect(jsonPath("$.fixtures").isEmpty());

        verify(apiFootballService).getUpcomingFixtures();
        verifyNoInteractions(fixtureMapperService);
    }

    @Test
    void getUpcomingFixtures_ShouldReturnInternalServerError_WhenUnexpectedExceptionThrown()
            throws Exception {
        when(apiFootballService.getUpcomingFixtures())
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/fixtures/upcoming").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.results").value(0))
                .andExpect(jsonPath("$.fixtures").isEmpty());

        verify(apiFootballService).getUpcomingFixtures();
        verifyNoInteractions(fixtureMapperService);
    }

    @Test
    void getUpcomingFixtures_ShouldReturnInternalServerError_WhenMapperServiceExceptionThrown()
            throws Exception {
        FixtureResponse serviceResponse = new FixtureResponse();
        when(apiFootballService.getUpcomingFixtures()).thenReturn(serviceResponse);
        when(fixtureMapperService.mapToDto(serviceResponse))
                .thenThrow(new RuntimeException("Mapper error"));

        mockMvc.perform(get("/api/fixtures/upcoming").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Internal server error"))
                .andExpect(jsonPath("$.results").value(0))
                .andExpect(jsonPath("$.fixtures").isEmpty());

        verify(apiFootballService).getUpcomingFixtures();
        verify(fixtureMapperService).mapToDto(serviceResponse);
    }

    @Test
    void getUpcomingFixtures_ShouldHandleNullServiceResponse() throws Exception {
        when(apiFootballService.getUpcomingFixtures()).thenReturn(null);
        when(fixtureMapperService.mapToDto(null)).thenReturn(createEmptyResponse());

        mockMvc.perform(get("/api/fixtures/upcoming").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("No fixtures found"));

        verify(apiFootballService).getUpcomingFixtures();
        verify(fixtureMapperService).mapToDto(null);
    }

    private FixtureResponseDto createSuccessResponse() {
        FixtureResponseDto response = new FixtureResponseDto();
        response.setStatus("success");
        response.setMessage("Fixtures retrieved successfully");
        response.setResults(2);
        response.setFixtures(List.of(new FixtureDto(), new FixtureDto()));
        return response;
    }

    private FixtureResponseDto createEmptyResponse() {
        FixtureResponseDto response = new FixtureResponseDto();
        response.setStatus("success");
        response.setMessage("No fixtures found");
        response.setResults(0);
        response.setFixtures(Collections.emptyList());
        return response;
    }
}
