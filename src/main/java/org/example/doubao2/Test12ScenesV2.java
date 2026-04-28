package org.example.doubao2;

import org.example.ClassGroupDetailRespDTO;
import org.example.IotDutyIntervalDO;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ===== V2 测试升级版（生产验证视角）=====
 * 重点：验证 shift，而不是 date
 */
public class Test12ScenesV2 {

    public static void main(String[] args) {

        List<ClassGroupDetailRespDTO> shifts = buildShiftTemplates();

        System.out.println("===== 场景1：白班早退，中班正常，夜班未结束 =====");
        scene1(shifts);

        System.out.println("\n===== 场景2：仅白班迟到 =====");
        scene2(shifts);

        System.out.println("\n===== 场景3：全天无打卡 =====");
        scene3(shifts);

        System.out.println("\n===== 场景4：跨天断档 =====");
        scene4(shifts);

        System.out.println("\n===== 场景5：中班漏打卡（关键） =====");
        scene5(shifts);

        System.out.println("\n===== 场景6：全勤 =====");
        scene6(shifts);
    }

    // ================= 场景1 =================
    private static void scene1(List<ClassGroupDetailRespDTO> shifts) {

        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 0));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 17, 0));

        IotDutyIntervalDO d2 = new IotDutyIntervalDO();
        d2.setStartTime(LocalDateTime.of(2026, 4, 25, 18, 0));
        d2.setEndTime(LocalDateTime.of(2026, 4, 26, 2, 0));


        print(TimeEngineV2.calc(shifts, Arrays.asList(d1, d2)));
    }

    // ================= 场景2 =================
    private static void scene2(List<ClassGroupDetailRespDTO> shifts) {

        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 30));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 18, 0));

        print(TimeEngineV2.calc(shifts, Arrays.asList(d1)));
    }

    // ================= 场景3 =================
    private static void scene3(List<ClassGroupDetailRespDTO> shifts) {

        print(TimeEngineV2.calc(shifts, Collections.emptyList()));
    }

    // ================= 场景4 =================
    private static void scene4(List<ClassGroupDetailRespDTO> shifts) {

        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 0));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 18, 0));

        IotDutyIntervalDO d2 = new IotDutyIntervalDO();
        d2.setStartTime(LocalDateTime.of(2026, 4, 27, 8, 0));
        d2.setEndTime(LocalDateTime.of(2026, 4, 27, 18, 0));

        print(TimeEngineV2.calc(shifts, Arrays.asList(d1, d2)));
    }

    // ================= 场景5（重点） =================
    private static void scene5(List<ClassGroupDetailRespDTO> shifts) {

        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 0));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 18, 0));

        IotDutyIntervalDO d2 = new IotDutyIntervalDO();
        d2.setStartTime(LocalDateTime.of(2026, 4, 26, 2, 0));
        d2.setEndTime(LocalDateTime.of(2026, 4, 26, 8, 0));

        print(TimeEngineV2.calc(shifts, Arrays.asList(d1, d2)));
    }

    // ================= 场景6 =================
    private static void scene6(List<ClassGroupDetailRespDTO> shifts) {

        IotDutyIntervalDO d1 = new IotDutyIntervalDO();
        d1.setStartTime(LocalDateTime.of(2026, 4, 25, 8, 0));
        d1.setEndTime(LocalDateTime.of(2026, 4, 25, 18, 0));

        IotDutyIntervalDO d2 = new IotDutyIntervalDO();
        d2.setStartTime(LocalDateTime.of(2026, 4, 25, 18, 0));
        d2.setEndTime(LocalDateTime.of(2026, 4, 26, 2, 0));

        IotDutyIntervalDO d3 = new IotDutyIntervalDO();
        d3.setStartTime(LocalDateTime.of(2026, 4, 26, 2, 0));
        d3.setEndTime(LocalDateTime.of(2026, 4, 26, 8, 0));

        print(TimeEngineV2.calc(shifts, Arrays.asList(d1, d2, d3)));
    }

    // ================= 打印 =================
    private static void print(Map<String, Long> map) {

        if (map.isEmpty()) {
            System.out.println("无缺勤记录");
            return;
        }

        map.forEach((k, v) ->
                System.out.println(k + " | " + v)
        );
    }

    // ================= 班次模板 =================
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