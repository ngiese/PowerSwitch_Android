package eu.power_switch.gui.fragment.wizard;

import android.os.Bundle;

import eu.power_switch.R;

/**
 * Created by Markus on 04.11.2016.
 */
public class SetupApartmentPage extends SingleLineTextInputPage {

    public static SetupApartmentPage newInstance() {
        Bundle args = new Bundle();
        SetupApartmentPage fragment = new SetupApartmentPage();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void onSetUiValues() {
        super.onSetUiValues();

        setTitle(R.string.configure_apartment);
        setHint("Home");
        setDescription(R.string.tutorial__apartment_explanation);
    }

    @Override
    public int getDefaultBackgroundColor() {
        return getResources().getColor(R.color.blue);
    }
}
