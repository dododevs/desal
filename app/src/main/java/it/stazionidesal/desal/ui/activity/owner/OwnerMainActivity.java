package it.stazionidesal.desal.ui.activity.owner;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.ui.fragment.UserDetailFragment;
import it.stazionidesal.desal.ui.fragment.owner.OwnerStationsFragment;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.ViewUtils;

public class OwnerMainActivity extends AppCompatActivity {

    private List<GasStation> mStations;

    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private final LinearLayout[] mNavigationItems = new LinearLayout[3];
    private int mNavigationSelectedItem = Integer.MAX_VALUE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_main);

        mStations = getIntent().getParcelableArrayListExtra("stations");

        mDrawerLayout = findViewById(R.id.activity_owner_main);

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(v -> mDrawerLayout.openDrawer(GravityCompat.START));

        final ImageView userButton = findViewById(R.id.activity_owner_main_navi_user);
        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        new UserDetailFragment()
                                .show(getSupportFragmentManager(), "userDetailDialog");
                        mDrawerLayout.removeDrawerListener(this);
                    }
                });
            }
        });

        final NavigationView navigationView = findViewById(R.id.navi);
        mNavigationItems[0] = navigationView.findViewById(R.id.activity_owner_main_navi_stations);
        mNavigationItems[1] = navigationView.findViewById(R.id.activity_owner_main_navi_validation);
        mNavigationItems[2] = navigationView.findViewById(R.id.activity_owner_main_navi_settings);
        for (int i = 0; i < mNavigationItems.length; i++) {
            final int j = i;
            mNavigationItems[i].setOnClickListener(v -> setNavigationSelectedItem(j));
        }

        setNavigationSelectedItem(0);
    }

    private void setNavigationSelectedItem(int index) {
        if (index == mNavigationSelectedItem) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        switch (index) {
            case 0: // stations
                mToolbar.setTitle(R.string.activity_owner_main_title_stations);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, OwnerStationsFragment.withStations(mStations))
                        .commit();
                break;
            case 1: // validation
                mDrawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, OwnerValidationActivity.class));
                return;
            case 2: // settings
                mToolbar.setTitle(R.string.activity_owner_main_title_settings);
                break;
        }

        final int darkGray = ColorUtils.get(this, R.color.darkGray);
        final int colorAccent = ColorUtils.get(this, R.color.colorAccent);
        for (int i = 0; i < mNavigationItems.length; i++) {
            final LinearLayout itemLayout = mNavigationItems[i];
            final ImageView itemIcon = (ImageView) itemLayout.getChildAt(0);
            final TextView itemLabel = (TextView) itemLayout.getChildAt(1);
            if (i == index) {
                itemLayout.setBackgroundResource(R.drawable.item_activity_owner_main_navi_background);
                itemIcon.setImageTintList(ColorStateList.valueOf(colorAccent));
                itemLabel.setTextColor(ColorStateList.valueOf(colorAccent));
            } else {
                mNavigationItems[i].setBackground(ViewUtils.getSelectableBackgroundDrawable(this));
                itemIcon.setImageTintList(ColorStateList.valueOf(darkGray));
                itemLabel.setTextColor(ColorStateList.valueOf(darkGray));
            }
        }

        mNavigationSelectedItem = index;
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }
}
