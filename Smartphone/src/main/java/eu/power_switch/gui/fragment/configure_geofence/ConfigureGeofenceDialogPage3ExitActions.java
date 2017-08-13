/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.fragment.configure_geofence;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import eu.power_switch.R;
import eu.power_switch.action.Action;
import eu.power_switch.event.ActionAddedEvent;
import eu.power_switch.google_play_services.geofence.Geofence;
import eu.power_switch.gui.adapter.ActionRecyclerViewAdapter;
import eu.power_switch.gui.dialog.AddActionDialog;
import eu.power_switch.gui.dialog.configuration.ConfigurationDialogPage;
import eu.power_switch.gui.dialog.configuration.holder.GeofenceConfigurationHolder;

/**
 * Created by Markus on 12.09.2015.
 */
public class ConfigureGeofenceDialogPage3ExitActions extends ConfigurationDialogPage<GeofenceConfigurationHolder> {

    private static final int REQUEST_CODE = 2;

    @BindView(R.id.add_action)
    FloatingActionButton addActionFAB;
    @BindView(R.id.recyclerview_list_of_actions)
    RecyclerView         recyclerViewTimerActions;

    private ArrayList<Action>         currentExitActions;
    private ActionRecyclerViewAdapter actionRecyclerViewAdapter;

    /**
     * Used to notify the setup page that some info has changed
     */
    public void updateConfiguration(ArrayList<Action> actions) {
        getConfiguration().setExitActions(actions);

        notifyConfigurationChanged();
    }

    /**
     * Used to add Actions from AddActionDialog
     *
     * @param action Action
     */
    public void addAction(Action action) {
        currentExitActions.add(action);
        actionRecyclerViewAdapter.notifyDataSetChanged();

        updateConfiguration(currentExitActions);
    }

    @Override
    protected void onRootViewInflated(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final Fragment  fragment = this;
        IconicsDrawable icon     = iconicsHelper.getFabIcon(MaterialDesignIconic.Icon.gmi_plus);
        addActionFAB.setImageDrawable(icon);
        addActionFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddActionDialog addActionDialog = AddActionDialog.newInstance(fragment, REQUEST_CODE);
                addActionDialog.show(getFragmentManager(), null);
            }
        });

        currentExitActions = new ArrayList<>();
        actionRecyclerViewAdapter = new ActionRecyclerViewAdapter(getActivity(), persistenceHandler, currentExitActions);
        actionRecyclerViewAdapter.setOnDeleteClickListener(new ActionRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                currentExitActions.remove(position);
                actionRecyclerViewAdapter.notifyDataSetChanged();
                updateConfiguration(currentExitActions);
            }
        });
        recyclerViewTimerActions.setAdapter(actionRecyclerViewAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewTimerActions.setLayoutManager(layoutManager);

        initializeData();

        updateConfiguration(currentExitActions);
    }

    @Override
    protected void showTutorial() {
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.dialog_fragment_configure_geofence_page_3;
    }

    private void initializeData() {
        Geofence geofence = getConfiguration().getGeofence();
        if (geofence != null) {
            try {
                currentExitActions.clear();
                currentExitActions.addAll(geofence.getActions(Geofence.EventType.EXIT));
            } catch (Exception e) {
                statusMessageHandler.showErrorMessage(getContentView(), e);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @SuppressWarnings("unused")
    public void onActionAdded(ActionAddedEvent e) {
        if (e.getRequestCode() != REQUEST_CODE) {
            // ignore event of other view
            return;
        }

        addAction(e.getAction());
    }

}
