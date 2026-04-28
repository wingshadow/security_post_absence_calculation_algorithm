package org.example.doubao2;

import java.time.LocalDateTime;

/**
 * @program: Test
 * @description:
 * @author: zhb
 * @create: 2026-04-28 14:44
 */
public class Interval {
    LocalDateTime start;
    LocalDateTime end;

    Interval(LocalDateTime s, LocalDateTime e) {
        this.start = s;
        this.end = e;
    }
}
