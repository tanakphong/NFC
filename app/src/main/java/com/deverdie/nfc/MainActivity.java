package com.deverdie.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "dlg";
//    private TextView text;
//    private NfcAdapter nfcAdapter;
//    private PendingIntent pendingIntent;

    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mNfcPendingIntent = null;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private TextView TvDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TvDisplay = findViewById(R.id.text);

        registerNFC();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcAdapter != null && mNfcPendingIntent != null) {
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            StringBuilder builder = new StringBuilder();
            builder.append("Hex:".concat(toHex(tag.getId()).concat(System.lineSeparator())));
            builder.append("Reversed Hex: ".concat(toReversedHex(tag.getId()).concat(System.lineSeparator())));
            builder.append(readMadSector(tag));

            TvDisplay.setText(builder.toString());
//            String cardNr = byteArrayToHexString(tag.getId());
//            Log.d(TAG, "cardNr: " + cardNr);
//            StringBuilder builder = new StringBuilder();
//            for (int i = (cardNr.length() - 2); i >= 0; i -= 2) {
//                builder.append(Character.toString(cardNr.charAt(i)).concat(Character.toString(cardNr.charAt(i + 1))));
//
//            }
//            Log.d(TAG, "cardNr Reverse: " + builder.toString());
//
//            TvDisplay.setText(cardNr + ":" + builder.toString());
//            Toast.makeText(getApplicationContext(), cardNr + ":" + builder.toString(), Toast.LENGTH_SHORT).show();

//            int p = 0;
//            String[] objs= new String[4];
//            for(int i=0;i<4;i++){
//                objs[i] = cardNr.substring(p,(p+2));
//                p+=2;
//            }
//            String CardNo = objs[3]+objs[2]+objs[1]+objs[0];
        }
    }

    private void registerNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mNfcPendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
            Log.i(TAG, "NFC adapter exists");
        } else {
            Log.d(TAG, "no NFC adapter exists");
        }
    }

    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private String readMadSector(Tag tag) {
        byte[] array = {(byte) 0xD3, (byte) 0xF7, (byte) 0xD3, (byte) 0xF7, (byte) 0xD3, (byte) 0xF7};
        byte[] data = null;
        byte[] b;
        StringBuilder sb = new StringBuilder();
        boolean success;
        try {
            MifareClassic mClassic = MifareClassic.get(tag);
            mClassic.connect();
            sb.append("getBlockCount: ".concat(String.valueOf(mClassic.getBlockCount())).concat(System.lineSeparator()));
            sb.append("getBlockCountInSector: ".concat(String.valueOf(mClassic.getBlockCountInSector(1))).concat(System.lineSeparator()));
//            sb.append("readBlock 10: ".concat(String.valueOf(toHex(mClassic.readBlock(10)))).concat(System.lineSeparator()));

            success = mClassic.authenticateSectorWithKeyA(1, MifareClassic.KEY_DEFAULT);
            if (success) {
                sb.append("readBlock 4: ".concat(String.valueOf(toHex(mClassic.readBlock(4)))).concat(System.lineSeparator()));

                sb.append("readBlock 5: ".concat(String.valueOf(toHex(mClassic.readBlock(5)))).concat(System.lineSeparator()));

                sb.append("readBlock 6: ".concat(String.valueOf(toHex(mClassic.readBlock(6)))).concat(System.lineSeparator()));
            } else {
                sb.append("Authentication failed");

                mClassic.close();

            }

        } catch (final TagLostException tE) {
            tE.printStackTrace();
            sb.append("Tag Lost");
        } catch ( final IOException iE){
            iE.printStackTrace();
            sb.append("IOEception");
        }


        return (sb.toString());
    }


    //    private String dumpTagData(Tag tag) {
