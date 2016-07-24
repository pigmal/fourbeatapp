package com.pigmal.android.hardware.fourbeat;

public class Protocol {
    public enum BUTTON_ID {RED, BLUE, YELLOW, GREEN}

    public enum BUTTON_STATE {OFF, ON}

    public static final int BUTTON_STATUS_CHANGE = 0x01;

    static final int COMMAND_APP_CONNECT = 0xFE;
    static final int COMMAND_UPDATE_BUTTONS = 0x01;
}
