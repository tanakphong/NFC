package com.deverdie.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "dlg";

    private NfcAdapter mNfcAdapter = null;
    private PendingIntent mNfcPendingIntent = null;
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private TextView TvDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TvDisplay = findViewById(R.id.display);

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
            String cardNr = byteArrayToHexString(tag.getId());
            Log.d(TAG, "cardNr: " + cardNr);
            StringBuilder builder = new StringBuilder();
            for (int i = (cardNr.length() - 1); i >= 0; i -= 2) {
                builder.append(cardNr.substring((i), 2));

            }
            Log.d(TAG, "cardNr Reverse: " + builder.toString());

            TvDisplay.setText(cardNr + ":" + builder.toString());
            Toast.makeText(getApplicationContext(), cardNr + ":" + builder.toString(), Toast.LENGTH_SHORT).show();

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
            Toast.makeText(getApplicationContext(), "NFC adapter exists", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "no NFC adapter exists");
            Toast.makeText(getApplicationContext(), "no NFC adapter exists", Toast.LENGTH_SHORT).show();
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
}
