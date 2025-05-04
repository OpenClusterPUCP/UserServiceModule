package com.example.userservicemodule.DTO;

public class AvailabilityZoneDTO {
    private Integer id;
    private String name;
    private String description;

    private int totalVcpu;
    private int usedVcpu;
    private int totalRam;
    private int usedRam;
    private int totalDisk;
    private int usedDisk;
    private int serverCount;
    private int sliceCount;
    private int totalVMs;


    public AvailabilityZoneDTO(Integer id, String name, String description, int totalVcpu, int usedVcpu, int totalRam, int usedRam, int totalDisk, int usedDisk, int serverCount, int sliceCount, int totalVMs) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalVcpu = totalVcpu;
        this.usedVcpu = usedVcpu;
        this.totalRam = totalRam;
        this.usedRam = usedRam;
        this.totalDisk = totalDisk;
        this.usedDisk = usedDisk;
        this.serverCount = serverCount;
        this.sliceCount = sliceCount;
        this.totalVMs = totalVMs;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public int getTotalVcpu() {
        return totalVcpu;
    }

    public void setTotalVcpu(int totalVcpu) {
        this.totalVcpu = totalVcpu;
    }

    public int getUsedVcpu() {
        return usedVcpu;
    }

    public void setUsedVcpu(int usedVcpu) {
        this.usedVcpu = usedVcpu;
    }

    public int getTotalRam() {
        return totalRam;
    }

    public void setTotalRam(int totalRam) {
        this.totalRam = totalRam;
    }

    public int getUsedRam() {
        return usedRam;
    }

    public void setUsedRam(int usedRam) {
        this.usedRam = usedRam;
    }

    public int getTotalDisk() {
        return totalDisk;
    }

    public void setTotalDisk(int totalDisk) {
        this.totalDisk = totalDisk;
    }

    public int getUsedDisk() {
        return usedDisk;
    }

    public void setUsedDisk(int usedDisk) {
        this.usedDisk = usedDisk;
    }

    public int getServerCount() {
        return serverCount;
    }

    public void setServerCount(int serverCount) {
        this.serverCount = serverCount;
    }

    public int getSliceCount() {
        return sliceCount;
    }

    public void setSliceCount(int sliceCount) {
        this.sliceCount = sliceCount;
    }

    public int getTotalVMs() {
        return totalVMs;
    }

    public void setTotalVMs(int totalVMs) {
        this.totalVMs = totalVMs;
    }
}

