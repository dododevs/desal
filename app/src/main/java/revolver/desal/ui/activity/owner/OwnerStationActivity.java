package revolver.desal.ui.activity.owner;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import revolver.desal.R;
import revolver.desal.api.services.stations.GasStation;
import revolver.desal.ui.activity.DeSalFragmentHostActivity;
import revolver.desal.ui.fragment.owner.station.OwnerStationInfoFragment;
import revolver.desal.ui.fragment.owner.station.OwnerStationInventoryFragment;
import revolver.desal.ui.fragment.owner.station.OwnerStationOilFragment;
import revolver.desal.ui.fragment.owner.station.OwnerStationShiftsFragment;
import revolver.desal.util.ui.ColorUtils;
import revolver.desal.util.ui.TextUtils;

public class OwnerStationActivity extends AppCompatActivity implements DeSalFragmentHostActivity {

    private GasStation mStation;

    private ProgressBar mToolbarWheel;
    private ViewPager2 mPager;
    private CoordinatorLayout mSnackbarContainer;

    private static final int mSectionCount = 4;
    private final ImageView[] mChooserItems = new ImageView[mSectionCount];
    private int mChooserSelectedColor, mChooserUnselectedColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_station);

        mStation = getIntent().getParcelableExtra("station");
        if (mStation == null) {
            Toast.makeText(this, R.string.error_no_station_selected, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(TextUtils.capitalizeWords(mStation.getName()));
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        mToolbarWheel = findViewById(R.id.wheel);
        mSnackbarContainer = findViewById(R.id.activity_owner_station);

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new SectionsAdapter(getSupportFragmentManager(), getLifecycle()));
        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffsetPixels == 0) {
                    return;
                }

                int leaving = ColorUtils.interpolateBetweenColors(
                        mChooserSelectedColor, mChooserUnselectedColor, positionOffset);
                int approaching = ColorUtils.interpolateBetweenColors(
                        mChooserUnselectedColor, mChooserSelectedColor, positionOffset);
                mChooserItems[position].setImageTintList(ColorStateList.valueOf(leaving));
                mChooserItems[position + 1].setImageTintList(ColorStateList.valueOf(approaching));
            }
        });

        mChooserItems[0] = findViewById(R.id.activity_owner_station_chooser_info);
        setupChooserItemClickListener(mChooserItems[0], 0);
        mChooserItems[1] = findViewById(R.id.activity_owner_station_chooser_accessories);
        setupChooserItemClickListener(mChooserItems[1], 1);
        mChooserItems[2] = findViewById(R.id.activity_owner_station_chooser_oil);
        setupChooserItemClickListener(mChooserItems[2], 2);
        mChooserItems[3] = findViewById(R.id.activity_owner_station_chooser_shifts);
        setupChooserItemClickListener(mChooserItems[3], 3);

        mChooserSelectedColor = ColorUtils.get(this, R.color.colorPrimaryDark);
        mChooserUnselectedColor = ColorUtils.get(this, R.color.midGray);
    }

    private void setupChooserItemClickListener(View v, int index) {
        final int i = index;
        v.setOnClickListener(v1 -> {
            if (mPager.getCurrentItem() != i) {
                mPager.setCurrentItem(i);
            }
        });
    }

    public void startLoading() {
        mToolbarWheel.setVisibility(View.VISIBLE);
    }

    public void stopLoading() {
        mToolbarWheel.setVisibility(View.GONE);
    }

    public CoordinatorLayout getSnackbarContainer() {
        return mSnackbarContainer;
    }

    public GasStation getStation() {
        return mStation;
    }

    private static class SectionsAdapter extends FragmentStateAdapter {
        SectionsAdapter(@NonNull FragmentManager fm, @NonNull Lifecycle lifecycle) {
            super(fm, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:
                    return new OwnerStationInventoryFragment();
                case 2:
                    return new OwnerStationOilFragment();
                case 3:
                    return new OwnerStationShiftsFragment();
                default:
                case 0:
                    return new OwnerStationInfoFragment();
            }
        }

        @Override
        public int getItemCount() {
            return mSectionCount;
        }
    }
}
