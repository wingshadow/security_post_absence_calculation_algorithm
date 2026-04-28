package org.example.doubao2;

import org.example.ClassGroupDetailRespDTO;
import org.example.IotDutyIntervalDO;
import java.time.*;
import java.util.*;

public class TimeEngine {

    public static Map<String, Long> calc(List<ClassGroupDetailRespDTO> templates, List<IotDutyIntervalDO> duties) {
        Map<String, Long> result = new LinkedHashMap<>();
        if (duties == null || duties.isEmpty()) return result;

        // 排序打卡
        duties.sort(Comparator.comparing(IotDutyIntervalDO::getStartTime));
        LocalDateTime min = duties.get(0).getStartTime();
        LocalDateTime max = duties.stream().map(IotDutyIntervalDO::getEndTime).max(LocalDateTime::compareTo).get();

        // 生成连续班次（白→中→夜→白→中→夜）
        List<Shift> shifts = buildShifts(templates, min, max);

        // 计算每个班次
        for (Shift s : shifts) {
            if (!s.start.isBefore(max)) continue;

            long covered = 0;
            for (IotDutyIntervalDO d : duties) {
                covered += overlap(s.start, s.end, d.getStartTime(), d.getEndTime());
            }

            long total = Duration.between(s.start, s.end).toMinutes();
            long absent = Math.max(0, total - covered);

            // ✅ 只保留【有缺勤】的班次（满足你最后要求）
            if (absent > 0) {
                result.put(format(s, covered, absent), absent);
            }
        }
        return result;
    }

    public static List<AbsenceDetail> calc2(
            List<ClassGroupDetailRespDTO> templates,
            List<IotDutyIntervalDO> duties) {

        List<AbsenceDetail> result = new ArrayList<>();
        if (duties == null || duties.isEmpty()) return result;

        duties.sort(Comparator.comparing(IotDutyIntervalDO::getStartTime));

        LocalDateTime min = duties.get(0).getStartTime();
        LocalDateTime max = duties.stream()
                .map(IotDutyIntervalDO::getEndTime)
                .max(LocalDateTime::compareTo)
                .get();

        List<Shift> shifts = buildShifts(templates, min, max);

        for (Shift s : shifts) {

            if (!s.start.isBefore(max)) continue;

            // 1. 收集覆盖区间
            List<Interval> covers = new ArrayList<>();

            for (IotDutyIntervalDO d : duties) {
                LocalDateTime cs = s.start.isAfter(d.getStartTime())
                        ? s.start
                        : d.getStartTime();

                LocalDateTime ce = s.end.isBefore(d.getEndTime())
                        ? s.end
                        : d.getEndTime();

                if (cs.isBefore(ce)) {
                    covers.add(new Interval(cs, ce));
                }
            }

            covers.sort(Comparator.comparing(i -> i.start));

            // 2. 扫描缺勤区间
            LocalDateTime cursor = s.start;

            for (Interval c : covers) {

                if (cursor.isBefore(c.start)) {

                    add(result, s, cursor, c.start);
                }

                if (cursor.isBefore(c.end)) {
                    cursor = c.end;
                }
            }

            // 3. 尾部缺勤
            if (cursor.isBefore(s.end)) {
                add(result, s, cursor, s.end);
            }
        }

        return result;
    }

    // ==================== 核心：自动连续排班（白→中→夜→白） ====================
    private static List<Shift> buildShifts(List<ClassGroupDetailRespDTO> templates, LocalDateTime min, LocalDateTime max) {
        List<Shift> result = new ArrayList<>();
        LocalDateTime cursor = min;
        int idx = 0;

        while (cursor.isBefore(max)) {
            ClassGroupDetailRespDTO t = templates.get(idx % templates.size());
            LocalTime st = LocalTime.parse(t.getClassStartTime());
            LocalTime et = LocalTime.parse(t.getClassEndTime());
            LocalDate baseDate = cursor.toLocalDate();
            LocalDateTime start = baseDate.atTime(st);
            LocalDateTime end;

            if (et.isBefore(st)) {
                end = start.plusDays(1).withHour(et.getHour()).withMinute(et.getMinute());
            } else {
                end = baseDate.atTime(et);
            }

            cursor = end;
            result.add(new Shift(baseDate, t, start, end));
            idx++;
        }
        return result;
    }

    // ==================== 区间重叠计算 ====================
    private static long overlap(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        LocalDateTime s = s1.isAfter(s2) ? s1 : s2;
        LocalDateTime e = e1.isBefore(e2) ? e1 : e2;
        return s.isBefore(e) ? Duration.between(s, e).toMinutes() : 0;
    }

    // ==================== 输出格式 ====================
    private static String format(Shift s, long covered, long absent) {
        return String.format("%s | %d | %s-%s | %d |%d",
                s.date,
                s.template.getId(),
                s.template.getClassStartTime(),
                s.template.getClassEndTime(),
                covered,
                absent);
    }

    // ==================== 内部班次结构 ====================
    static class Shift {
        LocalDate date;
        ClassGroupDetailRespDTO template;
        LocalDateTime start;
        LocalDateTime end;

        Shift(LocalDate d, ClassGroupDetailRespDTO t, LocalDateTime s, LocalDateTime e) {
            this.date = d;
            this.template = t;
            this.start = s;
            this.end = e;
        }
    }




    private static void add(
            List<AbsenceDetail> result,
            Shift s,
            LocalDateTime start,
            LocalDateTime end) {

        long minutes = Duration.between(start, end).toMinutes();

        if (minutes <= 0) return;

        AbsenceDetail d = new AbsenceDetail();
        d.date = s.date;
        d.shiftId = s.template.getId();
        d.shiftStart = s.start;
        d.shiftEnd = s.end;
        d.absentStart = start;
        d.absentEnd = end;
        d.absentMinutes = minutes;

        result.add(d);
    }
}