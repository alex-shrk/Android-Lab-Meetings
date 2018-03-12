package ru.ssau.sanya.mettings.Entity;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Meeting {
    private String uid;
    private String name;
    private String description;
    private String startTime;
    private String startDate;
    private String endDate;
    private String endTime;
    private HashMap<String,String> memberList = new HashMap<>();
    private String priority;

    public Meeting() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public HashMap<String, String> getMemberList() {
        return memberList;
    }

    public void setMemberList(HashMap<String, String> memberList) {
        this.memberList = memberList;
    }
    public List<String> getMembersUid(){
        return new ArrayList<>(memberList.values());
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }


}
