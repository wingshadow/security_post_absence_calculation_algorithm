package org.example.doubao2;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @program: Test
 * @description:
 * @author: zhb
 * @create: 2026-04-28 14:42
 */
public class AbsenceDetail {
    LocalDate date;              // 日期
    Long shiftId;               // 班次ID
    LocalDateTime shiftStart;   // 班次开始
    LocalDateTime shiftEnd;     // 班次结束

    LocalDateTime absentStart;  // 缺勤开始
    LocalDateTime absentEnd;    // 缺勤结束

    long absentMinutes;         // 缺勤时长（分钟）
}
