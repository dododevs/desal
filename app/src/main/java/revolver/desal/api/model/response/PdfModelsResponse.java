package revolver.desal.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import revolver.desal.api.services.models.PdfModel;

public class PdfModelsResponse extends BaseResponse {

    @SerializedName("models")
    private List<PdfModel> pdfModels;

    public List<PdfModel> getPdfModels() {
        return pdfModels;
    }

}
