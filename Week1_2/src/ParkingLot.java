import java.util.*;

public class ParkingLot {

    enum Status {
        EMPTY, OCCUPIED, DELETED
    }

    static class ParkingSpot {
        String licensePlate;
        long entryTime;
        Status status;

        ParkingSpot() {
            this.status = Status.EMPTY;
        }
    }

    private ParkingSpot[] table;
    private int capacity;
    private int size;

    private int totalProbes = 0;
    private int totalParks = 0;

    // Track hourly usage
    private Map<Integer, Integer> hourlyTraffic = new HashMap<>();

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.table = new ParkingSpot[capacity];

        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Hash function
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle
    public String parkVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        while (probes < capacity) {
            int current = (index + probes) % capacity;

            if (table[current].status == Status.EMPTY ||
                    table[current].status == Status.DELETED) {

                table[current].licensePlate = licensePlate;
                table[current].entryTime = System.currentTimeMillis();
                table[current].status = Status.OCCUPIED;

                totalProbes += probes;
                totalParks++;

                // Track peak hour
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                hourlyTraffic.put(hour, hourlyTraffic.getOrDefault(hour, 0) + 1);

                return "Assigned spot #" + current + " (" + probes + " probes)";
            }

            probes++;
        }

        return "Parking Full!";
    }

    // Exit vehicle
    public String exitVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        while (probes < capacity) {
            int current = (index + probes) % capacity;

            if (table[current].status == Status.EMPTY) {
                return "Vehicle not found!";
            }

            if (table[current].status == Status.OCCUPIED &&
                    table[current].licensePlate.equals(licensePlate)) {

                long durationMs = System.currentTimeMillis() - table[current].entryTime;
                double hours = durationMs / (1000.0 * 60 * 60);

                double fee = hours * 5; // $5/hour

                table[current].status = Status.DELETED;

                size--;

                return "Spot #" + current + " freed, Duration: "
                        + String.format("%.2f", hours)
                        + " hrs, Fee: $" + String.format("%.2f", fee);
            }

            probes++;
        }

        return "Vehicle not found!";
    }

    // Find nearest available spot (from entrance = 0)
    public int findNearestAvailable() {
        for (int i = 0; i < capacity; i++) {
            if (table[i].status == Status.EMPTY) {
                return i;
            }
        }
        return -1;
    }

    // Get statistics
    public String getStatistics() {
        int occupied = 0;

        for (ParkingSpot spot : table) {
            if (spot.status == Status.OCCUPIED) {
                occupied++;
            }
        }

        double occupancyRate = (occupied * 100.0) / capacity;
        double avgProbes = (totalParks == 0) ? 0 : (double) totalProbes / totalParks;

        // Find peak hour
        int peakHour = -1, max = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyTraffic.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                peakHour = entry.getKey();
            }
        }

        return "Occupancy: " + String.format("%.2f", occupancyRate) + "%" +
                ", Avg Probes: " + String.format("%.2f", avgProbes) +
                ", Peak Hour: " + peakHour + ":00";
    }

    // MAIN
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(10);

        System.out.println(lot.parkVehicle("ABC-1234"));
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));

        Thread.sleep(2000);

        System.out.println(lot.exitVehicle("ABC-1234"));

        System.out.println("Nearest available: " + lot.findNearestAvailable());

        System.out.println(lot.getStatistics());
    }
}