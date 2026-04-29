package org.example.doubao2;

import org.example.ClassGroupDetailRespDTO;
import org.example.IotDutyIntervalDO;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class TimeEngine {

    // =========================================================
    // 对外接口
    // =========================================================

    public static List<AbsenceDetail> calc2(
            List<ClassGroupDetailRespDTO> templates,
            List<IotDutyIntervalDO> duties) {

        List<AbsenceDetail> result = new ArrayList<>();
        if (duties == null || duties.isEmpty()) {
            return result;
        }

        duties.sort(Comparator.comparing(IotDutyIntervalDO::getStartTime));

        LocalDateTime dutyMin = duties.get(0).getStartTime();
        LocalDateTime dutyMax = duties.stream()
                .map(IotDutyIntervalDO::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElseThrow(() -> new RuntimeException("duties endTime 为空"));

        List<Shift> shifts = buildShifts(templates, dutyMin.toLocalDate(), dutyMax);

        for (Shift s : shifts) {
            if (!s.start.isBefore(dutyMax)) {
                break;
            }
            processShift(s, duties, result);
        }

        return result;
    }

    public static Map<String, Long> calc(
            List<ClassGroupDetailRespDTO> templates,
            List<IotDutyIntervalDO> duties) {

        Map<String, Long> result = new LinkedHashMap<>();
        calc2(templates, duties).stream()
                .filter(d -> d.absentMinutes > 0)
                .forEach(d -> result.merge(formatKey(d), d.absentMinutes, Long::sum));
        return result;
    }

    // =========================================================
    // 核心：单班次缺勤扫描
    // =========================================================

    private static void processShift(
            Shift s,
            List<IotDutyIntervalDO> duties,
            List<AbsenceDetail> result) {

        List<Interval> covers = duties.stream()
                .map(d -> intersect(s.start, s.end, d.getStartTime(), d.getEndTime()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(i -> i.start))
                .collect(Collectors.toList());

        List<Interval> merged = merge(covers);

        LocalDateTime cursor = s.start;
        boolean hasAbsence = false;

        for (Interval c : merged) {
            if (cursor.isBefore(c.start)) {
                addAbsence(result, s, cursor, c.start, merged);
                hasAbsence = true;
            }
            if (cursor.isBefore(c.end)) {
                cursor = c.end;
            }
        }

        if (cursor.isBefore(s.end)) {
            addAbsence(result, s, cursor, s.end, merged);
            hasAbsence = true;
        }

        if (!hasAbsence) {
            AbsenceDetail d = new AbsenceDetail();
            d.date          = s.date;
            d.shiftId       = s.template.getId();
            d.shiftStart    = s.start;
            d.shiftEnd      = s.end;
            d.absentStart   = null;
            d.absentEnd     = null;
            d.absentMinutes = 0;
            d.openTime      = s.start;
            d.openEnd       = s.end;
            result.add(d);
        }
    }

    // =========================================================
    // openTime/openEnd 填充
    // =========================================================

    private static void addAbsence(
            List<AbsenceDetail> result,
            Shift s,
            LocalDateTime absentStart,
            LocalDateTime absentEnd,
            List<Interval> merged) {

        long minutes = Duration.between(absentStart, absentEnd).toMinutes();
        if (minutes <= 0) {
            return;
        }

        AbsenceDetail d = new AbsenceDetail();
        d.date          = s.date;
        d.shiftId       = s.template.getId();
        d.shiftStart    = s.start;
        d.shiftEnd      = s.end;
        d.absentStart   = absentStart;
        d.absentEnd     = absentEnd;
        d.absentMinutes = minutes;

        d.openTime = merged.stream()
                .filter(i -> !i.end.isAfter(absentStart))
                .max(Comparator.comparing(i -> i.end))
                .map(i -> i.end)
                .orElse(s.start);

        d.openEnd = merged.stream()
                .filter(i -> !i.start.isBefore(absentEnd))
                .min(Comparator.comparing(i -> i.start))
                .map(i -> i.start)
                .orElse(s.end);

        result.add(d);
    }

    // =========================================================
    // buildShifts
    // =========================================================

    private static List<Shift> buildShifts(
            List<ClassGroupDetailRespDTO> templates,
            LocalDate startDate,
            LocalDateTime max) {

        List<Shift> result = new ArrayList<>();
        LocalDate curDate = startDate;
        int idx = 0;

        while (true) {
            ClassGroupDetailRespDTO t = templates.get(idx % templates.size());
            LocalTime st = LocalTime.parse(t.getClassStartTime());
            LocalTime et = LocalTime.parse(t.getClassEndTime());

            LocalDateTime shiftStart = curDate.atTime(st);
            LocalDateTime shiftEnd;

            if (et.isBefore(st) || et.equals(st)) {
                shiftEnd = shiftStart.plusDays(1)
                        .withHour(et.getHour()).withMinute(et.getMinute()).withSecond(0);
            } else {
                shiftEnd = curDate.atTime(et);
            }

            if (!shiftStart.isBefore(max)) {
                break;
            }

            result.add(new Shift(curDate, t, shiftStart, shiftEnd));

            curDate = shiftEnd.toLocalDate();
            idx++;
        }

        return result;
    }

    // =========================================================
    // 工具方法
    // =========================================================

    private static Interval intersect(
            LocalDateTime s1, LocalDateTime e1,
            LocalDateTime s2, LocalDateTime e2) {
        LocalDateTime s = s1.isAfter(s2) ? s1 : s2;
        LocalDateTime e = e1.isBefore(e2) ? e1 : e2;
        return s.isBefore(e) ? new Interval(s, e) : null;
    }

    private static List<Interval> merge(List<Interval> sorted) {
        List<Interval> merged = new ArrayList<>();
        for (Interval cur : sorted) {
            if (merged.isEmpty() || !merged.get(merged.size() - 1).end.isAfter(cur.start)) {
                merged.add(new Interval(cur.start, cur.end));
            } else {
                Interval last = merged.get(merged.size() - 1);
                if (cur.end.isAfter(last.end)) {
                    last.end = cur.end;
                }
            }
        }
        return merged;
    }

    private static String formatKey(AbsenceDetail d) {
        return String.format("%s|%d|%s-%s",
                d.date, d.shiftId,
                d.shiftStart.toLocalTime(),
                d.shiftEnd.toLocalTime());
    }

    // =========================================================
    // 内部结构（仅 Shift 和 Interval 保留，AbsenceDetail 已移出）
    // =========================================================

    static class Shift {
        LocalDate date;
        ClassGroupDetailRespDTO template;
        LocalDateTime start, end;

        Shift(LocalDate d, ClassGroupDetailRespDTO t, LocalDateTime s, LocalDateTime e) {
            date = d; template = t; start = s; end = e;
        }
    }

    static class Interval {
        LocalDateTime start, end;
        Interval(LocalDateTime s, LocalDateTime e) { start = s; end = e; }
    }
}