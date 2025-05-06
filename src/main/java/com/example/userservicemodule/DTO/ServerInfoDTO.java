package com.example.userservicemodule.DTO;

public class ServerInfoDTO {
    private String ip;
    private String gatewayAccessIp;
    private Integer gatewayAccessPort;

    public ServerInfoDTO(String ip, String gatewayAccessIp, Integer gatewayAccessPort) {
        this.ip = ip;
        this.gatewayAccessIp = gatewayAccessIp;
        this.gatewayAccessPort = gatewayAccessPort;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getGatewayAccessIp() {
        return gatewayAccessIp;
    }

    public void setGatewayAccessIp(String gatewayAccessIp) {
        this.gatewayAccessIp = gatewayAccessIp;
    }

    public Integer getGatewayAccessPort() {
        return gatewayAccessPort;
    }

    public void setGatewayAccessPort(Integer gatewayAccessPort) {
        this.gatewayAccessPort = gatewayAccessPort;
    }
}
