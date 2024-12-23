import java.util.*;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Workshop workshop = new Workshop();

        boolean running = true;
        while (running) {
            System.out.println("\n\n======================== Alfaridzi Service Motor =======================\n");
            System.out.println("\nMenu:\n");
            System.out.println("1. Input data Servis");
            System.out.println("2. Slot Servis, Antrian dan History");
            System.out.println("3. Hapus Servis (Selesai Servis)");
            System.out.println("4. Hapus Data Motor dari Database");
            System.out.println("5. Edit Data Servis");
            System.out.println("6. Edit Data Servis Database");
            System.out.println("7. Exit");
            System.out.print("\nInput Opsi: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: // Input data Servis
                    System.out.print("\nNama Pemilik Motor: ");
                    String ownerName = scanner.nextLine();
                    System.out.print("\nNomor Kendaraan: ");
                    String plateNumber = scanner.nextLine();
                    System.out.print("\nTipe Motor: ");
                    String type = scanner.nextLine();

                    MotorCycle motor = new MotorCycle(plateNumber, ownerName, type);
                    workshop.addMotorToService(motor);

                    System.out.println("\nPilih Tipe Servis:");
                    System.out.println("1. Servis Ringan (Rp. 250,000)");
                    System.out.println("2. Servis Lengkap (Rp. 400,000)");
                    System.out.println("3. Ganti Oli dan Kampas Rem (Rp. 175,000)");
                    System.out.print("\nInput Tipe Servis: ");
                    int serviceChoice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    String description = "";
                    double cost = 0;

                    switch (serviceChoice) {
                        case 1:
                            description = "Servis Ringan";
                            cost = 100000;
                            break;
                        case 2:
                            description = "Servis Lengkap";
                            cost = 200000;
                            break;
                        case 3:
                            description = "Ganti Oli dan Kampas Rem";
                            cost = 150000;
                            break;
                        default:
                            System.out.println("Pilihan Tipe Servis Tidak Valid.");
                            continue;
                    }

                    RegularService service = new RegularService(description, cost);
                    workshop.performService(motor, service);
                    System.out.println("Servis Ditambahkan: " + description + ", Total Harga: Rp. " + cost);
                    break;

                    case 2: // Lihat History Servis
                    // Menampilkan tanggal saat ini
                    LocalDate currentDate = LocalDate.now();
                    System.out.println("\nTanggal: " + currentDate);
                    System.out.println("Motor di slot servis aktif:");
    
                    List<MotorCycle> activeSlots = workshop.getActiveServiceSlots();
                    if (activeSlots.isEmpty()) {
                        System.out.println("- Tidak ada motor di slot servis.");
                    } else {
                        for (int i = 0; i < activeSlots.size(); i++) {
                            MotorCycle activemotor = activeSlots.get(i);
                            System.out.println((i + 1) + ". " + activemotor);
                        }
                    }

                    System.out.println("\nMotor dalam antrean servis:");
                    Queue<MotorCycle> queue = workshop.getServiceQueue();
                    if (queue.isEmpty()) {
                        System.out.println("- Tidak ada motor dalam antrean.");
                    } else {
                        int queuePosition = 1;
                        for (MotorCycle queuemotor : queue) {
                            System.out.println(queuePosition + ". " + queuemotor);
                            queuePosition++;
                        }
                    }

                    System.out.println("\nIngin melihat riwayat servis motor tertentu? (y/n)");
                    String response = scanner.nextLine();
                    if (response.equalsIgnoreCase("y")) {
                        System.out.print("Masukkan nomor plat motor: ");
                        String viewPlate = scanner.nextLine();

                        Optional<MotorCycle> motorToView = activeSlots.stream()
                                .filter(m -> m.getPlateNumber().equals(viewPlate))
                                .findFirst();

                        if (motorToView.isPresent()) {
                            workshop.displayServiceHistory(motorToView.get());
                        } else {
                            System.out.println("Motor tidak ditemukan di slot servis.");
                        }
                    }
                    break;
                    case 3: // Hapus History Servis
                    workshop.displayActiveServiceSlots();
                    System.out.print("Masukkan Nomor Slot Servis yang Selesai: ");
                    int slotIndex = scanner.nextInt() - 1;
                    scanner.nextLine(); // Consume newline
                    workshop.removeMotorFromService(slotIndex);
                    break;

                case 4: // Edit Data Servis

                System.out.print("Masukkan nomor plat motor yang ingin dihapus: ");
                    String plateToDelete = scanner.nextLine();

                    // Hapus motor berdasarkan nomor plat
                    workshop.deleteMotorFromDatabase(plateToDelete);

                
                break;
                case 5: // Edit
                System.out.println("Motor di slot servis aktif:");
                List<MotorCycle> activeMotors = workshop.getActiveServiceSlots();
                if (activeMotors.isEmpty()) {
                    System.out.println("- Tidak ada motor di slot servis.");
                } else {
                    for (int i = 0; i < activeMotors.size(); i++) {
                        System.out.println((i + 1) + ". " + activeMotors.get(i));
                    }
                
                    System.out.print("Pilih nomor motor yang ingin diedit: ");
                    int motorIndex = scanner.nextInt() - 1;
                    scanner.nextLine(); // Consume newline
                
                    if (motorIndex < 0 || motorIndex >= activeMotors.size()) {
                        System.out.println("Nomor motor tidak valid.");
                    } else {
                        MotorCycle selectedMotor = activeMotors.get(motorIndex);
                
                        System.out.println("Riwayat servis:");
                        List<Service> services = workshop.getServiceHistory(selectedMotor);
                        if (services.isEmpty()) {
                            System.out.println("- Tidak ada riwayat servis.");
                        } else {
                            for (int i = 0; i < services.size(); i++) {
                                Service currentservice = services.get(i);
                                System.out.println((i + 1) + ". " + currentservice.getDescription() + " (Rp. " + currentservice.getCost() + ")");
                            }
                
                            System.out.print("Pilih nomor servis yang ingin diedit: ");
                            int serviceIndex = scanner.nextInt() - 1;
                            scanner.nextLine(); // Consume newline
                
                            if (serviceIndex < 0 || serviceIndex >= services.size()) {
                                System.out.println("Nomor servis tidak valid.");
                            } else {                
                                System.out.println("Edit detail servis:");
                                System.out.print("Deskripsi baru: ");
                                String newDescription = scanner.nextLine();
                                System.out.print("Biaya baru: ");
                                double newCost = scanner.nextDouble();
                                scanner.nextLine(); // Consume newline
                
                                // Update servis
                                services.set(serviceIndex, new RegularService(newDescription, newCost));
                                System.out.println("Servis berhasil diperbarui.");
                            }
                        }
                    }
                }
                break;
                case 6: // Edit Data Motor
                    System.out.print("Masukkan nomor plat motor yang ingin diedit: ");
                    String editPlateNumber = scanner.nextLine();

                    // Mengambil data motor berdasarkan nomor plat
                    Motor motorToEdit = workshop.getMotorByPlateNumber(editPlateNumber);
                    if (motorToEdit != null) {
                        System.out.println("Motor yang ingin diedit: " + motorToEdit);

                        // Mengedit data motor
                        System.out.print("Nama Pemilik baru: ");
                        String newOwnerName = scanner.nextLine();
                        System.out.print("Tipe Motor baru: ");
                        String newType = scanner.nextLine();

                        // Update data motor
                        workshop.updateMotorInDatabase(editPlateNumber, newOwnerName, newType);
                    } else {
                        System.out.println("Motor dengan nomor plat " + editPlateNumber + " tidak ditemukan.");
                    }
                    break;
                                case 7:
                                    running = false;
                                break;
                                default:
                                    System.out.println("Invalid choice.");
                                break;
                            }
                        }

                        scanner.close();
                        System.out.println("Program ended.");
                    }
}