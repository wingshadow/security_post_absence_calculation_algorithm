package org.example;

/**
 * @program: Test
 * @description:
 * @author: zhb
 * @create: 2026-04-25 16:51
 */

public class ClassGroupDetailRespDTO {
    private Long id;
    private String classStartTime;

    private String classEndTime;

    // 是否跨天（18:00-02:00 这种）
    private Boolean crossDay;

    // 归属规则：0=开始日，1=结束日
    private Integer belongType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassStartTime() {
        return classStartTime;
    }

    public void setClassStartTime(String classStartTime) {
        this.classStartTime = classStartTime;
    }

    public String getClassEndTime() {
        return classEndTime;
    }

    public void setClassEndTime(String classEndTime) {
        this.classEndTime = classEndTime;
    }

    public Boolean getCrossDay() {
        return crossDay;
    }

    public void setCrossDay(Boolean crossDay) {
        this.crossDay = crossDay;
    }

    public Integer getBelongType() {
        return belongType;
    }

    public void setBelongType(Integer belongType) {
        this.belongType = belongType;
    }
}
