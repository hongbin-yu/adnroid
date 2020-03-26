package dajana.utils;

public enum MIMEType {
    IMAGE("image/*"), VIDEO("video/*"),AUDIO("audeo/*"),FILE("file/*");
    public String value;

    MIMEType(String value) {
        this.value = value;
    }
}
