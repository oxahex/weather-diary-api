package com.oxahex.weatherdiaryapi.service;

import com.oxahex.weatherdiaryapi.WeatherDiaryApiApplication;
import com.oxahex.weatherdiaryapi.domain.DateWeather;
import com.oxahex.weatherdiaryapi.domain.Diary;
import com.oxahex.weatherdiaryapi.repository.DateWeatherRepository;
import com.oxahex.weatherdiaryapi.repository.DiaryRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryService {
    @Value("${openweathermap.key}")
    private String apiKey;

    private final DiaryRepository diaryRepository;
    private final DateWeatherRepository dateWeatherRepository;

    private static final Logger logger = LoggerFactory.getLogger(WeatherDiaryApiApplication.class);

    public DiaryService(
            DiaryRepository diaryRepository,
            DateWeatherRepository dateWeatherRepository
    ) {
        this.diaryRepository = diaryRepository;
        this.dateWeatherRepository = dateWeatherRepository;
    }

    /**
     * Cron Job
     * <p> 매일 새벽 01시에 하루 전 날의 날씨 데이터를 DB에 저장
     */
    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveWeatherDate() {
        dateWeatherRepository.save(getWeatherFromAPI());
        logger.info("날씨 데이터 가져옴");
    }

    /**
     * Open API 호출을 통해 날씨 정보를 가져옴
     * @return 오늘 날씨 데이터
     */
    private DateWeather getWeatherFromAPI() {
        // get open API weather data
        String weatherData = getWeatherString();

        // parsing JSON data
        Map<String, Object> parsedWeatherData = parseWeatherData(weatherData);

        // 날씨 데이터 객체로 변환해 반환
        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parsedWeatherData.get("weather").toString());
        dateWeather.setIcon(parsedWeatherData.get("icon").toString());
        dateWeather.setTemperature((Double) parsedWeatherData.get("temp"));

        return dateWeather;
    }

    /**
     * 날짜와 일기 내용, 해당 날짜의 날씨 정보를 DB에 저장
     * @param date 일기 날짜
     * @param text 일기 내용
     */
    @Transactional
    public void createDiary(LocalDate date, String text) {

        logger.info("started to create diary");

        // 날씨 데이터 DB에서 가져옴(없는 경우 API 호출)
        DateWeather todayWeather = getDateWeather(date);

        // 날씨 일기 저장
        Diary newDiary = new Diary();
        newDiary.setDateWeather(todayWeather);
        newDiary.setText(text);

        diaryRepository.save(newDiary);

        logger.info("end to create diary");
    }

    /**
     * DB에서 특정 날짜의 날씨 데이터를 가져와 반환
     * <p> 날씨 정보가 DB에 없으면 API 호출해 날씨 정보 반환
     * @param date 가져올 날씨 데이터의 날짜
     * @return 해당 날짜의 날씨 데이터
     */
    private DateWeather getDateWeather(LocalDate date) {
        // DB에서 특정 일 날씨 데이터를 가져옴
        List<DateWeather> dateWeatherListFromDB = dateWeatherRepository.findAllByDate(date);
        if (dateWeatherListFromDB.isEmpty()) {
            // 오늘 날짜만 가져올 수 있기 때문에, 데이터가 없는 경우 날씨 없이 일기를 쓰거나 하는 등 정책이 필요함.
            // 우선 새로 API 호출을 통해 오늘 날씨 정보를 가져 옴.
            return getWeatherFromAPI();
        } else {
            return dateWeatherListFromDB.get(0);
        }
    }

    /**
     * 특정 날짜의 모든 일기 데이터 가져옴
     * @param date 조회할 날짜
     * @return 해당 날짜의 모든 일기 리스트
     */
    @Transactional(readOnly = true)
    public List<Diary> readDiary(LocalDate date) {
        logger.debug("read diary");

        // 해당 날짜의 일기를 가져옴
        return diaryRepository.findAllByDate(date);
    }

    /**
     * startDate, endDate 사이에 작성된 모든 일기를 가져옴
     * @param startDate 조회 시작 날짜
     * @param endDate 조회 종료 날짜
     * @return 해당 날짜 범위 안의 모든 일기 데이터 리스트
     */
    @Transactional(readOnly = true)
    public List<Diary> readDiaries(LocalDate startDate, LocalDate endDate) {
        return diaryRepository.findAllByDateBetween(startDate, endDate);
    }

    /**
     * 특정 날짜에 작성된 일기 중 첫 번째 일기 내용 수정
     * @param date 수정할 일기 날짜
     * @param text 수정할 내용
     */
    @Transactional
    public void updateDiary(LocalDate date, String text) {
        // 기존 일기 데이터 가져옴
        Diary diary = diaryRepository.getFirstByDate(date);

        // 내용 수정
        diary.setText(text);

        // DB 업데이트
        diaryRepository.save(diary);
    }

    /**
     * 특정 날짜의 모든 일기 삭제
     * @param date 삭제할 일기 날짜
     */
    @Transactional
    public void deleteDiary(LocalDate date) {
        diaryRepository.deleteAllByDate(date);
    }

    /**
     * Open API JSON String 파싱
     * @param jsonString Open API JSON string
     * @return temp, main, icon 데이터를 Map 타입으로 반환
     */
    private Map<String, Object> parseWeatherData(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();
        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        JSONArray weatherDataArray = (JSONArray) jsonObject.get("weather");
        JSONObject weatherData = (JSONObject) weatherDataArray.get(0);
        resultMap.put("weather", weatherData.get("main"));
        resultMap.put("icon", weatherData.get("icon"));

        return resultMap;
    }

    /**
     * 날씨 Open API 데이터 HTTP 요청
     * @return 요청 결과 JSON String 반환
     */
    private String getWeatherString() {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=seoul&appid=" + this.apiKey;

        // API URL
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            // 응답 코드 확인
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            // 결과 값 저장
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}