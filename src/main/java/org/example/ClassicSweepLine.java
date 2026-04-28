package org.example;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;

/**
 * 经典：时间扫描线 + 差分算法
 * 适用：考勤在岗、设备在线、班次重叠、多时间段时长合并统计
 */
public class ClassicSweepLine {

    // 时间点 + 差值
    static class TimePoint {
        LocalDateTime time;
        int diff;

        public TimePoint(LocalDateTime time, int diff) {
            this.time = time;
            this.diff = diff;
        }
    }

    /**
     * 计算：指定【目标时间区间】内，被【多个业务区间】覆盖的总分钟数
     * @param targetStart 目标开始（班次开始）
     * @param targetEnd   目标结束（班次结束）
     * @param ranges      业务时间段集合（打卡/在岗/设备在线区间）
     * @return 覆盖总分钟
     */
    public static long calcCoveredMinutes(LocalDateTime targetStart,
                                          LocalDateTime targetEnd,
                                          List<LocalDateTime[]> ranges) {
        List<TimePoint> pointList = new ArrayList<>();

        // 1. 差分打标：区间开始+1，区间结束-1
        for (LocalDateTime[] range : ranges) {
            LocalDateTime s = range[0];
            LocalDateTime e = range[1];
            pointList.add(new TimePoint(s, 1));
            pointList.add(new TimePoint(e, -1));
        }

        // 2. 时间升序排序（扫描线核心）
        pointList.sort(Comparator.comparing(p -> p.time));

        long covered = 0;
        // 当前重叠数量
        int currentCount = 0;
        LocalDateTime prevTime = null;

        // 3. 线性扫描
        for (TimePoint point : pointList) {
            LocalDateTime currTime = point.time;
            if (prevTime != null && prevTime.isBefore(currTime)) {
                // 片段在目标区间内 & 处于覆盖状态
                boolean inTarget = prevTime.isBefore(targetEnd) && currTime.isAfter(targetStart);
                if (inTarget && currentCount > 0) {
                    // 取交集边界
                    LocalDateTime segStart = prevTime.isAfter(targetStart) ? prevTime : targetStart;
                    LocalDateTime segEnd = currTime.isBefore(targetEnd) ? currTime : targetEnd;
                    if (segStart.isBefore(segEnd)) {
                        covered += Duration.between(segStart, segEnd).toMinutes();
                    }
                }
            }
            // 更新覆盖数
            currentCount += point.diff;
            prevTime = currTime;
        }
        return covered;
    }


    // ===================== 测试 Demo =====================
    public static void main(String[] args) {
        // 模拟一个班次 2026-04-25 18:00 ~ 次日02:00（跨天）
        LocalDateTime shiftS = LocalDateTime.of(2026,4,25,18,0);
        LocalDateTime shiftE = LocalDateTime.of(2026,4,26,2,0);

        // 模拟2条在岗打卡区间
        List<LocalDateTime[]> dutyRanges = new ArrayList<>();
//        dutyRanges.add(new LocalDateTime[]{
//                LocalDateTime.of(2026,4,25,8,0),
//                LocalDateTime.of(2026,4,25,17,0)
//        });
        dutyRanges.add(new LocalDateTime[]{
                LocalDateTime.of(2026,4,25,20,0),
                LocalDateTime.of(2026,4,26,3,0)
        });

        // 计算该班次覆盖时长
        long cover = calcCoveredMinutes(shiftS, shiftE, dutyRanges);
        long total = Duration.between(shiftS, shiftE).toMinutes();
        long absent = total - cover;

        System.out.println("班次总时长：" + total + " 分钟");
        System.out.println("在岗覆盖：" + cover + " 分钟");
        System.out.println("缺勤时长：" + absent + " 分钟");
    }
}