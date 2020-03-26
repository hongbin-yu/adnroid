package wang.switchy.hin2n.storage.db.base.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by janiszhang on 2018/5/4.
 */

@Entity(
        nameInDb = "N2NSettingList"//表名
)
public class N2NSettingModel /*implements Parcelable*/ {

    @Id(autoincrement = true)
    Long id;
    int version;
    String name;
    String ip;
    String netmask;
    String community;
    /**
     * 后续需要加密存储
     */
    String password;
    String superNode;
    boolean moreSettings;
    String superNodeBackup;
    String macAddr;
    int mtu;
    String localIP;
    int holePunchInterval;
    boolean resoveSupernodeIP;
    int localPort;
    boolean allowRouting;
    boolean dropMuticast;
    boolean useHttpTunnel;
    int traceLevel;
    boolean isSelcected;
    String deviceID;
    String deviceIP;
    String username;



    public String getSuperNode() {
        return this.superNode;
    }

    public void setSuperNode(String superNode) {
        this.superNode = superNode;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCommunity() {
        return this.community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getNetmask() {
        return this.netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsSelcected() {
        return this.isSelcected;
    }

    public void setIsSelcected(boolean isSelcected) {
        this.isSelcected = isSelcected;
    }

    public int getTraceLevel() {
        return this.traceLevel;
    }

    public void setTraceLevel(int traceLevel) {
        this.traceLevel = traceLevel;
    }

    public boolean getDropMuticast() {
        return this.dropMuticast;
    }

    public void setDropMuticast(boolean dropMuticast) {
        this.dropMuticast = dropMuticast;
    }

    public boolean getAllowRouting() {
        return this.allowRouting;
    }

    public void setAllowRouting(boolean allowRouting) {
        this.allowRouting = allowRouting;
    }

    public int getLocalPort() {
        return this.localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public boolean getResoveSupernodeIP() {
        return this.resoveSupernodeIP;
    }

    public void setResoveSupernodeIP(boolean resoveSupernodeIP) {
        this.resoveSupernodeIP = resoveSupernodeIP;
    }

    public int getHolePunchInterval() {
        return this.holePunchInterval;
    }

    public void setHolePunchInterval(int holePunchInterval) {
        this.holePunchInterval = holePunchInterval;
    }

    public String getLocalIP() {
        return this.localIP;
    }

    public void setLocalIP(String localIP) {
        this.localIP = localIP;
    }

    public int getMtu() {
        return this.mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    public String getMacAddr() {
        return this.macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public String getSuperNodeBackup() {
        return this.superNodeBackup;
    }

    public void setSuperNodeBackup(String superNodeBackup) {
        this.superNodeBackup = superNodeBackup;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isUseHttpTunnel() {
        return useHttpTunnel;
    }

    public void setUseHttpTunnel(boolean useHttpTunnel) {
        this.useHttpTunnel = useHttpTunnel;
    }

    public String getDeviceID() {return this.deviceID;}

    public void setDeviceID(String deviceID ) { this.username = username;}

    public String getDeviceIP() {return this.deviceIP;}

    public void setDeviceIP(String deviceIP ) { this.deviceIP = deviceIP;}

    public String getUsername() {return this.username;}

    public void setUsername(String username ) { this.username = username;}

    @Generated(hash = 1990821763)
    public N2NSettingModel(Long id, int version, String name, String ip, String netmask, String community, String password,
            String superNode, boolean moreSettings, String superNodeBackup, String macAddr, int mtu, String localIP,
            int holePunchInterval, boolean resoveSupernodeIP, int localPort, boolean allowRouting, boolean dropMuticast,
            boolean useHttpTunnel, int traceLevel, boolean isSelcected, String deviceID, String deviceIP, String username) {
        this.id = id;
        this.version = version;
        this.name = name;
        this.ip = ip;
        this.netmask = netmask;
        this.community = community;
        this.password = password;
        this.superNode = superNode;
        this.moreSettings = moreSettings;
        this.superNodeBackup = superNodeBackup;
        this.macAddr = macAddr;
        this.mtu = mtu;
        this.localIP = localIP;
        this.holePunchInterval = holePunchInterval;
        this.resoveSupernodeIP = resoveSupernodeIP;
        this.localPort = localPort;
        this.allowRouting = allowRouting;
        this.dropMuticast = dropMuticast;
        this.useHttpTunnel = useHttpTunnel;
        this.traceLevel = traceLevel;
        this.isSelcected = isSelcected;
        this.deviceID = deviceID;
        this.deviceIP = deviceIP;
        this.username = username;
    }

    @Generated(hash = 998225630)
    public N2NSettingModel() {
    }

    public boolean getMoreSettings() {
        return this.moreSettings;
    }

    public void setMoreSettings(boolean moreSettings) {
        this.moreSettings = moreSettings;
    }

    @Override
    public String toString() {
        return "N2NSettingModel{" +
                "id=" + id +
                ", version=" + version +
                ", name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", netmask='" + netmask + '\'' +
                ", community='" + community + '\'' +
                ", password='" + password + '\'' +
                ", superNode='" + superNode + '\'' +
                ", moreSettings=" + moreSettings +
                ", superNodeBackup='" + superNodeBackup + '\'' +
                ", macAddr='" + macAddr + '\'' +
                ", mtu=" + mtu +
                ", localIP='" + localIP + '\'' +
                ", holePunchInterval=" + holePunchInterval +
                ", resoveSupernodeIP=" + resoveSupernodeIP +
                ", localPort=" + localPort +
                ", allowRouting=" + allowRouting +
                ", dropMuticast=" + dropMuticast +
                ", useHttpTunnel=" + useHttpTunnel +
                ", traceLevel=" + traceLevel +
                ", isSelcected=" + isSelcected +
                ", deviceID="+deviceID+
                ", deviceIP="+deviceIP+
                ", username="+username+
                '}';
    }

    public boolean getUseHttpTunnel() {
        return this.useHttpTunnel;
    }
}
