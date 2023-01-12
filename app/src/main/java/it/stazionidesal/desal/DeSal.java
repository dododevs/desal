package it.stazionidesal.desal;

import android.app.Application;
import android.content.Context;
import android.graphics.Typeface;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import it.stazionidesal.desal.api.RestAPI;
import it.stazionidesal.desal.api.services.users.Session;
import it.stazionidesal.desal.api.services.stations.GasPrice;
import it.stazionidesal.desal.api.services.stations.GasPump;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.util.platform.Prefs;
import it.stazionidesal.desal.util.ui.M;

public class DeSal extends Application {

    public static final String API_URL = "https://desal.altervista.org/api/v2/";

    @Override
    public void onCreate() {
        super.onCreate();
        M.setDisplayMetrics(getResources().getDisplayMetrics());

        Fonts.Montserrat.Black = Typeface.createFromAsset(
                getAssets(), "fonts/montserrat/Montserrat-Black.ttf");
        Fonts.Montserrat.Bold = Typeface.createFromAsset(
                getAssets(), "fonts/montserrat/Montserrat-Bold.ttf");
        Fonts.Montserrat.ExtraBold = Typeface.createFromAsset(
                getAssets(), "fonts/montserrat/Montserrat-ExtraBold.ttf");
        Fonts.Montserrat.Light = Typeface.createFromAsset(
                getAssets(), "fonts/montserrat/Montserrat-Light.ttf");
        Fonts.Montserrat.Medium = Typeface.createFromAsset(
                getAssets(), "fonts/montserrat/Montserrat-Medium.ttf");
        Fonts.Montserrat.Regular = Typeface.createFromAsset(
                getAssets(), "fonts/montserrat/Montserrat-Regular.ttf");
        Fonts.Montserrat.SemiBold = Typeface.createFromAsset(
                getAssets(), "fonts/montserrat/Montserrat-SemiBold.ttf");
        Fonts.Montserrat.Thin = Typeface.createFromAsset(
                getAssets(), "fonts/montserrat/Montserrat-Thin.ttf");

        RestAPI.setOnRefreshCurrentSessionRequestedListener(() -> {
            if (isPersistentSessionAvailable(DeSal.this)) {
                return getPersistentSession(DeSal.this);
            } else {
                return null;
            }
        });
    }

    public static class Fonts {
        public static class Montserrat {
            public static Typeface Black, Bold, ExtraBold, Light, Medium, Regular, SemiBold, Thin;
        }
    }

    public static void persistSession(Context context, Session session) {
        final Gson gson = RestAPI.getDefaultGson();
        String json = gson.toJson(session);
        Prefs.put(context, "session", json);
    }

    public static Session getPersistentSession(Context context) {
        final Gson gson = RestAPI.getDefaultGson();

        final String json = Prefs.get(context, "session");
        if (json == null || json.isEmpty()) {
            return null;
        }

        final Session session;
        try {
            session = gson.fromJson(json, Session.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
        if (session == null || session.getToken() == null || session.getUserType() == null
                || (!session.isVerified() && session.getValidation() == null)) {
            return null;
        }

        return session;
    }

    public static boolean isPersistentSessionAvailable(Context context) {
        return Prefs.exists(context, "session");
    }

    public static void removePersistentSession(Context context) {
        if (isPersistentSessionAvailable(context)) {
            Prefs.remove(context, "session");
        }
    }

    public static void persistSelectedStation(Context context, GasStation station) {
        final Gson gson = RestAPI.getDefaultGson();
        String json = gson.toJson(station);
        Prefs.put(context, "selectedStation", json);
    }

    public static GasStation getPersistentSelectedStation(Context context) {
        final Gson gson = RestAPI.getDefaultGson();

        final String json = Prefs.get(context, "selectedStation");
        if (json == null || json.isEmpty()) {
            return null;
        }

        final GasStation station;
        try {
            station = gson.fromJson(json, GasStation.class);
        } catch (JsonSyntaxException e) {
            return null;
        }

        if (station == null || station.getSid() == null || station.getName() == null ||
                station.getPumps() == null || station.getPumps().isEmpty()) {
            return null;
        } else {
            for (final GasPrice price : station.getPrices()) {
                if (price == null || price.getFuel() == null || price.getType() == null) {
                    return null;
                }
            }
            for (final GasPump pump : station.getPumps()) {
                if (pump == null || pump.getPid() == null || pump.getDisplay() == null || pump.getAvailableFuel() == null) {
                    return null;
                }
            }
        }

        return station;
    }

    public static boolean isPersistentSelectedStationAvailable(Context context) {
        return Prefs.exists(context, "selectedStation");
    }
}
