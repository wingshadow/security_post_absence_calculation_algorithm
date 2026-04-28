package org.example.doubao2;

import org.example.ClassGroupDetailRespDTO;
import org.example.IotDutyIntervalDO;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TimeEngine33 {

    public static Map<String, Long> calcWithDate(List<ClassGroupDetailRespDTO> templates, List<IotDutyIntervalDO> duties) {
        Map<String, Long> result = new LinkedHashMap<>();
        if (duties.isEmpty()) return result;

        // 1. 处理打卡对应的班次
        for (IotDutyIntervalDO duty : duties) {
            LocalDateTime dStart = duty.getStartTime();
            LocalDateTime dEnd   = duty.getEndTime();
            LocalDate date = dStart.toLocalDate();

            for (ClassGroupDetailRespDTO t : templates) {
                LocalTime st = LocalTime.parse(t.getClassStartTime());
                LocalTime et = LocalTime.parse(t.getClassEndTime());
                LocalDateTime shiftS = date.atTime(st);
                LocalDateTime shiftE = date.atTime(et);

                if (et.isBefore(st)) {
                    shiftE = shiftE.plusDays(1);
                }

                if (isInside(dStart, shiftS, shiftE)) {
                    long total   = Duration.between(shiftS, shiftE).toMinutes();
                    long overlap = calculateOverlap(shiftS, shiftE, dStart, dEnd);
                    long absence = Math.max(0, total - overlap);

                    result.put(date + " 班次" + t.getId(), absence);
                    break;
                }
            }
        }

        // 2. 处理断档（只算【完全落在断档内】的班次）
        for (int i = 1; i < duties.size(); i++) {
            LocalDateTime preEnd  = duties.get(i - 1).getEndTime();
            LocalDateTime currStart = duties.get(i).getStartTime();

            if (preEnd.isBefore(currStart)) {
                addOnlyFullyInGap(preEnd, currStart, templates, result);
            }
        }

        return result;
    }

    // 【关键修复】只添加【完全、整个班次都在断档里】的，不包含边界
    private static void addOnlyFullyInGap(LocalDateTime gapStart, LocalDateTime gapEnd,
                                          List<ClassGroupDetailRespDTO> templates, Map<String, Long> result) {
        LocalDate currDay = gapStart.toLocalDate();
        while (!currDay.isAfter(gapEnd.toLocalDate())) {
            for (ClassGroupDetailRespDTO t : templates) {
                LocalTime st = LocalTime.parse(t.getClassStartTime());
                LocalTime et = LocalTime.parse(t.getClassEndTime());
                LocalDateTime shiftS = currDay.atTime(st);
                LocalDateTime shiftE = currDay.atTime(et);

                if (et.isBefore(st)) {
                    shiftE = shiftE.plusDays(1);
                }

                // ✅ 【最严格】整个班次必须完全在断档内部，不碰两边
                if (shiftS.isAfter(gapStart) && shiftE.isBefore(gapEnd)) {
                    long total = Duration.between(shiftS, shiftE).toMinutes();
                    result.put(currDay + " 班次" + t.getId(), total);
                }
            }
            currDay = currDay.plusDays(1);
        }
    }

    private static boolean isInside(LocalDateTime time, LocalDateTime start, LocalDateTime end) {
        return (time.isAfter(start) || time.isEqual(start)) && time.isBefore(end);
    }

    private static long calculateOverlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        LocalDateTime maxStart = s1.isAfter(s2) ? s1 : s2;
        LocalDateTime minEnd   = e1.isBefore(e2) ? e1 : e2;
        return maxStart.isBefore(minEnd) ? Duration.between(maxStart, minEnd).toMinutes() : 0L;
    }
}