package com.dhruv.guide;

import com.mbientlab.metawear.api.MetaWearController;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;

public abstract class ModuleFragment extends DialogFragment {
    protected MainActivity mwMnger;

    public void clearBluetoothDevice() { }

    public interface MetaWearManager {
        public MetaWearController getCurrentController();
        public boolean hasController();
        public boolean controllerReady();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof MainActivity)) {
            throw new IllegalStateException(
                    "Activity must implement fragment's callbacks.");
        }
        mwMnger= (MainActivity) activity;
    }

    public abstract void controllerReady(MetaWearController mwController);
}