//        StringBuilder sb = new StringBuilder();
//        byte[] id = tag.getId();
//        sb.append("ID (hex): ").append(toHex(id)).append('\n');
//        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
//        sb.append("ID (dec): ").append(toDec(id)).append('\n');
//        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');
//
//        String prefix = "android.nfc.tech.";
//        sb.append("Technologies: ");
//        for (String tech : tag.getTechList()) {
//            sb.append(tech.substring(prefix.length()));
//            sb.append(", ");
//        }
//
//        sb.delete(sb.length() - 2, sb.length());
//
//        for (String tech : tag.getTechList()) {
//            if (tech.equals(MifareClassic.class.getName())) {
//                sb.append('\n');
//                String type = "Unknown";
//
//                try {
//                    MifareClassic mifareTag = MifareClassic.get(tag);
//
//                    switch (mifareTag.getType()) {
//                        case MifareClassic.TYPE_CLASSIC:
//                            type = "Classic";
//                            break;
//                        case MifareClassic.TYPE_PLUS:
//                            type = "Plus";
//                            break;
//                        case MifareClassic.TYPE_PRO:
//                            type = "Pro";
//                            break;
//                    }
//                    sb.append("Mifare Classic type: ");
//                    sb.append(type);
//                    sb.append('\n');
//
//                    sb.append("Mifare size: ");
//                    sb.append(mifareTag.getSize() + " bytes");
//                    sb.append('\n');
//
//                    sb.append("Mifare sectors: ");
//                    sb.append(mifareTag.getSectorCount());
//                    sb.append('\n');
//
//                    sb.append("Mifare blocks: ");
//                    sb.append(mifareTag.getBlockCount());
//                } catch (Exception e) {
//                    sb.append("Mifare classic error: " + e.getMessage());
//                }
//            }
//
//            if (tech.equals(MifareUltralight.class.getName())) {
//                sb.append('\n');
//                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
//                String type = "Unknown";
//                switch (mifareUlTag.getType()) {
//                    case MifareUltralight.TYPE_ULTRALIGHT:
//                        type = "Ultralight";
//                        break;
//                    case MifareUltralight.TYPE_ULTRALIGHT_C:
//                        type = "Ultralight C";
//                        break;
//                }
//                sb.append("Mifare Ultralight type: ");
//                sb.append(type);
//            }
//        }
//
//        return sb.toString();
//    }
//

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    private long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        text = (TextView) findViewById(R.id.text);
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
//
//        if (nfcAdapter == null) {
//            Toast.makeText(this, "No NFC", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//
//        pendingIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, this.getClass())
//                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        if (nfcAdapter != null) {
//            if (!nfcAdapter.isEnabled())
//                showWirelessSettings();
//
//            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
//        }
//    }
//
//    private void showWirelessSettings() {
//        Toast.makeText(this, "You need to enable NFC", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//        startActivity(intent);
//    }
//
//    @Override
//    protected void onNewIntent(Intent intent) {
//        setIntent(intent);
//        resolveIntent(intent);
//    }
//
//    private void resolveIntent(Intent intent) {
//        String action = intent.getAction();
//
//        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
//            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            NdefMessage[] msgs;
//
//            if (rawMsgs != null) {
//                msgs = new NdefMessage[rawMsgs.length];
//
//                for (int i = 0; i < rawMsgs.length; i++) {
//                    msgs[i] = (NdefMessage) rawMsgs[i];
//                }
//
//            } else {
//                byte[] empty = new byte[0];
//                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
//                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//                byte[] payload = dumpTagData(tag).getBytes();
//                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
//                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
//                msgs = new NdefMessage[] {msg};
//            }
//
//            displayMsgs(msgs);
//        }
//    }
//
//    private void displayMsgs(NdefMessage[] msgs) {
//        if (msgs == null || msgs.length == 0)
//            return;
//
//        StringBuilder builder = new StringBuilder();
//        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
//        final int size = records.size();
//
//        for (int i = 0; i < size; i++) {
//            ParsedNdefRecord record = records.get(i);
//            String str = record.str();
//            builder.append(str).append("\n");
//        }
//
//        text.setText(builder.toString());
//    }
}
