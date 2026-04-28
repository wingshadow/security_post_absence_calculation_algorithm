package org.example.doubao2;

import org.example.ClassGroupDetailRespDTO;
import org.example.IotDutyIntervalDO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Test12Scenes2 {

    public static void main(String[] args) {
        List<ClassGroupDetailRespDTO> shifts = buildShiftTemplates();
        LocalDate testDate = LocalDate.of(2026, 4, 25);

//        System.out.println("===== 场景1：白班早退，中班正常，夜班未结束 =====");
//        scene1(shifts, testDate);
//
//        System.out.println("\n===== 场景2：仅白班迟到，中班夜班未开始 =====");
//        scene2(shifts, testDate);
//
//        System.out.println("\n===== 场景3：全天无任何打卡记录 =====");
//        scene3(shifts, testDate);

        System.out.println("\n===== 场景4：跨天漏打卡（4.25 18:00 → 4.27 08:00 断档）=====");
        scene4(shifts, testDate);

//        System.out.println("\n===== 场景5：中班未打卡，夜班正常 =====");
//        scene5(shifts, testDate);
//
//        System.out.println("\n===== 场景6：全勤（无任何迟到早退）=====");
//        scene6(shifts, testDate);
    }

    // 场景1：白班早退，中班正常，夜班未结束
    private static void scene1(List<ClassGroupDetailRespDTO> shifts, LocalDate date) {
        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 0));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 17, 0));

        IotDutyIntervalDO d2 = new IotDutyIntervalDO();
        d2.setStartTime(LocalDateTime.of(2026, 4, 25, 18, 0));
        d2.setEndTime(LocalDateTime.of(2026, 4, 26, 2, 0));

        List<AbsenceDetail> result = TimeEngine.calc2(shifts, Arrays.asList(d1, d2));
        result.forEach(r -> {
            System.out.println(
                    r.date + " | 班次" + r.shiftId +
                            " | " + r.shiftStart + "-" + r.shiftEnd +
                            " | 缺勤 " + r.absentStart + "-" + r.absentEnd +
                            " | " + r.absentMinutes + "min"
            );
        });
    }

    // 场景2：仅白班迟到
    private static void scene2(List<ClassGroupDetailRespDTO> shifts, LocalDate date) {
        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 30));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 18, 0));

        List<AbsenceDetail> result =  TimeEngine.calc2(shifts, Arrays.asList(d1));
        result.forEach(r -> {
            System.out.println(
                    r.date + " | 班次" + r.shiftId +
                            " | " + r.shiftStart + "-" + r.shiftEnd +
                            " | 缺勤 " + r.absentStart + "-" + r.absentEnd +
                            " | " + r.absentMinutes + "min"
            );
        });
    }

    // 场景3：无打卡
    private static void scene3(List<ClassGroupDetailRespDTO> shifts, LocalDate date) {
        List<AbsenceDetail> result = TimeEngine.calc2(shifts, Arrays.asList());
        result.forEach(r -> {
            System.out.println(
                    r.date + " | 班次" + r.shiftId +
                            " | " + r.shiftStart + "-" + r.shiftEnd +
                            " | 缺勤 " + r.absentStart + "-" + r.absentEnd +
                            " | " + r.absentMinutes + "min"
            );
        });
    }

    // 场景4：跨天断档 4.25 -> 4.27 漏打卡
    private static void scene4(List<ClassGroupDetailRespDTO> shifts, LocalDate date) {
        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 0));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 18, 0));

        IotDutyIntervalDO d2 = new IotDutyIntervalDO();
        d2.setStartTime(LocalDateTime.of(2026, 4, 27, 8, 0));
        d2.setEndTime(LocalDateTime.of(2026, 4, 27, 18, 0));

        List<AbsenceDetail> result = TimeEngine.calc2(shifts, Arrays.asList(d1, d2));
        result.forEach(r -> {
            System.out.println(
                    r.date + " | 班次" + r.shiftId +
                            " | " + r.shiftStart + "-" + r.shiftEnd +
                            " | 缺勤 " + r.absentStart + "-" + r.absentEnd +
                            " | " + r.absentMinutes + "min"
            );
        });
    }

    // 场景5：中班漏打卡，夜班正常
    private static void scene5(List<ClassGroupDetailRespDTO> shifts, LocalDate date) {
        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 0));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 18, 0));

        IotDutyIntervalDO d2 = new IotDutyIntervalDO();
        d2.setStartTime(LocalDateTime.of(2026, 4, 26, 2, 0));
        d2.setEndTime(LocalDateTime.of(2026, 4, 26, 8, 0));

        List<AbsenceDetail> result = TimeEngine.calc2(shifts, Arrays.asList(d1, d2));
        result.forEach(r -> {
            System.out.println(
                    r.date + " | 班次" + r.shiftId +
                            " | " + r.shiftStart + "-" + r.shiftEnd +
                            " | 缺勤 " + r.absentStart + "-" + r.absentEnd +
                            " | " + r.absentMinutes + "min"
            );
        });
    }

    // 场景6：全勤
    private static void scene6(List<ClassGroupDetailRespDTO> shifts, LocalDate date) {
        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 0));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 18, 0));

        IotDutyIntervalDO d2 = new IotDutyIntervalDO();
        d2.setStartTime(LocalDateTime.of(2026, 4, 25, 18, 0));
        d2.setEndTime(LocalDateTime.of(2026, 4, 26, 2, 0));

        IotDutyIntervalDO d3 = new IotDutyIntervalDO();
        d3.setStartTime(LocalDateTime.of(2026, 4, 26, 2, 0));
        d3.setEndTime(LocalDateTime.of(2026, 4, 26, 8, 0));

        List<AbsenceDetail> result = TimeEngine.calc2(shifts, Arrays.asList(d1, d2, d3));
        result.forEach(r -> {
            System.out.println(
                    r.date + " | 班次" + r.shiftId +
                            " | " + r.shiftStart + "-" + r.shiftEnd +
                            " | 缺勤 " + r.absentStart + "-" + r.absentEnd +
                            " | " + r.absentMinutes + "min"
            );
        });
    }

    // 统一打印结果
    private static void printResult(Map<String, Long> map) {
        if (map.isEmpty()) {
            System.out.println("无缺勤记录");
            return;
        }
        map.forEach((key, val) ->
                System.out.printf("%s → 缺勤 %d 分钟%n", key, val)
        );
    }

    // 班次模板：白、中、夜
    private static List<ClassGroupDetailRespDTO> buildShiftTemplates() {
        List<ClassGroupDetailRespDTO> list = new ArrayList<>();

        ClassGroupDetailRespDTO day = new ClassGroupDetailRespDTO();
        day.setId(1L);
        day.setClassStartTime("08:00");
        day.setClassEndTime("18:00");

        ClassGroupDetailRespDTO middle = new ClassGroupDetailRespDTO();
        middle.setId(2L);
        middle.setClassStartTime("18:00");
        middle.setClassEndTime("02:00");

        ClassGroupDetailRespDTO night = new ClassGroupDetailRespDTO();
        night.setId(3L);
        night.setClassStartTime("02:00");
        night.setClassEndTime("08:00");

        list.add(day);
        list.add(middle);
        list.add(night);
        return list;
    }
}