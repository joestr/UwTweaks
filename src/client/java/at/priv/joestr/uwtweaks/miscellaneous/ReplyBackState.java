package at.priv.joestr.uwtweaks.miscellaneous;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReplyBackState {
    boolean enabled = false;
    boolean isChatOpened = false;
    String lastWhisperPartner = null;
    boolean receivedAnotherWhisperWhileDraftingResponse = false;

    private static ReplyBackState instance;

    private ReplyBackState() {
    }

    public static ReplyBackState getInstance() {
        if (instance == null) {
            instance = new ReplyBackState();
        }
        return instance;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isChatOpened() {
        return isChatOpened;
    }

    public void setChatOpened(boolean chatOpened) {
        isChatOpened = chatOpened;
    }

    public String getLastWhisperPartner() {
        return lastWhisperPartner;
    }

    public void setLastWhisperPartner(String lastWhisperPartner) {
        this.lastWhisperPartner = lastWhisperPartner;
    }

    public boolean isReceivedAnotherWhisperWhileDraftingResponse() {
        return receivedAnotherWhisperWhileDraftingResponse;
    }

    public void setReceivedAnotherWhisperWhileDraftingResponse(boolean receivedAnotherWhisperWhileDraftingResponse) {
        this.receivedAnotherWhisperWhileDraftingResponse = receivedAnotherWhisperWhileDraftingResponse;
    }
}
