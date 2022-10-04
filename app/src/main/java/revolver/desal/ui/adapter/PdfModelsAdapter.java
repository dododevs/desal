package revolver.desal.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import revolver.desal.R;
import revolver.desal.api.services.models.Attributes;
import revolver.desal.api.services.models.PdfModel;
import revolver.desal.util.ui.TextUtils;
import revolver.desal.util.ui.ViewUtils;

public class PdfModelsAdapter extends RecyclerView.Adapter<PdfModelsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final WebView mPreviewView;
        final ProgressBar mPreviewWheelView;
        final TextView mTitleView;
        final TextView mSubtitleView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight().verticalMargin(16.f).get());

            mPreviewView = v.findViewById(R.id.item_pdf_model_preview);
            mPreviewWheelView = v.findViewById(R.id.item_pdf_model_preview_wheel);
            mTitleView = v.findViewById(R.id.item_pdf_model_title);
            mSubtitleView = v.findViewById(R.id.item_pdf_model_subtitle);
        }
    }

    private Context mContext;
    private List<PdfModel> mPdfModels;
    private OnModelClickListener mListener;

    public PdfModelsAdapter(List<PdfModel> models) {
        mPdfModels = models;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_pdf_model, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final PdfModel model = mPdfModels.get(i);

        viewHolder.mPreviewView.getSettings().setUseWideViewPort(true);
        viewHolder.mPreviewView.getSettings().setLoadWithOverviewMode(true);
        viewHolder.mPreviewView.loadData(model.getPage1(), "text/html", null);
        viewHolder.mPreviewView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                viewHolder.mPreviewWheelView.setVisibility(View.GONE);
            }
        });
        viewHolder.mTitleView.setText(TextUtils.capitalizeWords(model.getAttributes().getName()));
        viewHolder.mSubtitleView.setText(buildSubtitle(model.getAttributes()));

        viewHolder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onModelClicked(mPdfModels.get(viewHolder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPdfModels.size();
    }

    public void updateModels(List<PdfModel> models) {
        mPdfModels = models;
        notifyDataSetChanged();
    }

    public void setOnModelClickListener(OnModelClickListener listener) {
        mListener = listener;
    }

    private String buildSubtitle(final Attributes attributes) {
        return mContext.getString(R.string.item_pdf_model_subtitle, attributes.getPumps().size());
    }

    public interface OnModelClickListener {
        void onModelClicked(final PdfModel model);
    }
}
