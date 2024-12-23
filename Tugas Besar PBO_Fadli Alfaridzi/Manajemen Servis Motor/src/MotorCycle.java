class MotorCycle extends Motor {
    private String type;

    public MotorCycle(String plateNumber, String ownerName, String type) {
        super(plateNumber, ownerName);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return super.toString() + ", Tipe Motor: " + type;
    }
}