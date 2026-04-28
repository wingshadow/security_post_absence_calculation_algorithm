package org.example;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @program: Test
 * @description:
 * @author: zhb
 * @create: 2026-04-25 16:54
 */
@Data
public class IotDutyIntervalDO {

    private Long floorId;
    private Long shiftId;

    private LocalDate shiftDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
