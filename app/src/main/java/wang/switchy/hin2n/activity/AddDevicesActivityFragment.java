package wang.switchy.hin2n.activity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import wang.switchy.hin2n.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddDevicesActivityFragment extends Fragment {

    public AddDevicesActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_devices, container, false);
    }
}
