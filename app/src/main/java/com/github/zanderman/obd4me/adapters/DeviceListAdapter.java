package com.github.zanderman.obd4me.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.zanderman.obd.classes.OBDAdapter;
import com.github.zanderman.obd4me.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


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
public class DeviceListAdapter extends ArrayAdapter<OBDAdapter> implements View.OnClickListener {


    /**
     * Constant Private Members.
     */
    private final Context context;
    private final ArrayList<OBDAdapter> devices;

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
        this.devices = null;
    }


    /**
     * Constructor:
     *      DeviceListAdapter( Context, int, ArrayList<OBDAdapter> )
     *
     * Description:
     *      Provides adapter object with resource ID and List of objects.
     *
     * @param context   ...
     * @param resource  ...
     * @param objects   ...
     */
    public DeviceListAdapter(Context context, int resource, ArrayList<OBDAdapter> objects) {
        super(context, resource, objects);

        /**
         * Initialize members.
         */
        this.context = context;
        this.devices = objects;
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

        // Setup device layout inflater.
        // Might be able to use: LayoutInflater.from(this.context);
        LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate the custom layout.
        View row = inflater.inflate(R.layout.obd_item, parent, false);

        /**
         * Access all elements from within the custom view.
         */
        TextView nameTextView = (TextView) row.findViewById(R.id.nameTextView);
        TextView addressTextView = (TextView) row.findViewById(R.id.addressTextView);

        /**
         * Populate information on the view elements.
         */
        nameTextView.setText(this.devices.get(position).name);
        addressTextView.setText(this.devices.get(position).address);

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
         * TODO:
         *      1) pass device to main activity.
         *      2) start new activity with device that was clicked.
         *          a) interact with device in activity.
         */
    }
}
