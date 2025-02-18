package com.drawathang.game_server.contract;

import java.util.List;

public class RequiredBroadcastInfo {
    public List<String> recipients;

    public RequiredBroadcastInfo(List<String> recipients) {
        this.recipients = recipients;
    }
}
