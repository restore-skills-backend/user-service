package com.restore.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticeHours {

    private Long id;

    private DayOfWeek dayOfWeek;

    private String openingTime;

    private String closingTime;

}
