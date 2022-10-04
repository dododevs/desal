package revolver.desal.api.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import revolver.desal.api.model.response.BaseResponse;
import revolver.desal.api.model.request.ItemCreationRequest;
import revolver.desal.api.model.request.ItemUpdateRequest;
import revolver.desal.api.model.response.InventoryResponse;
import revolver.desal.api.services.inventory.SaleItem;

public interface InventoryService {

    @POST("inventory")
    Call<InventoryResponse> getInventory(@Query("sid") String sid);

    @POST("inventory/add")
    Call<BaseResponse> createNewItem(@Query("sid") String sid, @Body ItemCreationRequest creationRequest);

    @POST("inventory/update")
    Call<BaseResponse> updateItem(@Body ItemUpdateRequest updateRequest);

    @POST("inventory/sell")
    Call<BaseResponse> sellItem(@Query("sid") String sid, @Body SaleItem item);

    @POST("inventory/{oid}/onPumpRemoved")
    Call<BaseResponse> removeItem(@Path("oid") String oid);
}
