package dajana.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ZerotierSettingInfo implements Parcelable {
    private String nwid;
    private String ip;
    private String name;
    private String username;
    private String password;
    private String macaddress;
    private String netmask;

    public ZerotierSettingInfo(String nwid, String ip, String name, String username, String password, String macaddress, String netmask) {
        this.nwid = nwid;
        this.ip = ip;
        this.name = name;
        this.username = username;
        this.password = password;
        this.macaddress = macaddress;
        this.netmask = netmask;
    }


    protected ZerotierSettingInfo(Parcel in) {
        nwid = in.readString();
        ip = in.readString();
        name = in.readString();
        username = in.readString();
        password = in.readString();
        macaddress = in.readString();
        netmask = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nwid);
        dest.writeString(ip);
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(macaddress);
        dest.writeString(netmask);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ZerotierSettingInfo> CREATOR = new Creator<ZerotierSettingInfo>() {
        @Override
        public ZerotierSettingInfo createFromParcel(Parcel in) {
            return new ZerotierSettingInfo(in);
        }

        @Override
        public ZerotierSettingInfo[] newArray(int size) {
            return new ZerotierSettingInfo[size];
        }
    };

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
}
