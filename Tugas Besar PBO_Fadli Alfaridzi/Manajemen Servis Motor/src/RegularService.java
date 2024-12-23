class RegularService implements Service {
    private String description;
    private double cost;

    public RegularService(String description, double cost) {
        this.description = description;
        this.cost = cost;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public void performService() {
        // No print statement to avoid duplication
    }
}