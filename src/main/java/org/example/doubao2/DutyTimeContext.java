package org.example.doubao2;

import org.example.IotDutyIntervalDO;

import java.time.LocalDateTime;
import java.util.List;

public class DutyTimeContext {

    private final LocalDateTime min;
    private final LocalDateTime max;

    public DutyTimeContext(List<IotDutyIntervalDO> duties) {

        this.min = duties.stream()
                .map(IotDutyIntervalDO::getStartTime)
                .min(LocalDateTime::compareTo)
                .get();

        this.max = duties.stream()
                .map(IotDutyIntervalDO::getEndTime)
                .max(LocalDateTime::compareTo)
                .get();
    }

    public LocalDateTime getMin() {
        return min;
    }

    public LocalDateTime getMax() {
        return max;
    }
}