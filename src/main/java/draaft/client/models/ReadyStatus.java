package draaft.client.models;

public enum ReadyStatus {
    READY("Ready"),
    NOT_READY("Not Ready"),
    DRAAFTING("Draafting");

    private final String displayName;

    ReadyStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
