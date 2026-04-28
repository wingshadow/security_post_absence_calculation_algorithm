package org.example.doubao2;

import org.example.ClassGroupDetailRespDTO;
import org.example.IotDutyIntervalDO;

import java.time.*;
import java.util.*;

public class TimeEngineV2 {


    public static Map<String, Long> calc(
            List<ClassGroupDetailRespDTO> templates,
            List<IotDutyIntervalDO> duties) {

        Map<String, Long> result = new LinkedHashMap<>();
        if (duties == null || duties.isEmpty()) return result;

        duties.sort(Comparator.comparing(IotDutyIntervalDO::getStartTime));

        LocalDateTime min = duties.get(0).getStartTime();
        LocalDateTime max = duties.stream()
                .map(IotDutyIntervalDO::getEndTime)
                .max(LocalDateTime::compareTo)
                .get();

        // ✅ 生成班次（已修复幽灵班次）
        List<Shift> shifts = buildShifts(templates, min, max);

        // ✅ 按班次计算
        for (Shift s : shifts) {
            // ❗未开始班次直接跳过
            if (!s.start.isBefore(max)) {
                continue;
            }

            long covered = 0;

            for (IotDutyIntervalDO d : duties) {
                covered += overlap(s.start, s.end,
                        d.getStartTime(), d.getEndTime());
            }

            long total = Duration.between(s.start, s.end).toMinutes();
            long absent = Math.max(0, total - covered);

            // 只输出有意义的
            if (covered > 0 || absent > 0) {
                result.put(format(s, covered, absent), absent);
            }
        }

        return result;
    }

    // ================= 核心：班次生成 =================
    private static List<Shift> buildShifts(
            List<ClassGroupDetailRespDTO> templates,
            LocalDateTime min,
            LocalDateTime max) {

        List<Shift> result = new ArrayList<>();

        LocalDateTime cursor = min;

        int idx = 0;

        while (cursor.isBefore(max)) {

            ClassGroupDetailRespDTO t = templates.get(idx % templates.size());

            LocalTime st = LocalTime.parse(t.getClassStartTime());
            LocalTime et = LocalTime.parse(t.getClassEndTime());

            LocalDate baseDate = cursor.toLocalDate();

            LocalDateTime start = baseDate.atTime(st);

            LocalDateTime end = et.isBefore(st)
                    ? start.plusDays(1).withHour(et.getHour()).withMinute(et.getMinute())
                    : baseDate.atTime(et);

            // ⭐关键：时间推进
            cursor = end;

            result.add(new Shift(baseDate, t, start, end));

            idx++;
        }

        return result;
    }

    // ================= overlap =================
    private static long overlap(LocalDateTime s1, LocalDateTime e1,
                                LocalDateTime s2, LocalDateTime e2) {

        LocalDateTime s = s1.isAfter(s2) ? s1 : s2;
        LocalDateTime e = e1.isBefore(e2) ? e1 : e2;

        return s.isBefore(e) ? Duration.between(s, e).toMinutes() : 0;
    }

    private static String format(Shift s, long covered, long absent) {
        return String.format("%s | 班次%d | %s-%s | 覆盖=%d | 缺勤=%d",
                s.date,
                s.template.getId(),
                s.template.getClassStartTime(),
                s.template.getClassEndTime(),
                covered,
                absent);
    }

    // ================= 内部类 =================
    static class Shift {
        LocalDate date;
        ClassGroupDetailRespDTO template;
        LocalDateTime start;
        LocalDateTime end;

        Shift(LocalDate d, ClassGroupDetailRespDTO t,
              LocalDateTime s, LocalDateTime e) {
            this.date = d;
            this.template = t;
            this.start = s;
            this.end = e;
        }
    }


}