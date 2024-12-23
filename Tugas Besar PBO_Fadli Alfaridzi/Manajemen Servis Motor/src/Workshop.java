import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


class Workshop {
    private static final int MAX_SLOT = 4;
    private Queue<MotorCycle> serviceQueue = new LinkedList<>();
    private List<MotorCycle> activeServiceSlots = new ArrayList<>();
    private Map<MotorCycle, List<Service>> serviceHistory = new HashMap<>();

    public void addMotorToService(MotorCycle motor) {
    try (Connection conn = DatabaseConnection.getConnection()) {
        String insertMotorSql = "INSERT INTO Motor (plate_number, owner_name, type) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertMotorSql);
        stmt.setString(1, motor.getPlateNumber());
        stmt.setString(2, motor.getOwnerName());
        stmt.setString(3, motor.getType());
        stmt.executeUpdate();

        // Memeriksa apakah slot servis aktif sudah penuh
        if (activeServiceSlots.size() < MAX_SLOT) {
            activeServiceSlots.add(motor);
            System.out.println("\nMotor Ditambahkan ke dalam Slot Servis: \n" + motor + "\n=======================================================");
        } else {
            // Jika slot penuh, masukkan ke antrian
            serviceQueue.add(motor);
            System.out.println("\nSlot Servis Penuh. Motor masuk ke antrian: \n" + motor + "\n=======================================================");
        }
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("Gagal menambahkan motor ke database.");
    }
}


    public void performService(MotorCycle motor, Service service) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Cari motor ID berdasarkan plate_number
            String findMotorSql = "SELECT id FROM Motor WHERE plate_number = ?";
            PreparedStatement findStmt = conn.prepareStatement(findMotorSql);
            findStmt.setString(1, motor.getPlateNumber());
            ResultSet rs = findStmt.executeQuery();
    
            if (rs.next()) {
                int motorId = rs.getInt("id");
    
                // Simpan data servis
                String sql = "INSERT INTO Service (motor_id, description, cost, service_date) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, motorId);
                stmt.setString(2, service.getDescription());
                stmt.setDouble(3, service.getCost());
                stmt.setDate(4, java.sql.Date.valueOf(java.time.LocalDate.now()));
                stmt.executeUpdate();
    
                System.out.println("Servis berhasil ditambahkan untuk motor ID: " + motorId);
            } else {
                System.out.println("Motor tidak ditemukan di database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gagal menyimpan data servis ke database.");
        }
    }
    

    public void displayServiceHistory(MotorCycle motor) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT m.owner_name, m.type, s.description, s.cost, s.service_date " +
                         "FROM Motor m JOIN Service s ON m.id = s.motor_id WHERE m.plate_number = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, motor.getPlateNumber());
            ResultSet rs = stmt.executeQuery();
    
            System.out.println("\nRiwayat Servis untuk motor dengan plat: " + motor.getPlateNumber());
            while (rs.next()) {
                System.out.println("Nama Pemilik: " + rs.getString("owner_name"));
                System.out.println("Tipe Motor: " + rs.getString("type"));
                System.out.println("Deskripsi Servis: " + rs.getString("description"));
                System.out.println("Biaya: Rp. " + rs.getDouble("cost"));
                System.out.println("Tanggal Servis: " + rs.getDate("service_date"));
                System.out.println("-------------------------------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Gagal membaca riwayat servis.");
        }
    }
    

    public void removeMotorFromService(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= activeServiceSlots.size()) {
            System.out.println("Invalid slot index.");
            return;
        }

        MotorCycle removedMotor = activeServiceSlots.remove(slotIndex);
        System.out.println("Motor removed from service slot: " + removedMotor);

        if (!serviceQueue.isEmpty()) {
            MotorCycle nextMotor = serviceQueue.poll();
            activeServiceSlots.add(nextMotor);
            System.out.println("Motor dipindahkan dari slot antrian ke slot aktif: " + nextMotor);
        }
    }

    public void displayActiveServiceSlots() {
        System.out.println("Slot Servis Aktif:");
        for (int i = 0; i < activeServiceSlots.size(); i++) {
            System.out.println((i + 1) + ". " + activeServiceSlots.get(i));
        }
    }

    public void displayQueue() {
        System.out.println("Slot Antrian Servis:");
        for (MotorCycle motor : serviceQueue) {
            System.out.println("- " + motor);
        }
    }

    public List<MotorCycle> getActiveServiceSlots() {
        return new ArrayList<>(activeServiceSlots); // Return a copy to ensure encapsulation
    }

    public Queue<MotorCycle> getServiceQueue() {
        return new LinkedList<>(serviceQueue); // Return a copy to ensure encapsulation
    }

    public List<Service> getServiceHistory(MotorCycle motor) {
    return serviceHistory.getOrDefault(motor, new ArrayList<>());
    
    }

    public void deleteMotorFromDatabase(String plateNumber) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Disable auto-commit to manage transaction
    
            // Menghapus riwayat servis
            String deleteServiceSql = "DELETE FROM Service WHERE motor_id IN (SELECT id FROM Motor WHERE plate_number = ?)";
            try (PreparedStatement deleteServiceStmt = conn.prepareStatement(deleteServiceSql)) {
                deleteServiceStmt.setString(1, plateNumber);
                deleteServiceStmt.executeUpdate();
            }
    
            // Menghapus motor dari slot servis aktif dan antrian jika ada
            activeServiceSlots.removeIf(motor -> motor.getPlateNumber().equals(plateNumber));
            serviceQueue.removeIf(motor -> motor.getPlateNumber().equals(plateNumber));
    
            // Menghapus motor dari tabel Motor
            String deleteMotorSql = "DELETE FROM Motor WHERE plate_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteMotorSql)) {
                stmt.setString(1, plateNumber);
                stmt.executeUpdate();
            }
    
            // Commit transaksi
            conn.commit();
            System.out.println("Motor dan riwayat servis telah berhasil dihapus.");
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaksi jika terjadi error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            System.out.println("Gagal menghapus motor dan riwayat servis.");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Kembalikan auto-commit ke true setelah transaksi selesai
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateMotorInDatabase(String plateNumber, String newOwnerName, String newType) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Motor SET owner_name = ?, type = ? WHERE plate_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newOwnerName);
                stmt.setString(2, newType);
                stmt.setString(3, plateNumber);
    
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Data motor berhasil diperbarui.");
                } else {
                    System.out.println("Gagal memperbarui data motor. Pastikan nomor plat benar.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Terjadi kesalahan saat memperbarui data motor.");
        }
    }
    
    public Motor getMotorByPlateNumber(String plateNumber) {
        Motor motor = null;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Motor WHERE plate_number = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, plateNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String ownerName = rs.getString("owner_name");
                        String type = rs.getString("type");
                        motor = new MotorCycle(plateNumber, ownerName, type);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return motor;
    }

}