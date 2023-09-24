package com.oxahex.weatherdiaryapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "date_weather")
public class DateWeather {
    @Id
    private LocalDate date;

    private String weather;
    private String icon;
    private double temperature;
}