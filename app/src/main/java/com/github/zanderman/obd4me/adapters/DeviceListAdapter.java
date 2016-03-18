package com.github.zanderman.obd4me.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zanderman.obd.classes.OBDAdapter;
import com.github.zanderman.obd4me.R;
import com.github.zanderman.obd4me.activities.HUDActivity;

import java.util.ArrayList;


/**
 * Class:
 *      DeviceListAdapter
 *
 * Description:
 *      ...
 *
 * Author:
 *      Alexander DeRieux
 */
public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> implements View.OnClickListener {


    /**
     * Constant Private Members.
     */
    private final Context context;
    private final ArrayList<BluetoothDevice> devices;

    /**
     * Constructor:
     *      DeviceListAdapter( Context, int )
     *
     * Description:
     *      Provides adapter object with resource ID.
     *
     * @param context   Context in which the adapter appears.
     * @param resource  Resource ID number.
     */
    public DeviceListAdapter(Context context, int resource) {
        super(context, resource);

        /**
         * Initialize members.
         */
        this.context = context;
        this.devices = new ArrayList<BluetoothDevice>();
    }


    /**
     * Method:
     *      getView( int, View, ViewGroup )
     *
     * Description:
     *      ...
     *
     * @param   position        ...
     * @param   convertView     ...
     * @param   parent          ...
     * @return  View            ...
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Create row view.
        View row = convertView;

        // Check if view is being recycled.
        if (row == null) {
            // Setup device layout inflater.
            LayoutInflater inflater = LayoutInflater.from(this.context);

            // Inflate the custom layout.
            row = inflater.inflate(R.layout.obd_item, parent, false);
        }

        /**
         * Access all elements from within the custom view.
         */
        TextView nameTextView = (TextView) row.findViewById(R.id.nameTextView);
        TextView addressTextView = (TextView) row.findViewById(R.id.addressTextView);

        /**
         * Populate information on the view elements.
         */
        nameTextView.setText(this.devices.get(position).getName());
        addressTextView.setText(this.devices.get(position).getAddress());

        // Set this custom view's tag.
        row.setTag(new String[]{((Integer) position).toString()});

        // Set the custom view's clickable OnClick listener as this ListView adapter class.
        row.setOnClickListener(this);

        // Return the completed custom layout.
        return row;
    }


    /**
     * Method:
     *      onClick( View )
     *
     * Description:
     *      ...
     *
     * @param v     ...
     */
    @Override
    public void onClick(View v) {

        // Get the click device's index within the array of devices.
        int indexInAdapter = Integer.parseInt(((String[]) v.getTag())[0]);

        /**
         * Create new intent to that:
         *      1. Starts the HUD activity.
         *      2. Transfers selected OBDAdapter object.
         */
        Intent intent = new Intent(this.context, HUDActivity.class);
        intent.putExtra("device", this.devices.get(indexInAdapter));

        // Start the activity.
        this.context.startActivity(intent);

        Toast.makeText(getContext(), "Clicked: " + indexInAdapter, Toast.LENGTH_LONG);
    }


    /**
     * Method:
     *      add( BluetoothDevice )
     *
     * Description:
     *      ...
     *
     * @param object
     */
    @Override
    public void add(BluetoothDevice object) {
        super.add(object);

        // Add the adapter to the device list.
        this.devices.add(object);
    }

    /**
     * Method:
     *      contains( OBDAdapter )
     *
     * Description:
     *      Determines whether a given OBDAdapter object is present
     *      within the device list.
     *
     * @param   adapter     BluetoothAdapter to be checked.
     * @return boolean      Search status.
     */
    public boolean contains(BluetoothDevice adapter) {

        /**
         * Iterate over all elements within the device list
         * and determine if 'adapter' is present.
         */
        for (BluetoothDevice curr : this.devices) {
            if (curr.equals(adapter))
                return true;
        }

        // Adapter is not within the device list.
        return false;
    }
}
