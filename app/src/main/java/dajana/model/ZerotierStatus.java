package dajana.model;

import com.zerotier.libzt.ZeroTier;
public class ZerotierStatus {
    public enum RunningStatus {
        ONLINE,
        OFFLINE,
        NODEDOWN,
        ISREADY,
        DENIED

    }

    public RunningStatus runningStatus;
}
