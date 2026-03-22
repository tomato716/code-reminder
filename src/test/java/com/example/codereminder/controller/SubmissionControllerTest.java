package com.example.codereminder.controller;

import com.example.codereminder.dto.SubmissionDto;
import com.example.codereminder.service.SubmissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubmissionController.class)
class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubmissionService submissionService;

    @Test
    @DisplayName("userName, problemId, resultText, timestamp 값이 모두 있으면 상태 200을 반환한다.")
    void saveSubmission_Success() throws Exception {
        //given
        SubmissionDto submissionDto = new SubmissionDto(
                "park",
                2000L,
                "틀렸습니다",
                System.currentTimeMillis());

        String json = objectMapper.writeValueAsString(submissionDto);

        //when
        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        //then
        then(submissionService).should().save(any(SubmissionDto.class));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDto")
    @DisplayName("userName, problemId, resultText, timestamp 중 하나라도 없으면 상태 400을 반환한다.")
    void saveSubmission_Failure(SubmissionDto submissionDto) throws Exception {
        //given
        String json = objectMapper.writeValueAsString(submissionDto);

        //when
        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        //then
        then(submissionService).should(never()).save(any(SubmissionDto.class));
    }

    private static Stream<Arguments> provideInvalidDto() {
        return Stream.of(
                Arguments.of(new SubmissionDto("", 2000L, "틀렸습니다", System.currentTimeMillis())),
                Arguments.of(new SubmissionDto("park", null, "틀렸습니다", System.currentTimeMillis())),
                Arguments.of(new SubmissionDto("park", 2000L, "", System.currentTimeMillis())),
                Arguments.of(new SubmissionDto("park", 2000L, "틀렸습니다", null))
        );
    }
}