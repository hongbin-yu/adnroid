package wang.switchy.hin2n.storage.db.base.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

@Entity(
        nameInDb = "ZerotierSettingList"//表名
)

public class ZerotierSettingModel {
    @Id(autoincrement = true)
    private Long id;
    private String nwid;
    private String ip;
    private String name;
    private String username;
    private String password;
    private String macaddress;
    private String netmask;
    private boolean isSelcected;
    private boolean isAuto;
    private boolean isWifi;
    @Generated(hash = 159390240)
    public ZerotierSettingModel(Long id, String nwid, String ip, String name, String username, String password, String macaddress, String netmask, boolean isSelcected, boolean isAuto, boolean isWifi) {
        this.id = id;
        this.nwid = nwid;
        this.ip = ip;
        this.name = name;
        this.username = username;
        this.password = password;
        this.macaddress = macaddress;
        this.netmask = netmask;
        this.isSelcected = isSelcected;
        this.isAuto = isAuto;
        this.isWifi = isWifi;
    }

    public ZerotierSettingModel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNwid() {
        return nwid;
    }

    public void setNwid(String nwid) {
        this.nwid = nwid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMacaddress() {
        return macaddress;
    }

    public void setMacaddress(String macaddress) {
        this.macaddress = macaddress;
    }

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public boolean getIsSelcected() {
        return this.isSelcected;
    }

    public boolean isSelcected() {
        return isSelcected;
    }

    public void setSelcected(boolean selcected) {
        isSelcected = selcected;
    }

    public void setIsSelcected(boolean selcected) {
        isSelcected = selcected;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public boolean getIsAuto() {
        return this.isAuto;
    }

    public void setAuto(boolean auto) {
        isAuto = auto;
    }

    public void setIsAuto(boolean auto) {
        isAuto = auto;
    }

    public boolean isWifi() {
        return isWifi;
    }

    public boolean getIsWifi() {
        return this.isWifi;
    }

    public void setWifi(boolean wifi) {
        isWifi = wifi;
    }

    public void setIsWifi(boolean wifi) {
        isWifi = wifi;
    }

}
