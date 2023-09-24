package com.oxahex.weatherdiaryapi.controller;

import com.oxahex.weatherdiaryapi.domain.Diary;
import com.oxahex.weatherdiaryapi.service.DiaryService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    /**
     * 새 일기 생성
     * @param date 일기 날짜(yyyy-MM-dd))
     * @param text 일기 내용
     */
    @ApiOperation(value = "일기 텍스트와 날씨를 이용해 DB에 일기 저장")
    @PostMapping("/create/diary")
    public void createDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "날짜 형식: yyyy-MM-dd", example = "2023-09-23")
            LocalDate date,

            @RequestBody
            @ApiParam(value = "작성할 일기 내용")
            String text
    ) {
        diaryService.createDiary(date, text);

    }

    /**
     * 특정 날짜의 모든 일기 리스트 조회
     * @param date 조회 날짜(yyyy-MM-dd)
     * @return 해당 날짜에 작성된 모든 일기
     */
    @ApiOperation(value = "선택한 날짜의 모든 일기 리스트 조회")
    @GetMapping("/read/diary")
    public List<Diary> readDiary(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @ApiParam(value = "날짜 형식: yyyy-MM-dd", example = "2023-09-23")
        LocalDate date
    ) {
        return diaryService.readDiary(date);
    }

    /**
     * 특정 기간에 작성된 모든 일기 리스트 조회
     * @param startDate 조회 시작 날짜(yyyy-MM-dd)
     * @param endDate 조회 종료 날짜(yyyy-MM-dd)
     * @return 해당 날짜 사이에 작성된 모든 일기
     */
    @ApiOperation(value = "특정 기간에 작성된 모든 일기 리스트 조회")
    @GetMapping("/read/diaries")
    public List<Diary> readDiaries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회 시작 날짜: yyyy-MM-dd", example = "2023-09-23")
            LocalDate startDate,

            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "조회 종료 날짜: yyyy-MM-dd", example = "2023-09-24")
            LocalDate endDate
    ) {
        return diaryService.readDiaries(startDate, endDate);
    }

    /**
     * 특정 날짜의 첫 번째 일기 수정
     * @param date 수정할 일기의 날짜
     * @param text 수정할 일기 내용
     */
    @ApiOperation(value = "특정 날짜의 첫 번째 일기 수정")
    @PutMapping("/update/diary")
    public void updateDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "날짜 형식: yyyy-MM-dd", example = "2023-09-23")
            LocalDate date,

            @RequestBody
            @ApiParam(value = "수정할 일기 내용")
            String text
    ) {
        diaryService.updateDiary(date, text);
    }

    /**
     * 특정 날짜의 모든 일기 삭제
     * @param date 삭제할 날짜
     */
    @ApiOperation(value = "특정 날짜의 모든 일기 삭제")
    @DeleteMapping("/delete/diary")
    public void deleteDiary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @ApiParam(value = "날짜 형식: yyyy-MM-dd", example = "2023-09-23")
            LocalDate date
    ) {
        diaryService.deleteDiary(date);
    }
}