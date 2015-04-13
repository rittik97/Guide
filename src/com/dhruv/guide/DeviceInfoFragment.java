package com.dhruv.guide;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

import com.mbientlab.metawear.api.MetaWearController;
import com.mbientlab.metawear.api.Module;
import com.mbientlab.metawear.api.MetaWearController.DeviceCallbacks;
import com.mbientlab.metawear.api.GATT.GATTCharacteristic;
import com.mbientlab.metawear.api.MetaWearController.ModuleCallbacks;
import com.mbientlab.metawear.api.characteristic.Battery;
import com.mbientlab.metawear.api.characteristic.DeviceInformation;
import com.mbientlab.metawear.api.controller.Debug;
import com.mbientlab.metawear.api.controller.MechanicalSwitch;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class DeviceInfoFragment extends ModuleFragment {
	private MechanicalSwitch switchController;
	private Debug debugController;
	private DeviceCallbacks dCallback= new MetaWearController.DeviceCallbacks() {
		@Override
		public void connected() {
			mwMnger.getCurrentController().readDeviceInformation();
			switchController.enableNotification();
		}

		@Override
		public void receivedGATTCharacteristic(
				GATTCharacteristic characteristic, byte[] data) {
			if (characteristic == Battery.BATTERY_LEVEL) {
				values.put(characteristic, String.format(Locale.US, "%s", data[0]));
			} else {
				values.put(characteristic, new String(data));
			}
			final Integer viewId= views.get(characteristic);
			if (viewId != null && isVisible()) {
				((TextView) getView().findViewById(viewId)).setText(values.get(characteristic));
			}
		}

		@Override
		public void receivedRemoteRSSI(int rssi) {
			if (isVisible()) {
				((TextView) getView().findViewById(R.id.remote_rssi)).setText(String.format(Locale.US, "%d ", rssi));
			}
		}

		@Override
		public void disconnected() {
			for(Entry<GATTCharacteristic, Integer> it: views.entrySet()) {
				values.remove(it.getKey());
				((TextView) getView().findViewById(it.getValue())).setText("");
			}
		}
	};
	private ModuleCallbacks mCallback= new MechanicalSwitch.Callbacks() {
		@Override
		public void pressed() {
			if (isVisible()) {
				((TextView) getView().findViewById(R.id.mechanical_switch)).setText("Pressed");
			}
		}

		@Override
		public void released() {
			if (isVisible()) {
				((TextView) getView().findViewById(R.id.mechanical_switch)).setText("Released");
			}
		}
	};

	private HashMap<GATTCharacteristic, String> values= new HashMap<>();
	private final static HashMap<GATTCharacteristic, Integer> views= new HashMap<>();
	static {
		views.put(DeviceInformation.MANUFACTURER_NAME, R.id.manufacturer_name);
		views.put(DeviceInformation.SERIAL_NUMBER, R.id.serial_number);
		views.put(DeviceInformation.FIRMWARE_VERSION, R.id.firmware_version);
		views.put(DeviceInformation.HARDWARE_VERSION, R.id.hardware_version);
		views.put(Battery.BATTERY_LEVEL, R.id.battery_level);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_device_info, container, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d("metawear", "Device Info Created");
		if (savedInstanceState != null) {
			values= (HashMap<GATTCharacteristic, String>) savedInstanceState.getSerializable("STATE_VALUES");
		}
		for(Entry<GATTCharacteristic, String> it: values.entrySet()) {
			Integer viewId= views.get((GATTCharacteristic) it.getKey());
			if (viewId != null) {
				((TextView) view.findViewById(viewId)).setText(it.getValue());
			}
		}

		((TextView) view.findViewById(R.id.textView1)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mwMnger.controllerReady()) {
					mwMnger.getCurrentController().readBatteryLevel();
				} else {
					Toast.makeText(getActivity(), R.string.error_connect_board, Toast.LENGTH_LONG).show();
				}
			}
		});
		((TextView) view.findViewById(R.id.textView2)).setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mwMnger.controllerReady()) {
					mwMnger.getCurrentController().readRemoteRSSI();
				} else {
					Toast.makeText(getActivity(), R.string.error_connect_board, Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void onDestroy() {
		final MetaWearController mwController= mwMnger.getCurrentController();
		if (mwMnger.hasController()) {
			mwController.removeDeviceCallback(dCallback);
			mwController.removeModuleCallback(mCallback);
		}
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("STATE_VALUES", values);
	}

	@Override
	public void controllerReady(MetaWearController mwController) {
		switchController= (MechanicalSwitch) mwController.getModuleController(Module.MECHANICAL_SWITCH);
		debugController= (Debug) mwController.getModuleController(Module.DEBUG);

		mwController.addDeviceCallback(dCallback);
		mwController.addModuleCallback(mCallback);

		if (mwController.isConnected()) {
			mwController.readDeviceInformation();
			switchController.enableNotification();
		}
	}
}