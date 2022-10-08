package revolver.desal.api;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import revolver.desal.DeSal;
import revolver.desal.api.adapter.BitmapFactoryAdapter;
import revolver.desal.api.adapter.QrCodeSizeFactoryAdapter;
import revolver.desal.api.adapter.TransactionAdapter;
import revolver.desal.api.services.QrCodeService;
import revolver.desal.api.services.users.Session;
import revolver.desal.api.services.InventoryService;
import revolver.desal.api.services.PdfModelsService;
import revolver.desal.api.services.SessionService;
import revolver.desal.api.services.ShiftsService;
import revolver.desal.api.services.StationsService;
import revolver.desal.api.services.TransactionsService;
import revolver.desal.api.services.UsersService;
import revolver.desal.api.services.ValidationService;
import revolver.desal.api.services.transactions.Transaction;

public final class RestAPI {

    private static final Gson defaultGson;

    private static final Retrofit retrofit;
    private static final Retrofit authenticatedRetrofit;

    private static final SessionService sessionService;
    private static final UsersService usersService;
    private static final ValidationService validationService;
    private static final StationsService stationsService;
    private static final ShiftsService shiftsService;
    private static final InventoryService inventoryService;
    private static final TransactionsService transactionsService;
    private static final PdfModelsService modelsService;
    private static final QrCodeService qrCodeService;

    private static OnRefreshCurrentSessionRequested onRefreshCurrentSessionRequestedListener;
    private static Session currentSession;

    static {
        defaultGson = new GsonBuilder()
                .registerTypeAdapter(Transaction.class, new TransactionAdapter())
                .create();
        retrofit = new Retrofit.Builder()
                .baseUrl(DeSal.API_URL)
                .addConverterFactory(GsonConverterFactory.create(defaultGson))
                .build();
        authenticatedRetrofit = new Retrofit.Builder()
                .baseUrl(retrofit.baseUrl())
                .client(new OkHttpClient().newBuilder().cache(null).addInterceptor(new Interceptor() {
                    @NonNull
                    @Override
                    public Response intercept(@NonNull Chain chain) throws IOException {
                        return chain.proceed(chain.request().newBuilder()
                                .cacheControl(CacheControl.FORCE_NETWORK)
                                .addHeader("X-Token", currentSession != null ?
                                        currentSession.getToken() :
                                            (currentSession = onRefreshCurrentSessionRequestedListener
                                                .getCurrentSession()).getToken())
                                .build());
                    }
                }).build())
                .addConverterFactory(GsonConverterFactory.create(defaultGson))
                .build();

        usersService = retrofit.create(UsersService.class);
        sessionService = authenticatedRetrofit.create(SessionService.class);
        validationService = authenticatedRetrofit.create(ValidationService.class);
        stationsService = authenticatedRetrofit.create(StationsService.class);
        shiftsService = authenticatedRetrofit.create(ShiftsService.class);
        inventoryService = authenticatedRetrofit.create(InventoryService.class);
        transactionsService = authenticatedRetrofit.create(TransactionsService.class);
        modelsService = authenticatedRetrofit.create(PdfModelsService.class);

        final Retrofit qrRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.qrserver.com/v1/")
                .addConverterFactory(new BitmapFactoryAdapter())
                .addConverterFactory(new QrCodeSizeFactoryAdapter())
                .build();
        qrCodeService = qrRetrofit.create(QrCodeService.class);
    }

    public static void setOnRefreshCurrentSessionRequestedListener(OnRefreshCurrentSessionRequested l) {
        onRefreshCurrentSessionRequestedListener = l;
    }

    public static void setCurrentSession(Session s) {
        currentSession = s;
    }

    public static Session getCurrentSession() {
        return currentSession;
    }

    public static Gson getDefaultGson() {
        return defaultGson;
    }

    public static UsersService getUsersService() {
        return usersService;
    }

    public static SessionService getSessionService() {
        return sessionService;
    }

    public static ValidationService getValidationService() {
        return validationService;
    }

    public static StationsService getStationsService() {
        return stationsService;
    }

    public static ShiftsService getShiftsService() {
        return shiftsService;
    }

    public static InventoryService getInventoryService() {
        return inventoryService;
    }

    public static TransactionsService getTransactionsService() {
        return transactionsService;
    }

    public static PdfModelsService getModelsService() {
        return modelsService;
    }

    public static QrCodeService getQrCodeService() {
        return qrCodeService;
    }

    public interface OnRefreshCurrentSessionRequested {
        Session getCurrentSession();
    }
}
