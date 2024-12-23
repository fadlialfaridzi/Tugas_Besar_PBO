// File: Motor.java
class Motor {
    private String plateNumber;
    private String ownerName;

    public Motor(String plateNumber, String ownerName) {
        this.plateNumber = plateNumber;
        this.ownerName = ownerName;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public String getOwnerName() {
        return ownerName;
    }

    @Override
    public String toString() {
        return "Motor [Nomor Kendaraan: " + plateNumber + ", Nama Pemilik: " + ownerName + "]";
    }
}