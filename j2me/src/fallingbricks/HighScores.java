package fallingbricks;

import java.io.IOException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

/**
 *
 * @author Aaron
 */
public class HighScores {
    private final FallingBricksMidlet midlet;
    private final String rsName;
    private final int[] highScores;

    public HighScores(FallingBricksMidlet midlet,
            String rsName, int highScoreCount) {
        this.midlet = midlet;
        this.rsName = rsName;
        this.highScores = new int[highScoreCount];
    }

    public void load() throws IOException {
        RecordStore rs;
        try {
            rs = RecordStore.openRecordStore(rsName, false);
        } catch (RecordStoreNotFoundException ex) {
            midlet.reportProgress(100);
            return;
        } catch (RecordStoreException ex) {
            throw new IOException("Could not open record store '"
                    + rsName + "'");
        }
        midlet.reportProgress(30);
        RecordEnumeration re = null;
        try {
            re = rs.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                byte[] record = re.nextRecord();
                int index = record[0];
                highScores[index] = fromBytesToInt(record, 1);
            }
            midlet.reportProgress(100);
        } catch (RecordStoreException ex) {
            throw new IOException("Could not read from record store '"
                    + rsName + "'");
        } finally {
            if (re != null) {
                re.destroy();
            }
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (RecordStoreException ex) { }
            }
        }
    }

    /**
     * Deserializes an integer from a byte array.
     *
     * @param b byte array containing representation of integer.
     * @param off starting point of integer representation in byte array.
     *
     * @return integer represented in byte array.
     */
    private static int fromBytesToInt(byte[] b, int off) {
        return ((b[off+0] & 0xFF) << 24) +
                ((b[off+1] & 0xFF) << 16) +
                ((b[off+2] & 0xFF) << 8) +
                (b[off+3] & 0xFF);
    }

    public void save() throws IOException {
        RecordStore rs;
        try {
            rs = RecordStore.openRecordStore(rsName, true);
        } catch (RecordStoreException ex) {
            throw new IOException("Could not open record store '"
                    + rsName + "'");
        }
        midlet.reportProgress(20);

        // Delete all records first.
        RecordEnumeration re = null;
        try {
            re = rs.enumerateRecords(null, null, false);
            while (re.hasNextElement()) {
                int recordId = re.nextRecordId();
                rs.deleteRecord(recordId);
            }
            midlet.reportProgress(60);
        } catch (RecordStoreException ex) {
            throw new IOException("Could not truncate record store '"
                    + rsName + "'");
        } finally {
            if (re != null) {
                re.destroy();
            }
        }

        // Now save high scores.
        try {
            int progress = 60;
            int progressInc = (100 - progress) / highScores.length;
            for (int i = 0; i < highScores.length; i++) {
                byte[] record = new byte[5];
                record[0] = (byte)i;
                toBytes(highScores[i], record, 1);
                rs.addRecord(record, 0, record.length);
                midlet.reportProgress(progress += progressInc);
            }
            midlet.reportProgress(100);
        } catch (RecordStoreException ex) {
            throw new IOException("Could not write to record store '"
                    + rsName + "'");
        } finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                } catch (RecordStoreException ex) { }
            }
        }
    }

    /**
     * Serializes an integer into a byte array.
     *
     * @param i integer to be serialized.
     * @param b byte array which will receive serialized integer.
     * @param off starting point in byte array for receiving serialized
     * integer.
     */
    private static void toBytes(int i, byte[] b, int off) {
        b[off+0] = (byte)(i >> 24);
        b[off+1] = (byte)(i >> 16);
        b[off+2] = (byte)(i >> 8);
        b[off+3] = (byte)i;
    }

    public int[] getHighScores() {
        return highScores;
    }

    public boolean setHighScores(int[] newHighScores) {
        boolean saveRequired = false;
        for (int i = 0; i < highScores.length; i++) {
            if (newHighScores[i] > highScores[i]) {
                highScores[i] = newHighScores[i];
                saveRequired = true;
            }
        }
        return saveRequired;
    }
}
