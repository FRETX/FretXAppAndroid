package fretx.version4.utils.bluetooth;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;

import fretx.version4.R;

/**
 * FretXAppAndroid for FretX
 * Created by pandor on 11/07/17 18:00.
 */

public class ScanResultDialog extends DialogFragment {
    private Dialog dialog;
    private ListView listView;
    private SparseArray<BluetoothDevice> devices;
    private ArrayList<String> deviceNames = new ArrayList<>();

    public static ScanResultDialog newInstance(SparseArray<BluetoothDevice> devices) {
        final ScanResultDialog dialog = new ScanResultDialog();
        dialog.devices = devices;
        return dialog;
    }

    public ScanResultDialog() {}

    @Override
    @NonNull
    @SuppressWarnings("unchecked")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.scan_result_dialog);

        for (int i = 0; i < devices.size(); ++i) {
            final int key = devices.keyAt(i);
            final String name = devices.get(key).getName() + " (" + devices.get(key).getAddress() + ")";
            deviceNames.add(name);
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, deviceNames);

        listView = (ListView) dialog.findViewById(R.id.listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                Toast.makeText(getActivity(), "Connecting to FretX...", Toast.LENGTH_SHORT).show();
                Bluetooth.getInstance().connect(devices.get(devices.keyAt(position)));
            }
        });

        return dialog;
    }
}
