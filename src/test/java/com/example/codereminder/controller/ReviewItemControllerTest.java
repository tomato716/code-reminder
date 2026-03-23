package com.example.codereminder.controller;

import com.example.codereminder.dto.ReviewItemDto;
import com.example.codereminder.service.ReviewItemService;
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

@WebMvcTest(ReviewItemController.class)
class ReviewItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewItemService reviewItemService;

    @Test
    @DisplayName("userName, problemId, resultText, timestamp 값이 모두 있으면 상태 200을 반환한다.")
    void saveSubmission_Success() throws Exception {
        //given
        ReviewItemDto reviewItemDto = new ReviewItemDto(
                "park",
                2000L,
                "틀렸습니다",
                System.currentTimeMillis());

        String json = objectMapper.writeValueAsString(reviewItemDto);

        //when
        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));

        //then
        then(reviewItemService).should().save(any(ReviewItemDto.class));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDto")
    @DisplayName("userName, problemId, resultText, timestamp 중 하나라도 없으면 상태 400을 반환한다.")
    void saveSubmission_Failure(ReviewItemDto reviewItemDto) throws Exception {
        //given
        String json = objectMapper.writeValueAsString(reviewItemDto);

        //when
        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        //then
        then(reviewItemService).should(never()).save(any(ReviewItemDto.class));
    }

    private static Stream<Arguments> provideInvalidDto() {
        return Stream.of(
                Arguments.of(new ReviewItemDto("", 2000L, "틀렸습니다", System.currentTimeMillis())),
                Arguments.of(new ReviewItemDto("park", null, "틀렸습니다", System.currentTimeMillis())),
                Arguments.of(new ReviewItemDto("park", 2000L, "", System.currentTimeMillis())),
                Arguments.of(new ReviewItemDto("park", 2000L, "틀렸습니다", null))
        );
    }
}