package com.canalprep.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class LicenseManager {

    private static final String LICENSE_FILE_NAME = "license.dat";

    public static boolean isLicenseValid() {
        File licenseFile = new File(LICENSE_FILE_NAME);
        if (!licenseFile.exists()) {
            return generateLicense();
        } else {
            return validateLicense(licenseFile);
        }
    }

    private static boolean generateLicense() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LICENSE_FILE_NAME))) {
            LicenseData licenseData = new LicenseData(LocalDate.now());
            oos.writeObject(licenseData);
            System.out.println("License generated successfully. Valid until: " + licenseData.getExpirationDate());
            return true;
        } catch (IOException e) {
            System.err.println("Error generating license: " + e.getMessage());
            return false;
        }
    }

    private static boolean validateLicense(File licenseFile) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(licenseFile))) {
            LicenseData licenseData = (LicenseData) ois.readObject();
            LocalDate installationDate = licenseData.getInstallationDate();
            LocalDate expirationDate = installationDate.plus(365, ChronoUnit.DAYS);

            if (LocalDate.now().isBefore(expirationDate) || LocalDate.now().isEqual(expirationDate)) {
                System.out.println("License is valid. Expires on: " + expirationDate);
                return true;
            } else {
                System.err.println("License expired on: " + expirationDate);
                return false;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error validating license: " + e.getMessage());
            return false;
        }
    }

    private static class LicenseData implements Serializable {
        private static final long serialVersionUID = 1L;
        private LocalDate installationDate;

        public LicenseData(LocalDate installationDate) {
            this.installationDate = installationDate;
        }

        public LocalDate getInstallationDate() {
            return installationDate;
        }

        public LocalDate getExpirationDate() {
            return installationDate.plus(365, ChronoUnit.DAYS);
        }
    }
}


